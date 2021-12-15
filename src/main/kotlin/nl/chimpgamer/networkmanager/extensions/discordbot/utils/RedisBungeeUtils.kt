package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import nl.chimpgamer.networkmanager.bungeecord.NetworkManager
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

object RedisBungeeUtils {

    fun sendRedisBungeeMessage(discordBot: DiscordBot, message: String) {
        discordBot.scheduler.runAsync({
            (discordBot.networkManager as NetworkManager).redisBungee.sendChannelMessage("NetworkManagerBot", message)
        }, false)
    }
}