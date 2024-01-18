package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.redisbungee

import com.imaginarycode.minecraft.redisbungee.AbstractRedisBungeeAPI
import nl.chimpgamer.networkmanager.api.utils.PlatformType
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

class RedisBungeeHandler(discordBot: DiscordBot) {
    init {
        AbstractRedisBungeeAPI.getAbstractRedisBungeeAPI().registerPubSubChannels("NetworkManagerDiscordBot")
        val platformType = discordBot.networkManager.platformType
        if (platformType === PlatformType.BUNGEECORD) {
            discordBot.networkManager.registerListeners(BungeeCordListener(discordBot))
        } else if (platformType === PlatformType.VELOCITY) {
            discordBot.networkManager.registerListener(VelocityListener(discordBot))
        }
    }
}