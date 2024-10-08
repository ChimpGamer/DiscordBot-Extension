package nl.chimpgamer.networkmanager.extensions.discordbot.bungeecord.listeners

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import java.util.UUID

class BungeeCordListener(private val discordBot: DiscordBot) : Listener {

    @EventHandler
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