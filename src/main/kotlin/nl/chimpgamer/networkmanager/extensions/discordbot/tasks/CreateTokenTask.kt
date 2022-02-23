package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.generateToken

class CreateTokenTask(private val discordBot: DiscordBot, private val channel: MessageChannel, private val discordID: String) : Runnable {

    override fun run() {
        val token = generateToken()
        val msgStr = discordBot.messages.getString(DCMessage.REGISTRATION_TOKEN_RESPONSE)
                .replace("%newline%", "\n")
                .replace("%token%", token)
        val message: Message? = if (Utils.isJsonValid(msgStr)) {
            val jsonMessageEmbed = JsonMessageEmbed.fromJson(msgStr)
            Utils.sendMessageComplete(this.channel, jsonMessageEmbed.toMessageEmbed())
        } else {
            Utils.sendMessageComplete(this.channel, msgStr)
        }
        if (message == null) {
            discordBot.logger.warning("Failed to send REGISTRATION_TOKEN_RESPONSE!")
            return
        }
        this.discordBot.discordUserManager.insertToken(token, this.discordID, message)
    }
}