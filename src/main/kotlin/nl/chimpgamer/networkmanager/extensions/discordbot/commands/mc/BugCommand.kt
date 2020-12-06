package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import net.dv8tion.jda.api.entities.MessageEmbed
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.api.utils.Cooldown
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.api.utils.commands.NMBungeeCommand
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage
import java.util.*

class BugCommand(private val discordBot: DiscordBot, cmd: String) : NMBungeeCommand(discordBot.networkManager, cmd, listOf("networkmanager.bot.bug", "networkmanager.admin")) {
    override fun onExecute(sender: Sender, args: Array<String>) {
        sender as Player
        if (args.isNotEmpty()) {
            if (Cooldown.isInCooldown(sender.uuid, "BugCMD")) {
                sender.sendMessage("&7You have to wait &c%cooldown% &7before you can send a new bug report."
                        .replace("%cooldown%", TimeUtils.getTimeString(sender.language, Cooldown.getTimeLeft(sender.uuid, name).toLong())))
                return
            }
            val bug = args.joinToString(" ").trim()
            val bugReportChannel = discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_BUGREPORT_CHANNEL))
                    ?: return
            val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.BUGREPORT_ALERT))
            val fields: MutableList<MessageEmbed.Field> = LinkedList()
            for (field in jsonEmbedBuilder.fields) {
                val fieldName = field.name
                val fieldValue = field.value
                if (fieldName == null || fieldValue == null) {
                    continue
                }
                val name = insertBugReportPlaceholders(fieldName, sender, sender.server!!, bug)
                val value = insertBugReportPlaceholders(fieldValue, sender, sender.server!!, bug)
                val field1 = MessageEmbed.Field(name, value, field.isInline)
                fields.add(field1)
            }
            jsonEmbedBuilder.fields = fields
            sendChannelMessage(bugReportChannel,
                    jsonEmbedBuilder.build())
            sender.sendMessage(discordBot.messages.getString(MCMessage.BUG_SUCCESS))
            Cooldown(sender.uuid, "BugCMD", 60).start()
        } else {
            sender.sendMessage(discordBot.messages.getString(MCMessage.BUG_HELP))
        }
    }

    private fun insertBugReportPlaceholders(s: String, player: Player, serverName: String, bug: String): String {
        return s.replace("%playername%", player.name)
                .replace("%server%", serverName)
                .replace("%bug%", bug)
    }

    init {
        this.playerOnly = true
    }
}