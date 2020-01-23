package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import nl.chimpgamer.networkmanager.common.utils.Methods
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.generateToken

class CreateTokenTask(private val discordBot: DiscordBot, private val channel: MessageChannel, private val discordID: String) : Runnable {

    override fun run() {
        val token = generateToken()
        val msgStr = DCMessage.REGISTRATION_TOKEN.message
                .replace("%newline%", "\n")
                .replace("%token%", token)
        val message: Message? = if (Methods.isJsonValid(msgStr)) {
            val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(msgStr)
            Utils.sendMessageComplete(this.channel, jsonEmbedBuilder.build())
        } else {
            Utils.sendMessageComplete(this.channel, msgStr)
        }
        this.discordBot.discordUserManager.insertToken(token, this.discordID, message)
    }
}