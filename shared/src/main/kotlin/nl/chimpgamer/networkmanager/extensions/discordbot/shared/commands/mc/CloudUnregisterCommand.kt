package nl.chimpgamer.networkmanager.extensions.discordbot.shared.commands.mc

import cloud.commandframework.Command
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot

class CloudUnregisterCommand(private val discordBot: DiscordBot) {

    fun getCommand(name: String, vararg aliases: String): Command.Builder<Sender> {
        return discordBot.networkManager.cloudCommandManager.commandManager.commandBuilder(name, *aliases)
            .senderType(Player::class.java)
            .handler { context ->
                val sender = context.sender as Player
                discordBot.discordUserManager.deleteUser(sender)
            }
    }
}