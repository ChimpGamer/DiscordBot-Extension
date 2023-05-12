package nl.chimpgamer.networkmanager.extensions.discordbot.listeners

import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import nl.chimpgamer.networkmanager.api.utils.adventure.parse
import nl.chimpgamer.networkmanager.api.values.Command
import nl.chimpgamer.networkmanager.api.values.Message
import nl.chimpgamer.networkmanager.common.messaging.data.PlayerMessageData
import nl.chimpgamer.networkmanager.common.messaging.handlers.AdminChatMessageHandler
import nl.chimpgamer.networkmanager.common.messaging.handlers.StaffChatMessageHandler
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.GuildJoinCheckTask

class DiscordListener(private val discordBot: DiscordBot) : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromType(ChannelType.TEXT) || event.author.isBot) return

        val discordUserManager = discordBot.discordUserManager
        val cachedPlayers = discordBot.networkManager.cacheManager.cachedPlayers
        val cachedValues = discordBot.networkManager.cacheManager.cachedValues
        val user = event.author
        val channel = event.channel.asTextChannel()
        val message = event.message.contentStripped
        if (channel.guild == discordBot.guild) {

            if (channel.id == discordBot.settings.getString(Setting.DISCORD_EVENTS_ADMINCHAT_CHANNEL)) {
                val uuid = discordUserManager.getUuidByDiscordId(user.id) ?: return
                val player = cachedPlayers.getPlayer(uuid) ?: return
                if (cachedValues.getBoolean(Command.ADMINCHAT_ENABLED)) {
                    val perm1 = "networkmanager.command.adminchat"
                    val perm2 = "networkmanager.admin"
                    if (discordBot.networkManager.isRedisBungee) {
                        val data = PlayerMessageData(player.uuid, message)
                        data.permissions.apply {
                            add(perm1)
                            add(perm2)
                        }
                        val handler =
                            discordBot.networkManager.messagingServiceManager.getHandler(AdminChatMessageHandler::class.java)
                        handler?.send(data)
                    } else {
                        val replacements = mapOf(
                            "playername" to player.name,
                            "displayname" to player.name,
                            "server" to "Discord",
                            "message" to message
                        )

                        discordBot.networkManager.universalUtils.sendTranslatableMessageToStaff(
                            Message.ADMINCHAT_MESSAGE,
                            perm1,
                            perm2,
                            replacements = replacements
                        )
                    }
                }
            } else if (channel.id == discordBot.settings.getString(Setting.DISCORD_EVENTS_STAFFCHAT_CHANNEL)) {
                val uuid = discordUserManager.getUuidByDiscordId(user.id) ?: return
                val player = cachedPlayers.getPlayer(uuid) ?: return
                if (cachedValues.getBoolean(Command.STAFFCHAT_ENABLED)) {
                    val perm1 = "networkmanager.command.staffchat"
                    val perm2 = "networkmanager.admin"
                    if (discordBot.networkManager.isRedisBungee) {
                        val data = PlayerMessageData(player.uuid, message)
                        data.permissions.apply {
                            add(perm1)
                            add(perm2)
                        }
                        val handler =
                            discordBot.networkManager.messagingServiceManager.getHandler(StaffChatMessageHandler::class.java)
                        handler?.send(data)
                    } else {
                        val replacements = mapOf(
                            "playername" to player.name,
                            "displayname" to player.name,
                            "server" to "Discord",
                            "message" to message
                        )

                        discordBot.networkManager.universalUtils.sendTranslatableMessageToStaff(
                            Message.STAFFCHAT_MESSAGE,
                            perm1,
                            perm2,
                            replacements = replacements
                        )
                    }
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

                val chatMessageComponent = eventChatMessageFormat.parse(player, mapOf(
                    "mention" to member.asMention,
                    "discordname" to member.effectiveName,
                    "textchannel" to channel.name,
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
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val member = event.member
        if (event.guild == discordBot.guild && !member.user.isBot) {
            discordBot.scheduler.runAsync(GuildJoinCheckTask(discordBot, member), false)
        }
    }

    override fun onGuildMemberUpdatePending(event: GuildMemberUpdatePendingEvent) {
        val member = event.member
        if (event.guild == discordBot.guild && !member.user.isBot) {
            if (!event.oldPending && event.newPending) {
                val message = discordBot.messages.getString(DCMessage.EVENT_AGREED_MEMBERSHIP_SCREENING_REQUIREMENTS)
                if (message.isEmpty()) {
                    return
                }
                member.user.openPrivateChannel().queue { it.sendMessage(message.replace("%mention%", member.user.asTag)).queue() }
            }
        }
    }
}