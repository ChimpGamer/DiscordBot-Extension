package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.api.utils.commands.NMBungeeCommand
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

class UnregisterCommand(private val discordBot: DiscordBot, cmd: String, args: List<String>) : NMBungeeCommand(discordBot.networkManager, cmd, listOf("networkmanager.bot.unregister", "networkmanager.admin"), args) {
    override fun onExecute(sender: Sender, args: Array<String>) {
        sender as Player
        discordBot.discordUserManager.deleteUser(sender)
    }

    init {
        this.playerOnly = true
    }
}