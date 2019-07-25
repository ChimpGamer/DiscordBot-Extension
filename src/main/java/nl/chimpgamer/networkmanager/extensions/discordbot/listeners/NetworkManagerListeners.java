package nl.chimpgamer.networkmanager.extensions.discordbot.listeners;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import nl.chimpgamer.networkmanager.api.event.events.*;
import nl.chimpgamer.networkmanager.api.utils.TimeUtils;
import nl.chimpgamer.networkmanager.bungeecord.models.servers.NMServer;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;
import nl.chimpgamer.networkmanager.api.NMListener;
import nl.chimpgamer.networkmanager.api.cache.modules.CachedPlayers;
import nl.chimpgamer.networkmanager.api.event.NMEvent;
import nl.chimpgamer.networkmanager.api.event.events.ticket.TicketCreateEvent;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.api.models.punishments.Punishment;
import nl.chimpgamer.networkmanager.api.models.servers.Server;
import nl.chimpgamer.networkmanager.api.models.servers.ServerGroup;
import nl.chimpgamer.networkmanager.api.models.tickets.Ticket;
import nl.chimpgamer.networkmanager.api.values.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class NetworkManagerListeners implements NMListener {
    private final DiscordBot discordBot;

    @NMEvent
    public void onStaffChat(StaffChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        TextChannel staffChatChannel = this.getDiscordBot().getGuild().getTextChannelById(this.getDiscordBot().getConfigManager().getStaffChatChannelID());
        if (staffChatChannel == null) {
            return;
        }
        Utils.sendChannelMessage(staffChatChannel,
                DCMessage.STAFFCHAT_RECEIVE.getMessage()
                        .replace("%playername%", event.getSender().getName())
                        .replace("%server%", event.getSender().getServer())
                        .replace("%message%", event.getMessage()));
    }

    @NMEvent
    public void onAdminChat(AdminChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        TextChannel adminChatChannel = this.getDiscordBot().getGuild().getTextChannelById(this.getDiscordBot().getConfigManager().getAdminChatChannelID());
        if (adminChatChannel == null) {
            return;
        }
        Utils.sendChannelMessage(adminChatChannel,
                DCMessage.ADMINCHAT_RECEIVE.getMessage()
                        .replace("%playername%", event.getSender().getName())
                        .replace("%server%", event.getSender().getServer())
                        .replace("%message%", event.getMessage()));
    }

    @NMEvent
    public void onServerStatusChange(ServerStatusChangeEvent event) {
        final Server server = event.getServer();
        TextChannel serverStatusChannel = this.getDiscordBot().getGuild().getTextChannelById(this.getDiscordBot().getConfigManager().getServerStatusEventChannelId());
        if (serverStatusChannel == null) {
            return;
        }
        JsonEmbedBuilder jsonEmbedBuilder;
        if (event.isOnline()) {
            // Server went on
            jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.SERVER_STATUS_ONLINE.getMessage());
            final List<MessageEmbed.Field> fields = new LinkedList<>();
            for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
                String name = insertServerPlaceholders(field.getName(), server);
                String value = insertServerPlaceholders(field.getValue(), server);
                MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
                fields.add(field1);
            }
            jsonEmbedBuilder.setFields(fields);
            Utils.sendChannelMessage(serverStatusChannel,
                    jsonEmbedBuilder.build());
        } else {
            // Server went off
            jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.SERVER_STATUS_OFFLINE.getMessage());
            final List<MessageEmbed.Field> fields = new LinkedList<>();
            for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
                String name = insertServerPlaceholders(field.getName(), server);
                String value = insertServerPlaceholders(field.getValue(), server);
                MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
                fields.add(field1);
            }
            jsonEmbedBuilder.setFields(fields);
            Utils.sendChannelMessage(serverStatusChannel,
                    jsonEmbedBuilder.build());
        }
    }

    @NMEvent
    public void onPunishment(PunishmentEvent event) {
        if (event.getPunishment().getType() == Punishment.Type.NOTE) {
            return;
        }
        if (event.getPunishment().getType() == Punishment.Type.REPORT) {
            TextChannel reportChannel = this.getDiscordBot().getGuild().getTextChannelById(this.getDiscordBot().getConfigManager().getReportAlertsEventChannelId());
            if (reportChannel == null) {
                return;
            }
            JsonEmbedBuilder jsonEmbedBuilder;
            if (event.getPunishment().isActive()) {
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.REPORT_ALERT.getMessage());
                final List<MessageEmbed.Field> fields = new LinkedList<>();
                for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
                    String name = insertPunishmentPlaceholders(field.getName(), event);
                    String value = insertPunishmentPlaceholders(field.getValue(), event);
                    MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
                    fields.add(field1);
                }
                jsonEmbedBuilder.setFields(fields);
                Utils.sendChannelMessage(reportChannel,
                        jsonEmbedBuilder.build());
            }
        } else {
            TextChannel punishmentsChannel = this.getDiscordBot().getGuild().getTextChannelById(this.getDiscordBot().getConfigManager().getPunishmentAlertsEventChannelId());
            if (punishmentsChannel == null) {
                return;
            }
            JsonEmbedBuilder jsonEmbedBuilder;
            if (event.getPunishment().isActive()) {
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.PUNISHMENT_ALERT.getMessage());
                final List<MessageEmbed.Field> fields = new LinkedList<>();
                for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
                    String name = insertPunishmentPlaceholders(field.getName(), event);
                    String value = insertPunishmentPlaceholders(field.getValue(), event);
                    MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
                    fields.add(field1);
                }
                jsonEmbedBuilder.setFields(fields);
                Utils.sendChannelMessage(punishmentsChannel,
                        jsonEmbedBuilder.build());
            } else {
                jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.UNPUNISHMENT_ALERT.getMessage());
                final List<MessageEmbed.Field> fields = new LinkedList<>();
                for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
                    String name = insertPunishmentPlaceholders(field.getName(), event);
                    String value = insertPunishmentPlaceholders(field.getValue(), event);
                    MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
                    fields.add(field1);
                }
                jsonEmbedBuilder.setFields(fields);
                Utils.sendChannelMessage(this.getDiscordBot().getGuild().getTextChannelById(this.getDiscordBot().getConfigManager().getPunishmentAlertsEventChannelId()),
                        jsonEmbedBuilder.build());
            }
        }
    }

    @NMEvent
    public void onHelpOP(HelpOPRequestEvent event) {
        if (event.isCancelled()) {
            return;
        }
        TextChannel helpOPChannel = this.getDiscordBot().getGuild().getTextChannelById(this.getDiscordBot().getConfigManager().getHelpOPAlertsEventChannelId());
        if (helpOPChannel == null) {
            return;
        }
        JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.HELPOP_ALERT.getMessage());
        final List<MessageEmbed.Field> fields = new LinkedList<>();
        for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
            String name = insertHelpOPPlaceholders(field.getName(), event);
            String value = insertHelpOPPlaceholders(field.getValue(), event);
            MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
            fields.add(field1);
        }
        jsonEmbedBuilder.setFields(fields);
        Utils.sendChannelMessage(helpOPChannel,
                jsonEmbedBuilder.build());
    }

    @NMEvent
    public void onTicketCreate(TicketCreateEvent event) {
        if (event.isCancelled()) {
            return;
        }
        TextChannel ticketChannel = this.getDiscordBot().getGuild().getTextChannelById(this.getDiscordBot().getConfigManager().getTicketsChannelId());
        if (ticketChannel == null) {
            return;
        }
        JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.TICKET_CREATE_ALERT.getMessage());
        final List<MessageEmbed.Field> fields = new LinkedList<>();
        for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
            String name = insertTicketPlaceholders(field.getName(), event);
            String value = insertTicketPlaceholders(field.getValue(), event);

            fields.add(new MessageEmbed.Field(name, value, field.isInline()));
        }
        jsonEmbedBuilder.setFields(fields);
        Utils.sendChannelMessage(ticketChannel,
                jsonEmbedBuilder.build());
    }

    @NMEvent
    public void onChatLogCreated(ChatLogCreatedEvent event) {
        TextChannel chatLogChannel = this.getDiscordBot().getGuild().getTextChannelById(this.getDiscordBot().getConfigManager().getChatLogEventChannelId());
        if (chatLogChannel == null) {
            return;
        }
        JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.CHATLOG_ALERT.getMessage());
        final List<MessageEmbed.Field> fields = new LinkedList<>();
        for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
            String name = insertChatLogPlaceholders(field.getName(), event);
            String value = insertChatLogPlaceholders(field.getValue(), event);

            fields.add(new MessageEmbed.Field(name, value, field.isInline()));
        }
        jsonEmbedBuilder.setFields(fields);
        Utils.sendChannelMessage(chatLogChannel,
                jsonEmbedBuilder.build());
    }

    private String insertServerPlaceholders(String s, Server<NMServer> server) {
        return s.replace("%id%", String.valueOf(server.getId()))
                .replace("%name%", server.getServerName())
                .replace("%motd%", server.getMotd())
                .replace("%ip%", server.getIp())
                .replace("%port%", String.valueOf(server.getPort()))
                .replace("%groups%", server.getServerGroups().stream().map(ServerGroup::getGroupName).collect(Collectors.toList()).toString())
                .replace("%isrestricted%", String.valueOf(server.isRestricted()));
    }

    private String insertHelpOPPlaceholders(String s, HelpOPRequestEvent event) {
        final Player sender = event.getSender();
        return s.replace("%message%", event.getMessage())
                .replace("%requester%", sender.getName())
                .replace("%server%", sender.getServer());
    }

    private String insertPunishmentPlaceholders(String s, PunishmentEvent event) {
        final CachedPlayers cachedPlayers = getDiscordBot().getNetworkManager().getCacheManager().getCachedPlayers();
        final Punishment punishment = event.getPunishment();
        return s.replace("%id%", String.valueOf(punishment.getId()))
                .replace("%type%", Utils.firstUpperCase(punishment.getType().name().toLowerCase()))
                .replace("%playername%", cachedPlayers.getName(punishment.getUuid()))
                .replace("%punisher%", cachedPlayers.getName(punishment.getPunisher()))
                .replace("%time%", new SimpleDateFormat(getDiscordBot().getNetworkManager().getMessage(Message.PUNISHMENT_DATETIME_FORMAT)).format(new Date(punishment.getTime())))
                .replace("%ends%", punishment.getEnd() == -1 ? "Permanent" : new SimpleDateFormat(getDiscordBot().getNetworkManager().getMessage(1, "lang_punishment_datetime_format")).format(new Date(punishment.getEnd())))
                .replace("%expires%", TimeUtils.getTimeString(1, (punishment.getEnd() - punishment.getTime()) / 1000))
                .replace("%server%", punishment.getServer() == null ? "Global" : punishment.getServer())
                .replace("%ip%", punishment.getIp())
                .replace("%reason%", punishment.getReason())
                .replace("%unpunisher%", punishment.getUnbanner() == null ? "NULL" : cachedPlayers.getName(punishment.getUnbanner()))
                .replace("%active%", String.valueOf(punishment.isActive()));
    }

    private String insertTicketPlaceholders(String s, TicketCreateEvent event) {
        final CachedPlayers cachedPlayer = getDiscordBot().getNetworkManager().getCacheManager().getCachedPlayers();
        final Ticket ticket = event.getTicket();
        return s.replace("%id%", String.valueOf(ticket.getId()))
                .replace("%title%", ticket.getTitle())
                .replace("%creator%", cachedPlayer.getName(ticket.getCreator()));
    }

    private String insertChatLogPlaceholders(String s, ChatLogCreatedEvent event) {
        final Player creator = event.getCreator();
        final Player tracked = event.getTracked();
        return s.replace("%id%", String.valueOf(event.getChatLogId()))
                .replace("%creator%", creator.getName())
                .replace("%tracked%", tracked.getName())
                .replace("%server%", event.getServer())
                .replace("%url%", event.getChatLogUrl());
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}