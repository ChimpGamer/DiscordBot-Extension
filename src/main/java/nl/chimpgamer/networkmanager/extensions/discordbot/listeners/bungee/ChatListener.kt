package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage

class ChatListener(private val discordBot: DiscordBot) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: ChatEvent) {
        if (event.sender !is ProxiedPlayer || event.isCancelled || event.isCommand) {
            return
        }
        val proxiedPlayer = event.sender as ProxiedPlayer
        val currentServer = proxiedPlayer.server.info.name
        val chatEventChannels = Setting.DISCORD_EVENTS_CHAT_CHANNELS.asMap
        var globalId: String? = "000000000000000000"
        if (chatEventChannels.containsKey("all")) {
            globalId = chatEventChannels["all"]
        }
        val globalChatTextChannel = discordBot.guild.getTextChannelById(globalId!!)
        globalChatTextChannel?.sendMessage(DCMessage.CHAT_EVENT_FORMAT.message
                .replace("%playername%", proxiedPlayer.name)
                .replace("%server%", currentServer)
                .replace("%message%", event.message))?.queue()
        var serverId: String? = "000000000000000000"
        if (chatEventChannels.containsKey(currentServer)) {
            serverId = chatEventChannels[currentServer]
        }
        val serverChatTextChannel = discordBot.guild.getTextChannelById(serverId!!)
        serverChatTextChannel?.sendMessage(DCMessage.CHAT_EVENT_FORMAT.message
                .replace("%playername%", proxiedPlayer.name)
                .replace("%server%", currentServer)
                .replace("%message%", event.message
                        .replace(FinderUtil.USER_MENTION.toRegex(), "")
                        .replace(FinderUtil.ROLE_MENTION.toRegex(), "")))?.queue()
    }
}