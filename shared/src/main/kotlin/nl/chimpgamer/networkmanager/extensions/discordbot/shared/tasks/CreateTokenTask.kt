package nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils

class CreateTokenTask(
    private val discordBot: DiscordBot,
    private val commandInteractionEvent: GenericCommandInteractionEvent
) : Runnable {

    override fun run() {
        val token = Utils.generateToken()
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
                    discordBot.platform.warn("Failed to send REGISTRATION_TOKEN_RESPONSE!")
                    discordBot.platform.warn(failure.localizedMessage)
                }
        } else {
            commandInteractionEvent.reply(msgStr).setEphemeral(true)
                .queue({ success ->
                    discordBot.discordUserManager.insertToken(token, commandInteractionEvent.user.id,
                        success)
                }) { failure ->
                    discordBot.platform.warn("Failed to send REGISTRATION_TOKEN_RESPONSE!")
                    discordBot.platform.warn(failure.localizedMessage)
                }
        }
    }
}