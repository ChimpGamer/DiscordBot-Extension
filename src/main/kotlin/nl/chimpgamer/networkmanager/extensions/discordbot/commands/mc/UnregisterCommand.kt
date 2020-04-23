package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import nl.chimpgamer.networkmanager.api.commands.NMBungeeCommand
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

class UnregisterCommand(private val discordBot: DiscordBot, cmd: String, args: Array<String>) : NMBungeeCommand(discordBot.networkManager, cmd, listOf("networkmanager.bot.unregister", "networkmanager.admin"), *args) {
    override fun onExecute(sender: Sender, args: Array<String>) {
        val player = sender as Player
        discordBot.discordUserManager.deleteUser(player)
    }

    override fun onTabComplete(sender: Sender, args: Array<String>): List<String> {
        return emptyList()
    }

    init {
        this.playerOnly = true
    }
}