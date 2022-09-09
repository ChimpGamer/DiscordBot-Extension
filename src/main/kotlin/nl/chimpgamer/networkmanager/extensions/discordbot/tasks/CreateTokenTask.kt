package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.generateToken

class CreateTokenTask(
    private val discordBot: DiscordBot,
    private val commandInteractionEvent: GenericCommandInteractionEvent
) : Runnable {

    override fun run() {
        val token = generateToken()
        val msgStr = discordBot.messages.getString(DCMessage.REGISTRATION_TOKEN_RESPONSE)
            .replace("%newline%", "\n")
            .replace("%token%", token)

        if (Utils.isJsonValid(msgStr)) {
            val jsonMessageEmbed = JsonMessageEmbed.fromJson(msgStr)
            commandInteractionEvent.replyEmbeds(jsonMessageEmbed.toMessageEmbed()).setEphemeral(true)
                .queue({ success ->
                    discordBot.discordUserManager.insertToken(token, commandInteractionEvent.user.id,
                    success)
                }) { failure ->
                    discordBot.logger.warning("Failed to send REGISTRATION_TOKEN_RESPONSE!")
                    discordBot.logger.warning(failure.localizedMessage)
                }
        } else {
            commandInteractionEvent.reply(msgStr).setEphemeral(true)
                .queue({ success ->
                    discordBot.discordUserManager.insertToken(token, commandInteractionEvent.user.id,
                        success)
                }) { failure ->
                    discordBot.logger.warning("Failed to send REGISTRATION_TOKEN_RESPONSE!")
                    discordBot.logger.warning(failure.localizedMessage)
                }
        }
    }
}