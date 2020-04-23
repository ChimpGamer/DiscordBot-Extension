package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import nl.chimpgamer.networkmanager.common.utils.Methods
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder

class TokenExpiryTask(private val discordBot: DiscordBot, private val token: Token) : Runnable {

    override fun run() {
        if (!this.discordBot.discordUserManager.tokens.contains(this.token)) {
            return
        }
        val msgStr = discordBot.messages.getString(DCMessage.REGISTRATION_TOKEN_EXPIRED)
                .replace("%command_prefix%", discordBot.settings.getString(Setting.DISCORD_COMMAND_PREFIX))
        if (Methods.isJsonValid(msgStr)) {
            val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(msgStr)
            this.token.message.editMessage(jsonEmbedBuilder.build()).complete()
        } else {
            this.token.message.editMessage(msgStr).complete()
        }
        this.discordBot.discordUserManager.tokens.remove(this.token)
        this.discordBot.networkManager.debug("Token: ${this.token.token} has been removed!")
    }
}