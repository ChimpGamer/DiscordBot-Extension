package nl.chimpgamer.networkmanager.extensions.discordbot.listeners

import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import nl.chimpgamer.networkmanager.api.cache.modules.CachedPlayers
import nl.chimpgamer.networkmanager.api.cache.modules.CachedValues
import nl.chimpgamer.networkmanager.api.values.Command
import nl.chimpgamer.networkmanager.common.messaging.data.PlayerMessageData
import nl.chimpgamer.networkmanager.common.messaging.handlers.AdminChatMessageHandler
import nl.chimpgamer.networkmanager.common.messaging.handlers.StaffChatMessageHandler
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.GuildJoinCheckTask

class DiscordListener(private val discordBot: DiscordBot) : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val discordUserManager = discordBot.discordUserManager
        val cachedPlayers: CachedPlayers = discordBot.networkManager.cacheManager.cachedPlayers
        val cachedValues: CachedValues = discordBot.networkManager.cacheManager.cachedValues
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
                val player = cachedPlayers.getPlayer(uuid)
                if (cachedValues.getBoolean(Command.ADMINCHAT_ENABLED)) {
                    val alert = discordBot.networkManager.getMessage(player.language, "lang_adminchat_message")
                            .replace("%playername%", player.realName)
                            .replace("%username%", player.userName)
                            .replace("%nickname%", player.nicknameOrUserName)
                            .replace("%server%", "Discord")
                            .replace("%message%", msg)
                    val perm1 = "networkmanager.adminchat"
                    val perm2 = "networkmanager.admin"
                    if (discordBot.networkManager.isRedisBungee) {
                        val data = PlayerMessageData(player.uuid, msg)
                        data.permissions.addAll(listOf(perm1, perm2))
                        val handler = discordBot.networkManager.messagingServiceManager.getHandler(AdminChatMessageHandler::class.java)
                        handler.send(data)
                    } else {
                        discordBot.networkManager.sendMessageToStaff(alert, "all", perm1, perm2)
                    }
                }
            } else if (channel.id == discordBot.settings.getString(Setting.DISCORD_EVENTS_STAFFCHAT_CHANNEL)) {
                val uuid = discordUserManager.getUuidByDiscordId(user.id) ?: return
                val player = cachedPlayers.getPlayer(uuid)
                if (cachedValues.getBoolean(Command.STAFFCHAT_ENABLED)) {
                    val alert = discordBot.networkManager.getMessage(player.language, "lang_staffchat_message")
                            .replace("%playername%", player.realName)
                            .replace("%username%", player.userName)
                            .replace("%nickname%", player.nicknameOrUserName)
                            .replace("%server%", "Discord")
                            .replace("%message%", msg)
                    val perm1 = "networkmanager.staffchat"
                    val perm2 = "networkmanager.admin"
                    if (discordBot.networkManager.isRedisBungee) {
                        val data = PlayerMessageData(player.uuid, msg)
                        data.permissions.addAll(listOf(perm1, perm2))
                        val handler = discordBot.networkManager.messagingServiceManager.getHandler(StaffChatMessageHandler::class.java)
                        handler.send(data)
                    } else {
                        discordBot.networkManager.sendMessageToStaff(alert, "all", perm1, perm2)
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
}