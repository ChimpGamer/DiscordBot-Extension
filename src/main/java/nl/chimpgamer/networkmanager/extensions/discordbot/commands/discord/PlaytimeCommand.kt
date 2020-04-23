package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.MessageEmbed
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage
import java.sql.SQLException
import java.util.*

class PlaytimeCommand(private val discordBot: DiscordBot) : Command() {
    override fun execute(event: CommandEvent) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return
        }
        if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_PLAYTIME_ENABLED)) {
            return
        }
        if (event.args.isEmpty()) {
            val uuid = discordBot.discordUserManager.getUuidByDiscordId(event.author.id)
            if (uuid == null) {
                discordBot.logger.warning("${event.author.name} tried to use the playtime command but is not registed!")
                return
            }
            if (discordBot.networkManager.isPlayerOnline(uuid, true)) {
                val player = discordBot.networkManager.getPlayer(uuid)
                val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.COMMAND_PLAYTIME_RESPONSE))
                jsonEmbedBuilder.title = jsonEmbedBuilder.title?.replace("%playername%", player.name)
                val fields: MutableList<MessageEmbed.Field> = LinkedList()
                for (field in jsonEmbedBuilder.fields) {
                    val name = field.name
                            ?.replace("%playername%", player.name)
                            ?.replace("%playtime%", TimeUtils.getTimeString(player.language, player.playtime / 1000))
                            ?.replace("%liveplaytime%", TimeUtils.getTimeString(player.language, player.playtime / 1000))
                    val value = field.value
                            ?.replace("%playername%", player.name)
                            ?.replace("%playtime%", TimeUtils.getTimeString(player.language, player.playtime / 1000))
                            ?.replace("%liveplaytime%", TimeUtils.getTimeString(player.language, player.playtime / 1000))
                    val field1 = MessageEmbed.Field(name, value, field.isInline)
                    fields.add(field1)
                }
                jsonEmbedBuilder.fields = fields
                sendChannelMessage(event.textChannel,
                        jsonEmbedBuilder.build())
            } else {
                discordBot.scheduler.runAsync(Runnable {
                    val result = getOfflinePlayerPlaytime(uuid)
                    val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.COMMAND_PLAYTIME_RESPONSE))
                    jsonEmbedBuilder.title = jsonEmbedBuilder.title?.replace("%playername%", result[0])
                    val fields: MutableList<MessageEmbed.Field> = LinkedList()
                    for (field in jsonEmbedBuilder.fields) {
                        val name = field.name
                                ?.replace("%playername%", result[0])
                                ?.replace("%playtime%", TimeUtils.getTimeString(1, result[1].toLong() / 1000))
                                ?.replace("%liveplaytime%", TimeUtils.getTimeString(1, result[1].toLong() / 1000))
                        val value = field.value
                                ?.replace("%playername%", result[0])
                                ?.replace("%playtime%", TimeUtils.getTimeString(1, result[1].toLong() / 1000))
                                ?.replace("%liveplaytime%", TimeUtils.getTimeString(1, result[1].toLong() / 1000))
                        val field1 = MessageEmbed.Field(name, value, field.isInline)
                        fields.add(field1)
                    }
                    jsonEmbedBuilder.fields = fields
                    sendChannelMessage(event.textChannel,
                            jsonEmbedBuilder.build())
                }, false)
            }
        }
    }

    private fun getOfflinePlayerPlaytime(uuid: UUID): Array<String> {
        var result = arrayOf<String>()
        try {
            discordBot.mySQL.connection.use { connection ->
                connection.prepareStatement("SELECT username, playtime FROM nm_players WHERE uuid=?").use { ps ->
                    ps.setString(1, uuid.toString())
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            val username = rs.getString("username")
                            val playtime = rs.getLong("playtime")
                            result = arrayOf(username, playtime.toString())
                        }
                    }
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
        return result
    }

    init {
        name = discordBot.commandSettings.getString(CommandSetting.DISCORD_PLAYTIME_COMMAND)
        cooldown = 3
        botPermissions = arrayOf(Permission.MESSAGE_WRITE)
        guildOnly = true
    }
}