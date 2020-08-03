package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting

class ChatListener(private val discordBot: DiscordBot) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: ChatEvent) {
        if (event.sender !is ProxiedPlayer || event.isCancelled || event.isCommand) {
            return
        }
        val proxiedPlayer = event.sender as ProxiedPlayer
        val currentServer = proxiedPlayer.server.info.name
        val chatEventChannels = discordBot.settings.getMap(Setting.DISCORD_EVENTS_CHAT_CHANNELS)
        val globalId = chatEventChannels["all"] ?: "000000000000000000"
        val globalChatTextChannel = discordBot.guild.getTextChannelById(globalId)
        globalChatTextChannel?.sendMessage(discordBot.messages.getString(DCMessage.EVENT_CHAT)
                .replace("%playername%", proxiedPlayer.name)
                .replace("%server%", currentServer)
                .replace("%message%", event.message))?.queue()
        val serverId = chatEventChannels[currentServer] ?: "000000000000000000"
        val serverChatTextChannel = discordBot.guild.getTextChannelById(serverId)
        serverChatTextChannel?.sendMessage(discordBot.messages.getString(DCMessage.EVENT_CHAT)
                .replace("%playername%", proxiedPlayer.name)
                .replace("%server%", currentServer)
                .replace("%message%", event.message
                        .replace(FinderUtil.USER_MENTION.toRegex(), "")
                        .replace(FinderUtil.ROLE_MENTION.toRegex(), "")))?.queue()
    }
}