package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import net.dv8tion.jda.api.entities.MessageEmbed
import nl.chimpgamer.networkmanager.api.commands.NMBungeeCommand
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.sender.Sender
import nl.chimpgamer.networkmanager.api.utils.Cooldown
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage
import java.util.*

class SuggestionCommand(private val discordBot: DiscordBot, cmd: String) : NMBungeeCommand(discordBot.networkManager, cmd, listOf("networkmanager.bot.suggestion", "networkmanager.admin")) {
    override fun onExecute(sender: Sender, args: Array<String>) {
        sender as Player
        if (args.isNotEmpty()) {
            if (Cooldown.isInCooldown(sender.uuid, "SuggestionCMD")) {
                sender.sendMessage("&7You have to wait &c%cooldown% &7before you can send a new suggestion."
                        .replace("%cooldown%", TimeUtils.getTimeString(sender.language, Cooldown.getTimeLeft(sender.uuid, name).toLong())))
                return
            }
            val suggestion = args.joinToString(" ").trim()
            val suggestionsChannel = discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_SUGGESTION_CHANNEL))
                    ?: return
            val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.SUGGESTION_ALERT))
            val fields: MutableList<MessageEmbed.Field> = LinkedList()
            for (field in jsonEmbedBuilder.fields) {
                val fieldName = field.name
                val fieldValue = field.value
                if (fieldName == null || fieldValue == null) {
                    continue
                }
                val name = insertSuggestionPlaceholders(fieldName, sender, sender.server, suggestion)
                val value = insertSuggestionPlaceholders(fieldValue, sender, sender.server, suggestion)
                val field1 = MessageEmbed.Field(name, value, field.isInline)
                fields.add(field1)
            }
            jsonEmbedBuilder.fields = fields
            sendChannelMessage(suggestionsChannel,
                    jsonEmbedBuilder.build())
            sender.sendMessage(discordBot.messages.getString(MCMessage.SUGGESTION_SUCCESS))
            Cooldown(sender.uuid, "SuggestionCMD", 60).start()
        } else {
            sender.sendMessage(discordBot.messages.getString(MCMessage.SUGGESTION_HELP))
        }
    }

    override fun onTabComplete(sender: Sender, args: Array<String>): List<String> {
        return emptyList()
    }

    private fun insertSuggestionPlaceholders(s: String, player: Player, serverName: String, suggestion: String): String {
        return s.replace("%playername%", player.name)
                .replace("%server%", serverName)
                .replace("%suggestion%", suggestion)
    }

    init {
        this.playerOnly = true
    }
}