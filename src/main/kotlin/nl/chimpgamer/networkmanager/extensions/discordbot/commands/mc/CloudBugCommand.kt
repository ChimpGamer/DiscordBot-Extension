package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import cloud.commandframework.Command
import cloud.commandframework.arguments.standard.StringArgument
import net.dv8tion.jda.api.entities.MessageEmbed
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.api.utils.Cooldown
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils
import java.util.*

class CloudBugCommand(private val discordBot: DiscordBot) {

    fun getCommand(name: String, vararg aliases: String): Command.Builder<Sender> {
        val messageArgument = StringArgument.optional<Sender>("message", StringArgument.StringMode.GREEDY)
        return discordBot.networkManager.cloudCommandManager.commandManager.commandBuilder(name, *aliases)
            .senderType(Player::class.java)
            .argument(messageArgument)
            .handler { context ->
                val player = context.sender as Player
                val message = context.getOptional(messageArgument).orElse(null)
                if (message == null) {
                    player.sendMessage(discordBot.messages.getString(MCMessage.BUG_HELP))
                    return@handler
                }

                if (Cooldown.isInCooldown(player.uuid, name)) {
                    player.sendMessage(
                        "&7You have to wait &c%cooldown% &7before you can send a new bug report."
                            .replace(
                                "%cooldown%",
                                TimeUtils.getTimeString(
                                    player.language,
                                    Cooldown.getTimeLeft(player.uuid, "BugCMD").toLong()
                                )
                            )
                    )
                    return@handler
                }
                val bugReportChannel =
                    discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_BUGREPORT_CHANNEL))
                        ?: return@handler
                val jsonEmbedBuilder =
                    JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.BUGREPORT_ALERT))
                val fields: MutableList<MessageEmbed.Field> = LinkedList()
                for (field in jsonEmbedBuilder.fields) {
                    val fieldName = field.name
                    val fieldValue = field.value
                    if (fieldName == null || fieldValue == null) {
                        continue
                    }

                    val newFieldName = insertBugReportPlaceholders(fieldName, player, message)
                    val newFieldValue = insertBugReportPlaceholders(fieldValue, player, message)
                    val field1 = MessageEmbed.Field(newFieldName, newFieldValue, field.isInline)
                    fields.add(field1)
                }
                jsonEmbedBuilder.fields = fields
                Utils.sendChannelMessage(
                    bugReportChannel,
                    jsonEmbedBuilder.build()
                )
                player.sendMessage(discordBot.messages.getString(MCMessage.BUG_SUCCESS))
                Cooldown(player.uuid, name, 60).start()
            }
    }

    private fun insertBugReportPlaceholders(message: String, player: Player, bug: String): String {
        return Placeholders.Companion.setPlaceholders(player, message)
            .replace("%bug%", bug)
    }
}