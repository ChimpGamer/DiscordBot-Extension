package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.api.utils.commands.NMBungeeCommand
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage

class DiscordCommand(private val discordBot: DiscordBot, name: String) : NMBungeeCommand(discordBot.networkManager, name, null) {
    override fun onExecute(sender: Sender, args: Array<String>) {
        sender.sendMessage(discordBot.messages.getString(MCMessage.DISCORD_RESPONSE))
    }

    init {
        this.playerOnly = true
    }
}