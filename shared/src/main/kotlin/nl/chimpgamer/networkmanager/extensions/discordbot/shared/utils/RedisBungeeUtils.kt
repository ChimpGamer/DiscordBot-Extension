package nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils

import com.imaginarycode.minecraft.redisbungee.AbstractRedisBungeeAPI
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot

object RedisBungeeUtils {

    fun sendRedisBungeeMessage(discordBot: DiscordBot, message: String) {
        discordBot.scheduler.runAsync({
            AbstractRedisBungeeAPI.getAbstractRedisBungeeAPI().sendChannelMessage("NetworkManagerDiscordBot", message)
        }, false)
    }
}