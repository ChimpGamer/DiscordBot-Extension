package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import com.imaginarycode.minecraft.redisbungee.RedisBungee
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

object RedisBungeeUtils {

    fun sendRedisBungeeMessage(discordBot: DiscordBot, message: String) {
        discordBot.scheduler.runAsync({
            RedisBungee.getApi().sendChannelMessage("NetworkManagerBot", message)
        }, false)
    }
}