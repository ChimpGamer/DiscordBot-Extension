package nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import nl.chimpgamer.networkmanager.api.utils.adventure.parse
import nl.chimpgamer.networkmanager.api.values.Command
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks.GuildJoinCheckTask

class DiscordListener(private val discordBot: DiscordBot) : CoroutineEventListener {

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is MessageReceivedEvent -> onMessageReceived(event)
            is GuildMemberJoinEvent -> onGuildMemberJoin(event)
            is GuildMemberUpdatePendingEvent -> onGuildMemberUpdatePending(event)
        }
    }

    private fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromType(ChannelType.TEXT) || event.author.isBot) return
        if (!event.isFromGuild) return

        val discordUserManager = discordBot.discordUserManager
        val cachedPlayers = discordBot.networkManager.cacheManager.cachedPlayers
        val cachedValues = discordBot.networkManager.cacheManager.cachedValues
        val user = event.author
        val channel = event.channel.asTextChannel()
        val message = event.message.contentStripped

        if (channel.id == discordBot.settings.getString(Setting.DISCORD_EVENTS_ADMINCHAT_CHANNEL)) {
            val uuid = discordUserManager.getUuidByDiscordId(user.id) ?: return
            val player = cachedPlayers.getPlayer(uuid) ?: return
            if (cachedValues.getBoolean(Command.ADMINCHAT_ENABLED)) {
                discordBot.networkManager.chatManager.sendAdminChatMessage(player, message)
            }
        } else if (channel.id == discordBot.settings.getString(Setting.DISCORD_EVENTS_STAFFCHAT_CHANNEL)) {
            val uuid = discordUserManager.getUuidByDiscordId(user.id) ?: return
            val player = cachedPlayers.getPlayer(uuid) ?: return
            if (cachedValues.getBoolean(Command.STAFFCHAT_ENABLED)) {
                discordBot.networkManager.chatManager.sendStaffChatMessage(player, message)
            }
        } else {
            val chatEventChannels = discordBot.settings.getMap(Setting.DISCORD_EVENTS_CHAT_CHANNELS)
            val chatChannel = chatEventChannels.entries.firstOrNull { it.value == channel.id } ?: return
            val serverName = chatChannel.key
            val eventChatMessageFormat = discordBot.messages.getString(MCMessage.EVENT_CHAT)
            if (eventChatMessageFormat.isEmpty()) return
            val member = event.member ?: return

            val playerUUID = discordBot.discordUserManager.getUuidByDiscordId(member.id) ?: return
            val player = cachedPlayers.getIfLoaded(playerUUID) ?: return

            val role = member.roles.find { it.color == member.color }
            val chatMessageComponent = eventChatMessageFormat.parse(player, mapOf(
                "mention" to member.asMention,
                "discordname" to member.effectiveName,
                "textchannel" to channel.name,
                "discord_member_role" to role?.name,
                "message" to message
            ))

            if (serverName.equals("all", ignoreCase = true)) {
                cachedPlayers.players.values.forEach { target ->
                    target.sendMessage(chatMessageComponent)
                }
            } else {
                discordBot.networkManager.getPlayersOnServer(serverName).keys.forEach { targetPlayerUUID ->
                    val target = cachedPlayers.getIfLoaded(targetPlayerUUID) ?: return@forEach
                    target.sendMessage(chatMessageComponent)
                }
            }
        }
    }

    private fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val member = event.member
        if (member.user.isBot) return
        if (event.guild == discordBot.guild) {
            discordBot.scheduler.runAsync(GuildJoinCheckTask(discordBot, member), false)
        }
    }

    private suspend fun onGuildMemberUpdatePending(event: GuildMemberUpdatePendingEvent) {
        val member = event.member
        if (member.user.isBot) return
        if (event.guild == discordBot.guild) {
            if (!event.oldPending && event.newPending) {
                val message = discordBot.messages.getString(DCMessage.EVENT_AGREED_MEMBERSHIP_SCREENING_REQUIREMENTS)
                if (message.isEmpty()) {
                    return
                }
                member.user.openPrivateChannel().flatMap { it.sendMessage(message.replace("%mention%", member.user.asMention)) }.await()
            }
        }
    }
}