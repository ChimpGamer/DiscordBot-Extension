package nl.chimpgamer.networkmanager.extensions.discordbot.bungeecord

import com.imaginarycode.minecraft.redisbungee.AbstractRedisBungeeAPI
import nl.chimpgamer.networkmanager.api.extensions.NMExtension
import nl.chimpgamer.networkmanager.extensions.discordbot.bungeecord.listeners.BungeeCordListener
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.Platform

class BungeeCordPlatform : NMExtension(), Platform {
    private val discordBot = DiscordBot(this)

    override fun onEnable() {
        discordBot.enable()
        if (networkManager.isRedisBungee) {
            AbstractRedisBungeeAPI.getAbstractRedisBungeeAPI().registerPubSubChannels("NetworkManagerDiscordBot")
            networkManager.registerListener(BungeeCordListener(discordBot))
        }
    }

    override fun onDisable() {
        discordBot.disable()
    }

    override fun onConfigsReload() {
        discordBot.reloadConfigs()
    }
}