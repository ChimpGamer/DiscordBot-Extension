package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils

class TokenExpiryTask(private val discordBot: DiscordBot, private val token: Token) : Runnable {

    override fun run() {
        if (!this.discordBot.discordUserManager.tokens.contains(this.token)) return


        val msgStr = discordBot.messages.getString(DCMessage.REGISTRATION_TOKEN_EXPIRED)

        if (Utils.isJsonValid(msgStr)) {
            val jsonMessageEmbed = JsonMessageEmbed.fromJson(msgStr)
            token.interaction.editOriginalEmbeds(jsonMessageEmbed.toMessageEmbed()).queue {
                this.discordBot.discordUserManager.tokens.remove(this.token)
                this.discordBot.networkManager.debug("Token: ${this.token.token} has been removed!")
            }
        } else {
            token.interaction.editOriginal(msgStr).queue {
                this.discordBot.discordUserManager.tokens.remove(this.token)
                this.discordBot.networkManager.debug("Token: ${this.token.token} has been removed!")
            }
        }
    }
}