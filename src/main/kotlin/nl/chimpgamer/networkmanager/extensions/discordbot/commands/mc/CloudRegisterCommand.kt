package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import cloud.commandframework.Command
import cloud.commandframework.arguments.standard.StringArgument
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage

class CloudRegisterCommand(private val discordBot: DiscordBot) {

    fun getCommand(name: String, vararg aliases: String): Command.Builder<Sender> {
        val tokenArgument = StringArgument.of<Sender>("token")
        return discordBot.networkManager.cloudCommandManager.commandManager.commandBuilder(name, *aliases)
            .senderType(Player::class.java)
            .argument(tokenArgument)
            .handler { context ->
                val sender = context.sender as Player
                val token = context[tokenArgument]
                if (token.length != 13) { // Invalid token
                    sender.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_INVALID_TOKEN))
                } else { // Verify user with token
                    discordBot.discordUserManager.verifyUser(sender, token)
                    discordBot.networkManager.debug("Verifying " + sender.realName + " with token: $token")
                }
            }
    }
}