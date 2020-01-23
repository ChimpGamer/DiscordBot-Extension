package nl.chimpgamer.networkmanager.extensions.discordbot.listeners

import net.dv8tion.jda.api.entities.MessageEmbed
import nl.chimpgamer.networkmanager.api.NMListener
import nl.chimpgamer.networkmanager.api.cache.modules.CachedPlayers
import nl.chimpgamer.networkmanager.api.event.NMEvent
import nl.chimpgamer.networkmanager.api.event.events.*
import nl.chimpgamer.networkmanager.api.event.events.ticket.TicketCreateEvent
import nl.chimpgamer.networkmanager.api.models.punishments.Punishment
import nl.chimpgamer.networkmanager.api.models.servers.Server
import nl.chimpgamer.networkmanager.api.models.servers.ServerGroup
import nl.chimpgamer.networkmanager.common.utils.Methods
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage
import java.util.*
import java.util.stream.Collectors

class NetworkManagerListeners(private val discordBot: DiscordBot) : NMListener {

    @NMEvent
    fun onStaffChat(event: StaffChatEvent) {
        if (event.isCancelled) {
            return
        }
        val staffChatChannel = discordBot.guild.getTextChannelById(Setting.DISCORD_EVENTS_STAFFCHAT_CHANNEL.asString)
                ?: return
        sendChannelMessage(staffChatChannel,
                DCMessage.STAFFCHAT_RECEIVE.message
                        .replace("%playername%", event.sender.name)
                        .replace("%server%", event.sender.server)
                        .replace("%message%", event.message))
    }

    @NMEvent
    fun onAdminChat(event: AdminChatEvent) {
        if (event.isCancelled) {
            return
        }
        val adminChatChannel = discordBot.guild.getTextChannelById(Setting.DISCORD_EVENTS_ADMINCHAT_CHANNEL.asString)
                ?: return
        sendChannelMessage(adminChatChannel,
                DCMessage.ADMINCHAT_RECEIVE.message
                        .replace("%playername%", event.sender.name)
                        .replace("%server%", event.sender.server)
                        .replace("%message%", event.message))
    }

    @NMEvent
    fun onServerStatusChange(event: ServerStatusChangeEvent) {
        val server = event.server
        val serverChannels = Setting.DISCORD_EVENTS_SERVERSTATUS_CHANNELS.asMap
        var globalId: String? = "000000000000000000"
        if (serverChannels.containsKey("all")) {
            globalId = serverChannels["all"]
        }
        val globalServerTextChannel = discordBot.guild.getTextChannelById(globalId!!)
        if (globalServerTextChannel != null) {
            val jsonEmbedBuilder: JsonEmbedBuilder
            if (event.isOnline) { // Server went on
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.SERVER_STATUS_ONLINE.message)
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
                sendChannelMessage(globalServerTextChannel,
                        jsonEmbedBuilder.build())
            } else { // Server went off
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.SERVER_STATUS_OFFLINE.message)
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
                sendChannelMessage(globalServerTextChannel,
                        jsonEmbedBuilder.build())
            }
        }
        var channelId: String? = "000000000000000000"
        if (serverChannels.containsKey(server.serverName)) {
            channelId = serverChannels[server.serverName]
        }
        val serverStatusChannel = discordBot.guild.getTextChannelById(channelId!!)
        if (serverStatusChannel != null) {
            val jsonEmbedBuilder: JsonEmbedBuilder
            if (event.isOnline) { // Server went on
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.SERVER_STATUS_ONLINE.message)
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
                sendChannelMessage(serverStatusChannel,
                        jsonEmbedBuilder.build())
            } else { // Server went off
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.SERVER_STATUS_OFFLINE.message)
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
                sendChannelMessage(serverStatusChannel,
                        jsonEmbedBuilder.build())
            }
        }
    }

    @NMEvent
    fun onPunishment(event: PunishmentEvent) {
        if (event.punishment.type == Punishment.Type.NOTE) {
            return
        }
        if (event.punishment.type == Punishment.Type.REPORT) {
            val reportChannel = discordBot.guild.getTextChannelById(Setting.DISCORD_EVENTS_REPORT_CHANNEL.asString)
                    ?: return
            val jsonEmbedBuilder: JsonEmbedBuilder
            if (event.punishment.isActive) {
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.REPORT_ALERT.message)
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
                sendChannelMessage(reportChannel,
                        jsonEmbedBuilder.build())
            }
        } else {
            val punishmentsChannel = discordBot.guild.getTextChannelById(Setting.DISCORD_EVENTS_PUNISHMENT_CHANNEL.asString)
                    ?: return
            val jsonEmbedBuilder: JsonEmbedBuilder
            if (event.punishment.isActive) {
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.PUNISHMENT_ALERT.message)
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
                sendChannelMessage(punishmentsChannel,
                        jsonEmbedBuilder.build())
            } else {
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.UNPUNISHMENT_ALERT.message)
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
                sendChannelMessage(punishmentsChannel,
                        jsonEmbedBuilder.build())
            }
        }
    }

    @NMEvent
    fun onHelpOP(event: HelpOPRequestEvent) {
        if (event.isCancelled) {
            return
        }
        val helpOPChannel = discordBot.guild.getTextChannelById(Setting.DISCORD_EVENTS_HELPOP_CHANNEL.asString)
                ?: return
        val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.HELPOP_ALERT.message)
        val fields: MutableList<MessageEmbed.Field> = LinkedList()
        for (field in jsonEmbedBuilder.fields) {
            val name = insertHelpOPPlaceholders(field.name, event)
            val value = insertHelpOPPlaceholders(field.value, event)
            val field1 = MessageEmbed.Field(name, value, field.isInline)
            fields.add(field1)
        }
        jsonEmbedBuilder.fields = fields
        sendChannelMessage(helpOPChannel,
                jsonEmbedBuilder.build())
    }

    @NMEvent
    fun onTicketCreate(event: TicketCreateEvent) {
        if (event.isCancelled) {
            return
        }
        val ticketChannel = discordBot.guild.getTextChannelById(Setting.DISCORD_EVENTS_TICKETS_CHANNEL.asString)
                ?: return
        val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.TICKET_CREATE_ALERT.message)
        val fields: MutableList<MessageEmbed.Field> = LinkedList()
        for (field in jsonEmbedBuilder.fields) {
            val name = insertTicketPlaceholders(field.name, event)
            val value = insertTicketPlaceholders(field.value, event)
            fields.add(MessageEmbed.Field(name, value, field.isInline))
        }
        jsonEmbedBuilder.fields = fields
        sendChannelMessage(ticketChannel,
                jsonEmbedBuilder.build())
    }

    @NMEvent
    fun onChatLogCreated(event: ChatLogCreatedEvent) {
        val chatLogChannel = discordBot.guild.getTextChannelById(Setting.DISCORD_EVENTS_CHATLOG_CHANNEL.asString)
                ?: return
        val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.CHATLOG_ALERT.message)
        val fields: MutableList<MessageEmbed.Field> = LinkedList()
        for (field in jsonEmbedBuilder.fields) {
            val name = insertChatLogPlaceholders(field.name, event)
            val value = insertChatLogPlaceholders(field.value, event)
            fields.add(MessageEmbed.Field(name, value, field.isInline))
        }
        jsonEmbedBuilder.fields = fields
        sendChannelMessage(chatLogChannel,
                jsonEmbedBuilder.build())
    }

    private fun insertServerPlaceholders(s: String?, server: Server): String {
        return s!!.replace("%id%", server.id.toString())
                .replace("%name%", server.serverName)
                .replace("%motd%", server.motd)
                .replace("%ip%", server.ip)
                .replace("%port%", server.port.toString())
                .replace("%groups%", server.serverGroups.stream().map { obj: ServerGroup -> obj.groupName }.collect(Collectors.toList()).toString())
                .replace("%isrestricted%", server.isRestricted.toString())
    }

    private fun insertHelpOPPlaceholders(s: String?, event: HelpOPRequestEvent): String {
        val sender = event.sender
        return s!!.replace("%message%", event.message)
                .replace("%requester%", sender.name)
                .replace("%server%", sender.server)
    }

    private fun insertPunishmentPlaceholders(s: String?, event: PunishmentEvent): String {
        return Methods.parsePunishmentPlaceholders(event.punishment, 1, s)
    }

    private fun insertTicketPlaceholders(s: String?, event: TicketCreateEvent): String {
        val cachedPlayer: CachedPlayers = discordBot.networkManager.cacheManager.cachedPlayers
        val ticket = event.ticket
        return s!!.replace("%id%", ticket.id.toString())
                .replace("%title%", ticket.title)
                .replace("%creator%", cachedPlayer.getName(ticket.creator))
    }

    private fun insertChatLogPlaceholders(s: String?, event: ChatLogCreatedEvent): String {
        val creator = event.creator
        val tracked = event.tracked
        return s!!.replace("%id%", event.chatLogId.toString())
                .replace("%creator%", creator.name)
                .replace("%tracked%", tracked.name)
                .replace("%server%", event.server)
                .replace("%url%", event.chatLogUrl)
    }

}