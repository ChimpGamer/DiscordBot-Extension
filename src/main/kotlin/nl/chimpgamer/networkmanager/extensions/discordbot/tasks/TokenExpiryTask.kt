package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils

class TokenExpiryTask(private val discordBot: DiscordBot, private val token: Token) : Runnable {

    override fun run() {
        if (!this.discordBot.discordUserManager.tokens.contains(this.token)) {
            return
        }
        val msgStr = discordBot.messages.getString(DCMessage.REGISTRATION_TOKEN_EXPIRED)
                .replace("%command_prefix%", discordBot.settings.getString(Setting.DISCORD_COMMAND_PREFIX))
        if (Utils.isJsonValid(msgStr)) {
            val jsonMessageEmbed = JsonMessageEmbed.fromJson(msgStr)
            this.token.message.editMessageEmbeds(jsonMessageEmbed.toMessageEmbed()).complete()
        } else {
            this.token.message.editMessage(msgStr).complete()
        }
        this.discordBot.discordUserManager.tokens.remove(this.token)
        this.discordBot.networkManager.debug("Token: ${this.token.token} has been removed!")
    }
}