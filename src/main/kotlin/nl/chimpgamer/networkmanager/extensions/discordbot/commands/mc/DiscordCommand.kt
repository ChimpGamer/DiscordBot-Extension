package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import nl.chimpgamer.networkmanager.api.commands.NMBungeeCommand
import nl.chimpgamer.networkmanager.api.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage

class DiscordCommand(private val discordBot: DiscordBot, name: String) : NMBungeeCommand(discordBot.networkManager, name, null) {
    override fun onExecute(sender: Sender, strings: Array<String>) {
        sender.sendMessage(discordBot.messages.getString(MCMessage.DISCORD_RESPONSE))
    }

    override fun onTabComplete(sender: Sender, strings: Array<String>): List<String> {
        return emptyList()
    }

    init {
        this.playerOnly = true
    }
}