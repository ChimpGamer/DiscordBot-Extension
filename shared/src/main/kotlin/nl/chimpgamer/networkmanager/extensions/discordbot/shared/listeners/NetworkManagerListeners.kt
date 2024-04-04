package nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.data.DataObject
import nl.chimpgamer.networkmanager.api.event.PostOrders
import nl.chimpgamer.networkmanager.api.event.events.*
import nl.chimpgamer.networkmanager.api.event.events.player.AsyncPlayerLoginEvent
import nl.chimpgamer.networkmanager.api.event.events.player.PlayerDisconnectEvent
import nl.chimpgamer.networkmanager.api.event.events.player.ServerConnectedEvent
import nl.chimpgamer.networkmanager.api.event.events.ticket.TicketCreateEvent
import nl.chimpgamer.networkmanager.api.models.punishments.Punishment
import nl.chimpgamer.networkmanager.api.models.servers.Server
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.api.utils.adventure.toLegacy
import nl.chimpgamer.networkmanager.api.utils.stripColors
import nl.chimpgamer.networkmanager.api.values.Message
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks.SyncRanksTask
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils.sendChannelMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.parsePlaceholdersToFields
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class NetworkManagerListeners(private val discordBot: DiscordBot) {

    private fun onStaffChat(event: StaffChatEvent) {
        if (event.isCancelled) return
        val staffChatChannel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_STAFFCHAT_CHANNEL))
                ?: return
        sendChannelMessage(
            staffChatChannel,
            discordBot.messages.getString(DCMessage.EVENT_STAFFCHAT)
                .replace("%playername%", event.sender.name)
                .replace("%displayname%", event.sender.displayName.toLegacy().stripColors())
                .replace("%server%", event.sender.server!!)
                .replace("%message%", event.message)
        )
    }

    private fun onAdminChat(event: AdminChatEvent) {
        if (event.isCancelled) return
        val adminChatChannel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_ADMINCHAT_CHANNEL))
                ?: return
        sendChannelMessage(
            adminChatChannel,
            discordBot.messages.getString(DCMessage.EVENT_ADMINCHAT)
                .replace("%playername%", event.sender.name)
                .replace("%displayname%", event.sender.displayName.toLegacy().stripColors())
                .replace("%server%", event.sender.server!!)
                .replace("%message%", event.message)
        )
    }

    private fun onServerStatusChange(event: ServerStatusChangeEvent) {
        val server = event.server
        val serverChannels = discordBot.settings.getMap(Setting.DISCORD_EVENTS_SERVERSTATUS_CHANNELS)
        val globalId = serverChannels["all"] ?: "000000000000000000"
        val channelId = serverChannels[server.serverName] ?: "000000000000000000"
        val textChannel =
            discordBot.guild.getTextChannelById(globalId) ?: discordBot.guild.getTextChannelById(channelId)
        if (textChannel != null) {
            val dcMessage = if (event.isOnline) DCMessage.SERVER_STATUS_ONLINE else DCMessage.SERVER_STATUS_OFFLINE

            val data = DataObject.fromJson(discordBot.messages.getString(dcMessage))
            val embedBuilder = EmbedBuilder.fromData(data).apply {
                val title = insertServerPlaceholders(data.getString("title", null), server)
                val description = insertServerPlaceholders(data.getString("description", "").takeIf { it.isNotEmpty() }, server)
                setTitle(title)
                setDescription(description)
                parsePlaceholdersToFields { text -> insertServerPlaceholders(text, server) }
            }

            sendChannelMessage(textChannel, embedBuilder.build())
        }
    }

    private fun onPunishment(event: PunishmentEvent) {
        if (event.punishment.type === Punishment.Type.NOTE) {
            return
        }
        if (event.punishment.type === Punishment.Type.REPORT) {
            val reportChannel =
                discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_REPORT_CHANNEL))
                    ?: return
            if (event.punishment.isActive) {
                val data = DataObject.fromJson(discordBot.messages.getString(DCMessage.REPORT_ALERT))
                val embedBuilder = EmbedBuilder.fromData(data).apply {
                    val title = insertPunishmentPlaceholders(data.getString("title", null), event)
                    val description = insertPunishmentPlaceholders(data.getString("description", "").takeIf { it.isNotEmpty() }, event)
                    setTitle(title)
                    setDescription(description)
                    parsePlaceholdersToFields { text -> insertPunishmentPlaceholders(text, event) }
                }

                sendChannelMessage(reportChannel, embedBuilder.build())
            }
        } else {
            val punishmentsChannel =
                discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_PUNISHMENT_CHANNEL))
                    ?: return

            val dcMessage = if (event.punishment.isActive) DCMessage.PUNISHMENT_ALERT else DCMessage.UNPUNISHMENT_ALERT

            val data = DataObject.fromJson(discordBot.messages.getString(dcMessage))
            val embedBuilder = EmbedBuilder.fromData(data).apply {
                val title = insertPunishmentPlaceholders(data.getString("title", null), event)
                val description = insertPunishmentPlaceholders(data.getString("description", "").takeIf { it.isNotEmpty() }, event)
                setTitle(title)
                setDescription(description)
                parsePlaceholdersToFields { text -> insertPunishmentPlaceholders(text, event) }
            }

            sendChannelMessage(punishmentsChannel, embedBuilder.build())
        }
    }

    private fun onHelpOP(event: HelpOPRequestEvent) {
        if (event.isCancelled) {
            return
        }
        val helpOPChannel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_HELPOP_CHANNEL))
                ?: return

        val data = DataObject.fromJson(discordBot.messages.getString(DCMessage.HELPOP_ALERT))
        val embedBuilder = EmbedBuilder.fromData(data).apply {
            val title = insertHelpOPPlaceholders(data.getString("title", null), event)
            val description = insertHelpOPPlaceholders(data.getString("description", "").takeIf { it.isNotEmpty() }, event)
            setTitle(title)
            setDescription(description)
            parsePlaceholdersToFields { text -> insertHelpOPPlaceholders(text, event) }
        }

        sendChannelMessage(helpOPChannel, embedBuilder.build())
    }

    private fun onTicketCreate(event: TicketCreateEvent) {
        if (event.isCancelled) {
            return
        }
        val ticketChannel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_TICKETS_CHANNEL))
                ?: return

        val data = DataObject.fromJson(discordBot.messages.getString(DCMessage.TICKET_CREATE_ALERT))
        val embedBuilder = EmbedBuilder.fromData(data).apply {
            val title = insertTicketPlaceholders(data.getString("title", null), event)
            val description = insertTicketPlaceholders(data.getString("description", "").takeIf { it.isNotEmpty() }, event)
            setTitle(title)
            setDescription(description)
            parsePlaceholdersToFields { text -> insertTicketPlaceholders(text, event) }
        }

        sendChannelMessage(ticketChannel, embedBuilder.build())
    }

    private fun onChatLogCreated(event: ChatLogCreatedEvent) {
        val chatLogChannel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_CHATLOG_CHANNEL))
                ?: return

        val data = DataObject.fromJson(discordBot.messages.getString(DCMessage.CHATLOG_ALERT))
        val embedBuilder = EmbedBuilder.fromData(data).apply {
            val title = insertChatLogPlaceholders(data.getString("title", null), event)
            val description = insertChatLogPlaceholders(data.getString("description", "").takeIf { it.isNotEmpty() }, event)
            setTitle(title)
            setDescription(description)
            parsePlaceholdersToFields { text -> insertChatLogPlaceholders(text, event) }
        }

        sendChannelMessage(chatLogChannel, embedBuilder.build())
    }

    private fun onAsyncPlayerLogin(event: AsyncPlayerLoginEvent) {
        val player = event.player ?: return
        val discordId = discordBot.discordUserManager.getDiscordIdByUuid(player.uuid)
        if (discordId != null) {
            checkNotNull(discordBot.guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
            val member = discordBot.guild.getMemberById(discordId)
            if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME_ENABLED)) {
                val format = Placeholders.setPlaceholders(
                    player,
                    discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT)
                )
                discordBot.discordManager.setNickName(member, format)
            }
            if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
                // Delay the task 1.5 seconds to make sure that permissions are loaded
                discordBot.scheduler.runDelayed(SyncRanksTask(discordBot, player), 1500L, TimeUnit.MILLISECONDS)
            }
        }

        if (event.hasLoggedInBefore) {
            val channel =
                discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_LOGIN_CHANNEL))
                    ?: return
            sendChannelMessage(
                channel,
                Placeholders.setPlaceholders(player, discordBot.messages.getString(DCMessage.EVENT_PLAYERLOGIN))
            )
        } else {
            val channel =
                discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_FIRST_LOGIN_CHANNEL))
                    ?: return
            sendChannelMessage(
                channel,
                Placeholders.setPlaceholders(player, discordBot.messages.getString(DCMessage.EVENT_FIRST_PLAYERLOGIN))
            )
        }
    }

    private fun onDisconnect(event: PlayerDisconnectEvent) {
        val player = event.player
        val channel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_DISCONNECT_CHANNEL))
                ?: return
        sendChannelMessage(channel, Placeholders.setPlaceholders(player, discordBot.messages.getString(DCMessage.EVENT_DISCONNECT)))
    }

    private fun onServerConnected(event: ServerConnectedEvent) {
        val player = event.player
        val server = event.server
        val previousServer = event.previousServer ?: return

        val channel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_DISCONNECT_CHANNEL))
                ?: return

        val message = Placeholders.setPlaceholders(player, discordBot.messages.getString(DCMessage.EVENT_SERVER_SWITCH)
            .replace("%previous-server%", previousServer.serverName)
            .replace("%server%", server.serverName)
        )
        sendChannelMessage(channel, message)
    }

    private fun insertServerPlaceholders(s: String?, server: Server): String {
        if (s == null) return ""
        return s.replace("%id%", server.id.toString())
            .replace("%name%", server.displayName.stripColors())
            .replace("%servername%", server.displayName.stripColors())
            .replace("%motd%", server.motd?.stripColors() ?: "No MOTD")
            .replace("%ip%", server.ip)
            .replace("%port%", server.port.toString())
            .replace("%groups%", server.serverGroups.joinToString { it.groupName })
            .replace("%isrestricted%", server.restricted.toString())
    }

    private fun insertHelpOPPlaceholders(s: String?, event: HelpOPRequestEvent): String {
        val sender = event.sender
        if (s == null) return ""
        return s.replace("%message%", event.message)
            .replace("%requester%", sender.name)
            .replace("%server%", sender.server!!)
    }

    private fun insertPunishmentPlaceholders(s: String?, event: PunishmentEvent): String {
        if (s == null) return ""
        val networkManager = discordBot.networkManager
        val cachedPlayers = networkManager.cacheManager.cachedPlayers
        val punishment = event.punishment
        // TODO: Make setting to define this value
        val languageId = 1


        val parsed = s
            .replace("%id%", punishment.id.toString())
            .replace("%type%",
                punishment.type.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
            .replace("%uuid%", punishment.uuid.toString())
            .replace("%playername%", punishment.username ?: "Unknown")
            .replace("%username%", punishment.username ?: "Unknown")
            .replace("%ip%", punishment.ip)
            .replace("%server%", punishment.server ?: "Global")
            .replace("%reason%", punishment.reason)
            .replace("%unbanreason%", punishment.unbanReason ?: "None")
            .replace(
                "%punisher%",
                if (cachedPlayers.isConsole(punishment.punisher)) "Console" else punishment.punisherName ?: "Unknown"
            )
            .replace(
                "%time%", SimpleDateFormat(networkManager.getMessage(languageId, Message.PUNISHMENT_DATETIME_FORMAT))
                    .format(Date(punishment.time))
            )
            .replace(
                "%ends%", SimpleDateFormat(networkManager.getMessage(languageId, Message.PUNISHMENT_DATETIME_FORMAT))
                    .format(Date(punishment.end))
            ).replace(
                "%expires%",
                if (punishment.end == -1L) networkManager.getMessage(languageId, Message.NEVER)
                else TimeUtils.getTimeString(
                    languageId,
                    Utils.ceilDiv(punishment.end - System.currentTimeMillis(), 1000)
                )
            )

        return parsed.stripColors()
    }

    private fun insertTicketPlaceholders(s: String?, event: TicketCreateEvent): String {
        val cachedPlayers = discordBot.networkManager.cacheManager.cachedPlayers
        val ticket = event.ticket
        if (s == null) return ""
        val creator = cachedPlayers.getName(ticket.creator) ?: "Unknown (Could not find name by creator UUID)"
        return s.replace("%id%", ticket.id.toString())
            .replace("%title%", ticket.title)
            .replace("%creator%", creator)
    }

    private fun insertChatLogPlaceholders(s: String?, event: ChatLogCreatedEvent): String {
        val creator = event.creator
        val tracked = event.tracked
        if (s == null) return ""
        return s.replace("%id%", event.chatLogId.toString())
            .replace("%creator%", creator.name)
            .replace("%tracked%", tracked.name)
            .replace("%server%", event.server)
            .replace("%url%", event.chatLogUrl)
    }

    private fun onPlayerChat(event: PlayerChatEvent) {
        if (event.isCancelled) return
        val player = event.player
        val currentServer = player.server ?: "Unknown"
        val chatEventChannels = discordBot.settings.getMap(Setting.DISCORD_EVENTS_CHAT_CHANNELS)
        val globalId = chatEventChannels["all"] ?: "000000000000000000"
        val globalChatTextChannel = discordBot.guild.getTextChannelById(globalId)
        var message = discordBot.messages.getString(DCMessage.EVENT_CHAT)
            .replace("%playername%", player.name)
            .replace("%displayname%", player.displayName.toLegacy())
            .replace("%server%", player.serverDisplay.toLegacy())
            .replace(
                "%message%", event.message
                    .replace(Utils.USER_MENTION_REGEX, "")
                    .replace(Utils.ROLE_MENTION_REGEX, "")
            )
        message = Placeholders.setPlaceholders(player, message).stripColors()

        globalChatTextChannel?.sendMessage(message)?.queue()
        val serverId = chatEventChannels[currentServer] ?: "000000000000000000"
        val serverChatTextChannel = discordBot.guild.getTextChannelById(serverId)
        serverChatTextChannel?.sendMessage(message)?.queue()
    }

    init {
        discordBot.platform.eventBus.run {
            subscribe(StaffChatEvent::class.java, ::onStaffChat)
            subscribe(AdminChatEvent::class.java, ::onAdminChat)
            subscribe(ServerStatusChangeEvent::class.java, ::onServerStatusChange)
            subscribe(PunishmentEvent::class.java, ::onPunishment)
            subscribe(HelpOPRequestEvent::class.java, ::onHelpOP)
            subscribe(TicketCreateEvent::class.java, ::onTicketCreate)
            subscribe(ChatLogCreatedEvent::class.java, ::onChatLogCreated)
            subscribe(AsyncPlayerLoginEvent::class.java, ::onAsyncPlayerLogin)
            subscribe(PlayerDisconnectEvent::class.java, ::onDisconnect)
            subscribe(ServerConnectedEvent::class.java, ::onServerConnected)
            subscribe(PlayerChatEvent::class.java, ::onPlayerChat, PostOrders.LAST)
        }
    }
}
