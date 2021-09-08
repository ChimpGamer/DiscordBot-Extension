package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.api.utils.commands.NMBungeeCommand
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage

class RegisterCommand(private val discordBot: DiscordBot, cmd: String, aliases: List<String>) : NMBungeeCommand(discordBot.networkManager, cmd, listOf("networkmanager.bot.register", "networkmanager.admin"), aliases) {
    override fun onExecute(sender: Sender, args: Array<String>) {
        sender as Player
        if (args.isEmpty()) {
            sender.sendMessage(discordBot.messages.getString(MCMessage.REGISTER_HELP)
                    .replace("%playername%", sender.name))
        } else {
            val token = args[0]
            if (token.length != 13) { // Invalid token
                sender.sendMessage(discordBot.messages.getString(MCMessage.REGISTER_INVALID_TOKEN)
                        .replace("%playername%", sender.name))
            } else { // Verify user with token
                discordBot.discordUserManager.verifyUser(sender, token)
                networkManager.debug("Verifying " + sender.realName + " with token: $token")
            }
        }
    }

    init {
        this.playerOnly = true
    }
}