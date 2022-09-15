package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee

import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.AbstractConnectionListener

class BungeeCordJoinLeaveListener(discordBot: DiscordBot) : AbstractConnectionListener(discordBot), Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPostLogin(event: ServerConnectEvent) {
        if (event.reason === ServerConnectEvent.Reason.JOIN_PROXY) {
            onLogin(event.player.uniqueId)
        }
    }
}