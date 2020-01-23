package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import nl.chimpgamer.networkmanager.common.utils.Methods
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils

class TokenExpiryTask(private val discordBot: DiscordBot, private val token: Token) : Runnable {

    override fun run() {
        if (!this.discordBot.discordUserManager.tokens.contains(this.token)) {
            return
        }
        val msgStr = DCMessage.REGISTRATION_TOKEN_EXPIRED.message
                .replace("%command_prefix%", Setting.DISCORD_COMMAND_PREFIX.asString)
        if (Methods.isJsonValid(msgStr)) {
            val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(msgStr)
            Utils.editMessage(this.token.getMessage(), jsonEmbedBuilder.build())
        } else {
            Utils.editMessage(this.token.getMessage(), msgStr)
        }
        this.discordBot.discordUserManager.tokens.remove(this.token)
        this.discordBot.networkManager.debug("Token: " + this.token.token.toString() + " has been removed!")
    }
}