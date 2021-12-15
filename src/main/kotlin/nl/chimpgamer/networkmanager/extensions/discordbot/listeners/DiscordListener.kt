package nl.chimpgamer.networkmanager.extensions.discordbot.listeners

import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.api.values.Command
import nl.chimpgamer.networkmanager.common.messaging.data.PlayerMessageData
import nl.chimpgamer.networkmanager.common.messaging.handlers.AdminChatMessageHandler
import nl.chimpgamer.networkmanager.common.messaging.handlers.StaffChatMessageHandler
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.GuildJoinCheckTask

class DiscordListener(private val discordBot: DiscordBot) : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val discordUserManager = discordBot.discordUserManager
        val cachedPlayers = discordBot.networkManager.cacheManager.cachedPlayers
        val cachedValues = discordBot.networkManager.cacheManager.cachedValues
        if (!event.isFromType(ChannelType.TEXT) || event.author.isBot) {
            return
        }
        val user = event.author
        val channel = event.textChannel
        val message = event.message
        val msg = message.contentStripped
        if (channel.guild == discordBot.guild) {
            if (channel.id == discordBot.settings.getString(Setting.DISCORD_EVENTS_ADMINCHAT_CHANNEL)) {
                val uuid = discordUserManager.getUuidByDiscordId(user.id) ?: return
                val player = cachedPlayers.getPlayer(uuid) ?: return
                if (cachedValues.getBoolean(Command.ADMINCHAT_ENABLED)) {
                    val alert = discordBot.networkManager.getMessage(player.language, "lang_adminchat_message")
                        .replace("%playername%", player.realName)
                        .replace("%username%", player.userName)
                        .replace("%nickname%", player.nicknameOrUserName)
                        .replace("%server%", "Discord")
                        .replace("%message%", msg)
                    val perm1 = "networkmanager.command.adminchat"
                    val perm2 = "networkmanager.admin"
                    if (discordBot.networkManager.isRedisBungee) {
                        val data = PlayerMessageData(player.uuid, msg)
                        data.permissions.apply {
                            add(perm1)
                            add(perm2)
                        }
                        val handler =
                            discordBot.networkManager.messagingServiceManager.getHandler(AdminChatMessageHandler::class.java)
                        handler?.send(data)
                    } else {
                        discordBot.networkManager.universalUtils.sendMessageToStaff(alert, "all", perm1, perm2)
                    }
                }
            } else if (channel.id == discordBot.settings.getString(Setting.DISCORD_EVENTS_STAFFCHAT_CHANNEL)) {
                val uuid = discordUserManager.getUuidByDiscordId(user.id) ?: return
                val player = cachedPlayers.getPlayer(uuid) ?: return
                if (cachedValues.getBoolean(Command.STAFFCHAT_ENABLED)) {
                    val alert = discordBot.networkManager.getMessage(player.language, "lang_staffchat_message")
                        .replace("%playername%", player.realName)
                        .replace("%username%", player.userName)
                        .replace("%nickname%", player.nicknameOrUserName)
                        .replace("%server%", "Discord")
                        .replace("%message%", msg)
                    val perm1 = "networkmanager.command.staffchat"
                    val perm2 = "networkmanager.admin"
                    if (discordBot.networkManager.isRedisBungee) {
                        val data = PlayerMessageData(player.uuid, msg)
                        data.permissions.apply {
                            add(perm1)
                            add(perm2)
                        }
                        val handler =
                            discordBot.networkManager.messagingServiceManager.getHandler(StaffChatMessageHandler::class.java)
                        handler?.send(data)
                    } else {
                        discordBot.networkManager.universalUtils.sendMessageToStaff(alert, "all", perm1, perm2)
                    }
                }
            }
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val member = event.member
        if (event.guild == discordBot.guild && !member.user.isBot) {
            discordBot.scheduler.runAsync(GuildJoinCheckTask(discordBot, member), false)
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) return
        val member = event.member ?: return
        val chatEventChannels = discordBot.settings.getMap(Setting.DISCORD_EVENTS_CHAT_CHANNELS)
        val chatChannel = chatEventChannels.entries.firstOrNull { it.value == event.channel.id } ?: return
        val serverName = chatChannel.key
        val eventChatMessageFormat = discordBot.messages.getString(MCMessage.EVENT_CHAT)
        if (eventChatMessageFormat.isEmpty()) return
        val cachedPlayers = discordBot.networkManager.cacheManager.cachedPlayers

        val playerUUID = discordBot.discordUserManager.getUuidByDiscordId(member.id) ?: return
        val player = cachedPlayers.getIfLoaded(playerUUID) ?: return

        if (serverName.equals("all", ignoreCase = true)) {
            cachedPlayers.players.values.forEach { target ->
                target.sendMessage(
                    Placeholders.setPlaceholders(player, eventChatMessageFormat
                        .replace("%discordname%", member.effectiveName)
                        .replace("%textchannel%", event.channel.name)
                        .replace("%message%", event.message.contentStripped))
                )
            }
        } else {
            discordBot.networkManager.getPlayersOnServer(serverName).keys.forEach { targetPlayerUUID ->
                val target = cachedPlayers.getIfLoaded(targetPlayerUUID) ?: return@forEach
                target.sendMessage(
                    Placeholders.setPlaceholders(player, eventChatMessageFormat
                        .replace("%discordname%", member.effectiveName)
                        .replace("%textchannel%", event.channel.name)
                        .replace("%message%", event.message.contentStripped)
                    )
                )
            }
        }
    }
}