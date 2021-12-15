package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import cloud.commandframework.Command
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage

class CloudDiscordCommand(private val discordBot: DiscordBot) {

    fun getCommand(name: String, vararg aliases: String): Command.Builder<Sender> {
        return discordBot.networkManager.cloudCommandManager.commandManager.commandBuilder(name, *aliases)
            .senderType(Player::class.java)
            .handler { context ->
                context.sender.sendMessage(discordBot.messages.getString(MCMessage.DISCORD_RESPONSE))
            }
    }
}