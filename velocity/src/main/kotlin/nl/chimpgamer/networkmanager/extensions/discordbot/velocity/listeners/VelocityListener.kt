package nl.chimpgamer.networkmanager.extensions.discordbot.velocity.listeners

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent
import com.velocitypowered.api.event.Subscribe
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import java.util.UUID

class VelocityListener(private val discordBot: DiscordBot) {

    @Subscribe
    fun onPubSubMessage(event: PubSubMessageEvent) {
        if (event.channel != "NetworkManagerDiscordBot") return
        val message = event.message
        val split = message.split(" ").toTypedArray()
        if (split[0].equals("load", ignoreCase = true)) {
            val uuid = UUID.fromString(split[1])
            discordBot.discordUserManager.load(uuid)
        }
    }
}