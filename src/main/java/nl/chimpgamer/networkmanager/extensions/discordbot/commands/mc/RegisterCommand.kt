package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import nl.chimpgamer.networkmanager.api.commands.NMBungeeCommand
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage

class RegisterCommand(private val discordBot: DiscordBot, cmd: String, args: Array<String>) : NMBungeeCommand(discordBot.networkManager, cmd, listOf("networkmanager.bot.verify", "networkmanager.admin"), *args) {
    override fun onExecute(sender: Sender, args: Array<String>) {
        val player = sender as Player
        if (args.isEmpty()) {
            player.sendMessage(MCMessage.VERIFY_HELP.message
                    .replace("%playername%", player.name))
        } else {
            val token = args[0]
            if (token.length != 13) { // Invalid token
                player.sendMessage(MCMessage.VERIFY_INVALID_TOKEN.message
                        .replace("%playername%", player.name))
            } else { // Verify user with token
                discordBot.discordUserManager.verifyUser(player, token)
                networkManager.debug("Verifying " + player.realName + " with token: " + token)
            }
        }
    }

    override fun onTabComplete(sender: Sender, args: Array<String>): List<String> {
        return emptyList()
    }

    init {
        this.isPlayerOnly = true
    }
}