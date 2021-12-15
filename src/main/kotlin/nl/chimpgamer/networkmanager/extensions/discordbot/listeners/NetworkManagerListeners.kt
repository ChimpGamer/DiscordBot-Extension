package nl.chimpgamer.networkmanager.extensions.discordbot.listeners

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import net.dv8tion.jda.api.entities.MessageEmbed
import nl.chimpgamer.networkmanager.api.event.PostOrders
import nl.chimpgamer.networkmanager.api.event.events.*
import nl.chimpgamer.networkmanager.api.event.events.player.AsyncPlayerLoginEvent
import nl.chimpgamer.networkmanager.api.event.events.ticket.TicketCreateEvent
import nl.chimpgamer.networkmanager.api.models.punishments.Punishment
import nl.chimpgamer.networkmanager.api.models.servers.Server
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.api.utils.stripColors
import nl.chimpgamer.networkmanager.common.utils.Methods
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage
import java.util.*

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
                .replace("%server%", event.sender.server!!)
                .replace("%message%", event.message)
        )
    }

    private fun onServerStatusChange(event: ServerStatusChangeEvent) {
        val server = event.server
        val serverChannels = discordBot.settings.getMap(Setting.DISCORD_EVENTS_SERVERSTATUS_CHANNELS)
        val globalId = serverChannels["all"] ?: "000000000000000000"
        val globalServerTextChannel = discordBot.guild.getTextChannelById(globalId)
        if (globalServerTextChannel != null) {
            val jsonEmbedBuilder: JsonEmbedBuilder
            if (event.isOnline) { // Server went on
                jsonEmbedBuilder =
                    JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.SERVER_STATUS_ONLINE))
                jsonEmbedBuilder
                    .setTitle(insertServerPlaceholders(jsonEmbedBuilder.title, server))
                    .setDescription(insertServerPlaceholders(jsonEmbedBuilder.description.toString(), server))
                val fields: MutableList<MessageEmbed.Field> = LinkedList()
                for (field in jsonEmbedBuilder.fields) {
                    val name = insertServerPlaceholders(field.name, server)
                    val value = insertServerPlaceholders(field.value, server)
                    val field1 = MessageEmbed.Field(name, value, field.isInline)
                    fields.add(field1)
                }
                jsonEmbedBuilder.fields = fields
                sendChannelMessage(
                    globalServerTextChannel,
                    jsonEmbedBuilder.build()
                )
            } else { // Server went off
                jsonEmbedBuilder =
                    JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.SERVER_STATUS_OFFLINE))
                jsonEmbedBuilder
                    .setTitle(insertServerPlaceholders(jsonEmbedBuilder.title, server))
                    .setDescription(insertServerPlaceholders(jsonEmbedBuilder.description.toString(), server))
                val fields: MutableList<MessageEmbed.Field> = LinkedList()
                for (field in jsonEmbedBuilder.fields) {
                    val name = insertServerPlaceholders(field.name, server)
                    val value = insertServerPlaceholders(field.value, server)
                    val field1 = MessageEmbed.Field(name, value, field.isInline)
                    fields.add(field1)
                }
                jsonEmbedBuilder.fields = fields
                sendChannelMessage(
                    globalServerTextChannel,
                    jsonEmbedBuilder.build()
                )
            }
        }
        val channelId = serverChannels[server.serverName] ?: "000000000000000000"
        val serverStatusChannel = discordBot.guild.getTextChannelById(channelId)
        if (serverStatusChannel != null) {
            val jsonEmbedBuilder: JsonEmbedBuilder
            if (event.isOnline) { // Server went on
                jsonEmbedBuilder =
                    JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.SERVER_STATUS_ONLINE))
                jsonEmbedBuilder
                    .setTitle(insertServerPlaceholders(jsonEmbedBuilder.title, server))
                    .setDescription(insertServerPlaceholders(jsonEmbedBuilder.description.toString(), server))
                val fields: MutableList<MessageEmbed.Field> = LinkedList()
                for (field in jsonEmbedBuilder.fields) {
                    val name = insertServerPlaceholders(field.name, server)
                    val value = insertServerPlaceholders(field.value, server)
                    val field1 = MessageEmbed.Field(name, value, field.isInline)
                    fields.add(field1)
                }
                jsonEmbedBuilder.fields = fields
                sendChannelMessage(
                    serverStatusChannel,
                    jsonEmbedBuilder.build()
                )
            } else { // Server went off
                jsonEmbedBuilder =
                    JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.SERVER_STATUS_OFFLINE))
                jsonEmbedBuilder
                    .setTitle(insertServerPlaceholders(jsonEmbedBuilder.title, server))
                    .setDescription(insertServerPlaceholders(jsonEmbedBuilder.description.toString(), server))
                val fields: MutableList<MessageEmbed.Field> = LinkedList()
                for (field in jsonEmbedBuilder.fields) {
                    val name = insertServerPlaceholders(field.name, server)
                    val value = insertServerPlaceholders(field.value, server)
                    val field1 = MessageEmbed.Field(name, value, field.isInline)
                    fields.add(field1)
                }
                jsonEmbedBuilder.fields = fields
                sendChannelMessage(
                    serverStatusChannel,
                    jsonEmbedBuilder.build()
                )
            }
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
            val jsonEmbedBuilder: JsonEmbedBuilder
            if (event.punishment.isActive) {
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.REPORT_ALERT))
                jsonEmbedBuilder
                    .setTitle(insertPunishmentPlaceholders(jsonEmbedBuilder.title, event))
                    .setDescription(insertPunishmentPlaceholders(jsonEmbedBuilder.description.toString(), event))
                val fields: MutableList<MessageEmbed.Field> = LinkedList()
                for (field in jsonEmbedBuilder.fields) {
                    val name = insertPunishmentPlaceholders(field.name, event)
                    val value = insertPunishmentPlaceholders(field.value, event)
                    val field1 = MessageEmbed.Field(name, value, field.isInline)
                    fields.add(field1)
                }
                jsonEmbedBuilder.fields = fields
                sendChannelMessage(
                    reportChannel,
                    jsonEmbedBuilder.build()
                )
            }
        } else {
            val punishmentsChannel =
                discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_PUNISHMENT_CHANNEL))
                    ?: return
            val jsonEmbedBuilder: JsonEmbedBuilder
            if (event.punishment.isActive) {
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.PUNISHMENT_ALERT))
                jsonEmbedBuilder
                    .setTitle(insertPunishmentPlaceholders(jsonEmbedBuilder.title, event))
                    .setDescription(insertPunishmentPlaceholders(jsonEmbedBuilder.description.toString(), event))
                val fields: MutableList<MessageEmbed.Field> = LinkedList()
                for (field in jsonEmbedBuilder.fields) {
                    val name = insertPunishmentPlaceholders(field.name, event)
                    val value = insertPunishmentPlaceholders(field.value, event)
                    val field1 = MessageEmbed.Field(name, value, field.isInline)
                    fields.add(field1)
                }
                jsonEmbedBuilder.fields = fields
                sendChannelMessage(
                    punishmentsChannel,
                    jsonEmbedBuilder.build()
                )
            } else {
                jsonEmbedBuilder =
                    JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.UNPUNISHMENT_ALERT))
                jsonEmbedBuilder
                    .setTitle(insertPunishmentPlaceholders(jsonEmbedBuilder.title, event))
                    .setDescription(insertPunishmentPlaceholders(jsonEmbedBuilder.description.toString(), event))
                val fields: MutableList<MessageEmbed.Field> = LinkedList()
                for (field in jsonEmbedBuilder.fields) {
                    val name = insertPunishmentPlaceholders(field.name, event)
                    val value = insertPunishmentPlaceholders(field.value, event)
                    val field1 = MessageEmbed.Field(name, value, field.isInline)
                    fields.add(field1)
                }
                jsonEmbedBuilder.fields = fields
                sendChannelMessage(
                    punishmentsChannel,
                    jsonEmbedBuilder.build()
                )
            }
        }
    }

    private fun onHelpOP(event: HelpOPRequestEvent) {
        if (event.isCancelled) {
            return
        }
        val helpOPChannel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_HELPOP_CHANNEL))
                ?: return
        val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.HELPOP_ALERT))
        val fields: MutableList<MessageEmbed.Field> = LinkedList()
        for (field in jsonEmbedBuilder.fields) {
            val name = insertHelpOPPlaceholders(field.name, event)
            val value = insertHelpOPPlaceholders(field.value, event)
            val field1 = MessageEmbed.Field(name, value, field.isInline)
            fields.add(field1)
        }
        jsonEmbedBuilder.fields = fields
        sendChannelMessage(
            helpOPChannel,
            jsonEmbedBuilder.build()
        )
    }

    private fun onTicketCreate(event: TicketCreateEvent) {
        if (event.isCancelled) {
            return
        }
        val ticketChannel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_TICKETS_CHANNEL))
                ?: return
        val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.TICKET_CREATE_ALERT))
        val fields: MutableList<MessageEmbed.Field> = LinkedList()
        for (field in jsonEmbedBuilder.fields) {
            val name = insertTicketPlaceholders(field.name, event)
            val value = insertTicketPlaceholders(field.value, event)
            fields.add(MessageEmbed.Field(name, value, field.isInline))
        }
        jsonEmbedBuilder.fields = fields
        sendChannelMessage(
            ticketChannel,
            jsonEmbedBuilder.build()
        )
    }

    private fun onChatLogCreated(event: ChatLogCreatedEvent) {
        val chatLogChannel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_CHATLOG_CHANNEL))
                ?: return
        val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(discordBot.messages.getString(DCMessage.CHATLOG_ALERT))
        val fields: MutableList<MessageEmbed.Field> = LinkedList()
        for (field in jsonEmbedBuilder.fields) {
            val name = insertChatLogPlaceholders(field.name, event)
            val value = insertChatLogPlaceholders(field.value, event)
            fields.add(MessageEmbed.Field(name, value, field.isInline))
        }
        jsonEmbedBuilder.fields = fields
        sendChannelMessage(
            chatLogChannel,
            jsonEmbedBuilder.build()
        )
    }

    private fun onAsyncPlayerLogin(event: AsyncPlayerLoginEvent) {
        val player = event.player ?: return
        if (event.hasLoggedInBefore) {
            val channel =
                discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_LOGIN_CHANNEL))
                    ?: return
            sendChannelMessage(channel, Placeholders.setPlaceholders(player, discordBot.messages.getString(DCMessage.EVENT_PLAYERLOGIN)))
            return
        }

        val channel =
            discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_FIRST_LOGIN_CHANNEL))
                ?: return
        sendChannelMessage(channel, Placeholders.setPlaceholders(player, discordBot.messages.getString(DCMessage.EVENT_FIRST_PLAYERLOGIN)))
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
        val parsed = Methods.parsePunishmentPlaceholders(event.punishment, 1, s)
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
            .replace("%server%", currentServer)
            .replace("%message%", event.message
                .replace(FinderUtil.USER_MENTION.toRegex(), "")
                .replace(FinderUtil.ROLE_MENTION.toRegex(), ""))
        message = Placeholders.setPlaceholders(player, message)

        globalChatTextChannel?.sendMessage(message)?.queue()
        val serverId = chatEventChannels[currentServer] ?: "000000000000000000"
        val serverChatTextChannel = discordBot.guild.getTextChannelById(serverId)
        serverChatTextChannel?.sendMessage(message)?.queue()
    }

    init {
        discordBot.eventBus.run {
            subscribe(StaffChatEvent::class.java, ::onStaffChat)
            subscribe(AdminChatEvent::class.java, ::onAdminChat)
            subscribe(ServerStatusChangeEvent::class.java, ::onServerStatusChange)
            subscribe(PunishmentEvent::class.java, ::onPunishment)
            subscribe(HelpOPRequestEvent::class.java, ::onHelpOP)
            subscribe(TicketCreateEvent::class.java, ::onTicketCreate)
            subscribe(ChatLogCreatedEvent::class.java, ::onChatLogCreated)
            subscribe(AsyncPlayerLoginEvent::class.java, ::onAsyncPlayerLogin)
            subscribe(PlayerChatEvent::class.java, ::onPlayerChat, PostOrders.LAST)
        }
    }

}