package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import nl.chimpgamer.networkmanager.api.commands.NMBungeeCommand
import nl.chimpgamer.networkmanager.api.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage

class NetworkManagerBotCommand(private val discordBot: DiscordBot, cmd: String?) : NMBungeeCommand(discordBot.networkManager, cmd, listOf("networkmanager.admin"), "nmbot") {
    override fun onExecute(sender: Sender, args: Array<String>) {
        if (args.size == 2) {
            if (args[0].equals("reload", ignoreCase = true)) {
                when (args[1].toLowerCase()) {
                    "config", "settings" -> {
                        discordBot.settings.reload()
                        sender.sendMessage(discordBot.messages.getString(MCMessage.RELOAD_CONFIG))
                    }
                    "messages" -> {
                        discordBot.messages.reload()
                        sender.sendMessage(discordBot.messages.getString(MCMessage.RELOAD_MESSAGES))
                    }
                    "jda" -> {
                        val success = discordBot.discordManager.restartJDA()
                        if (success) {
                            sender.sendMessage(discordBot.messages.getString(MCMessage.RELOAD_JDA_SUCCESS))
                        } else {
                            sender.sendMessage(discordBot.messages.getString(MCMessage.RELOAD_JDA_FAILED))
                        }
                    }
                }
            }
        }
    }

    override fun onTabComplete(sender: Sender, args: Array<String>): List<String> {
        if (args.size == 1 && "reload".startsWith(args[0].toLowerCase())) {
            return listOf("reload")
        }
        return if (args.size == 2) {
            listOf("config", "settings", "messages", "jda")
        } else emptyList()
    }

}