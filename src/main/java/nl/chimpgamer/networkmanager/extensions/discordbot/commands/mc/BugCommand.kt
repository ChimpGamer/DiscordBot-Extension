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

class BugCommand(private val discordBot: DiscordBot, cmd: String?) : NMBungeeCommand(discordBot.networkManager, cmd, listOf("networkmanager.bot.bug", "networkmanager.admin")) {
    override fun onExecute(sender: Sender, args: Array<String>) {
        val player = sender as Player
        if (args.isNotEmpty()) {
            if (Cooldown.isInCooldown(player.uuid, "BugCMD")) {
                player.sendMessage("&7You have to wait &c%cooldown% &7before you can send a new bug report."
                        .replace("%cooldown%", TimeUtils.getTimeString(player.language, Cooldown.getTimeLeft(player.uuid, name).toLong())))
                return
            }
            val sb = StringBuilder()
            for (arg in args) {
                sb.append(arg).append(" ")
            }
            val bug = sb.toString().trim { it <= ' ' }
            val bugReportChannel = discordBot.guild.getTextChannelById(Setting.DISCORD_EVENTS_BUGREPORT_CHANNEL.asString)
                    ?: return
            val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.BUGREPORT_ALERT.message)
            val fields: MutableList<MessageEmbed.Field> = LinkedList()
            for (field in jsonEmbedBuilder.fields) {
                val name = insertBugReportPlaceholders(field.name, player, player.server, bug)
                val value = insertBugReportPlaceholders(field.value, player, player.server, bug)
                val field1 = MessageEmbed.Field(name, value, field.isInline)
                fields.add(field1)
            }
            jsonEmbedBuilder.fields = fields
            sendChannelMessage(bugReportChannel,
                    jsonEmbedBuilder.build())
            player.sendMessage(MCMessage.BUG_SUCCESS.message)
            Cooldown(player.uuid, "BugCMD", 60).start()
        } else {
            player.sendMessage(MCMessage.BUG_HELP.message)
        }
    }

    override fun onTabComplete(sender: Sender, args: Array<String>): List<String> {
        return emptyList()
    }

    private fun insertBugReportPlaceholders(s: String?, player: Player, serverName: String, bug: String): String {
        return s!!.replace("%playername%", player.name)
                .replace("%server%", serverName)
                .replace("%bug%", bug)
    }

    init {
        this.isPlayerOnly = true
    }
}