package nl.chimpgamer.networkmanager.extensions.discordbot.velocity

import com.imaginarycode.minecraft.redisbungee.AbstractRedisBungeeAPI
import nl.chimpgamer.networkmanager.api.extensions.NMExtension
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.Platform
import nl.chimpgamer.networkmanager.extensions.discordbot.velocity.listeners.VelocityListener

class VelocityPlatform : NMExtension(), Platform {
    private val discordBot = DiscordBot(this)
    override fun onEnable() {
        discordBot.enable()
        if (networkManager.isRedisBungee) {
            AbstractRedisBungeeAPI.getAbstractRedisBungeeAPI().registerPubSubChannels("NetworkManagerDiscordBot")
            networkManager.registerListener(VelocityListener(discordBot))
        }
    }
    override fun onDisable() {
        discordBot.disable()
    }

    override fun onConfigsReload() {
        discordBot.reloadConfigs()
    }
}