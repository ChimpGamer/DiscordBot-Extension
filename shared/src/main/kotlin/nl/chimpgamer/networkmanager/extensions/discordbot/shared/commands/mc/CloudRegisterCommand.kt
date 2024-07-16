package nl.chimpgamer.networkmanager.extensions.discordbot.shared.commands.mc

import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.MCMessage
import org.incendo.cloud.CommandManager
import org.incendo.cloud.parser.standard.StringParser.stringParser

class CloudRegisterCommand(private val discordBot: DiscordBot) {

    fun registerCommands(commandManager: CommandManager<Sender>, name: String, vararg aliases: String) {
        commandManager.command(
            commandManager.commandBuilder(name, *aliases)
                .senderType(Player::class.java)
                .required("token", stringParser())
                .handler { context ->
                    val sender = context.sender()
                    val token = context.get<String>("token")
                    if (token.length != 13) { // Invalid token
                        sender.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_INVALID_TOKEN))
                    } else { // Verify user with token
                        discordBot.discordUserManager.verifyUser(sender, token)
                        discordBot.networkManager.debug("Verifying " + sender.realName + " with token: $token")
                    }
                }
        )
    }
}