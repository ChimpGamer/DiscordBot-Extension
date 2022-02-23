package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.AbstractConnectionListener

class VelocityJoinLeaveListener(discordBot: DiscordBot): AbstractConnectionListener(discordBot) {

    @Subscribe
    fun onPostLogin(event: PostLoginEvent) {
        onLogin(event.player.uniqueId)
    }
}