package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage
import java.sql.SQLException
import java.util.*

class PlaytimeCommand(private val discordBot: DiscordBot) : Command() {
    override fun execute(event: CommandEvent) {
        if (!event.isFromType(ChannelType.TEXT)) return
        if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_PLAYTIME_ENABLED)) return

        if (event.args.isEmpty()) {
            val uuid = discordBot.discordUserManager.getUuidByDiscordId(event.author.id)
            if (uuid == null) {
                discordBot.logger.warning("${event.author.name} tried to use the playtime command but is not registed!")
                return
            }
            if (discordBot.networkManager.isPlayerOnline(uuid, true)) {
                val player = discordBot.networkManager.getPlayer(uuid) ?: return
                var jsonMessageEmbed = JsonMessageEmbed.fromJson(discordBot.messages.getString(DCMessage.COMMAND_PLAYTIME_RESPONSE))
                jsonMessageEmbed = jsonMessageEmbed.toBuilder()
                    .title(jsonMessageEmbed.title?.replace("%playername%", player.name))
                    .parsePlaceholdersToFields(mapOf(
                    "%playername%" to player.name,
                    "%playtime%" to TimeUtils.getTimeString(player.language, player.playtime / 1000),
                    "%liveplaytime%" to TimeUtils.getTimeString(player.language, player.livePlaytime / 1000)
                )).build()

                sendChannelMessage(event.textChannel,
                        jsonMessageEmbed.toMessageEmbed())
            } else {
                discordBot.scheduler.runAsync({
                    val result = getOfflinePlayerPlaytime(uuid)
                    val userName = result.first
                    val playtime = result.second
                    if (userName.isEmpty() || playtime == 0L) {
                        return@runAsync
                    }
                    var jsonMessageEmbed = JsonMessageEmbed.fromJson(discordBot.messages.getString(DCMessage.COMMAND_PLAYTIME_RESPONSE))
                    jsonMessageEmbed = jsonMessageEmbed.toBuilder()
                        .title(jsonMessageEmbed.title?.replace("%playername%", userName))
                        .parsePlaceholdersToFields(mapOf(
                            "%playername%" to userName,
                            "%playtime%" to TimeUtils.getTimeString(1, playtime / 1000),
                            "%liveplaytime%" to TimeUtils.getTimeString(1, playtime / 1000)
                        )).build()

                    sendChannelMessage(event.textChannel,
                            jsonMessageEmbed.toMessageEmbed())
                }, false)
            }
        }
    }

    private fun getOfflinePlayerPlaytime(uuid: UUID): Pair<String, Long> {
        var pair = Pair("", 0L)
        try {
            discordBot.mySQL.connection.use { connection ->
                connection.prepareStatement("SELECT `username`, `playtime` FROM nm_players WHERE `uuid`=?;").use { ps ->
                    ps.setString(1, uuid.toString())
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            val username = rs.getString("username")
                            val playtime = rs.getLong("playtime")
                            pair = Pair(username, playtime)
                        }
                    }
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
        return pair
    }

    init {
        name = discordBot.commandSettings.getString(CommandSetting.DISCORD_PLAYTIME_COMMAND)
        cooldown = 3
        botPermissions = arrayOf(Permission.MESSAGE_WRITE)
        guildOnly = true
    }
}