package nl.chimpgamer.networkmanager.extensions.discordbot.shared.commands.mc

import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.MCMessage
import org.incendo.cloud.CommandManager

class CloudDiscordCommand(private val discordBot: DiscordBot) {

    fun registerCommands(commandManager: CommandManager<Sender>, name: String, vararg aliases: String) {
        commandManager.command(
            commandManager.commandBuilder(name, *aliases)
                .senderType(Player::class.java)
                .handler { context ->
                    context.sender().sendRichMessage(discordBot.messages.getString(MCMessage.DISCORD_RESPONSE))
                }
        )
    }
}