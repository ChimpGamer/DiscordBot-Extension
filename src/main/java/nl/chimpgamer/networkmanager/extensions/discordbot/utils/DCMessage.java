package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

public enum DCMessage {

    STAFFCHAT_RECEIVE("discord.staffchat-receive"),
    ADMINCHAT_RECEIVE("discord.adminchat-receive"),
    REGISTRATION_TOKEN("discord.registration-token"),
    REGISTRATION_NOT_IN_SERVER("registration-not-in-server"),
    REGISTRATION_COMPLETED("discord.registration-completed"),
    UNREGISTER_COMPLETED("discord.unregister-completed"),
    SERVER_STATUS_ONLINE("discord.server-status-online"),
    SERVER_STATUS_OFFLINE("discord.server-status-offline"),
    HELPOP_ALERT("discord.helpop-alert"),
    TICKET_CREATE_ALERT("discord.ticket-create-alert"),
    PUNISHMENT_ALERT("discord.punishment-alert"),
    UNPUNISHMENT_ALERT("discord.unpunishment-alert"),
    REPORT_ALERT("discord.report-alert"),
    BUGREPORT_ALERT("discord.bugreport-alert"),
    SUGGESTION_ALERT("discord.suggestion-alert"),
    CHATLOG_ALERT("discord.chatlog-alert"),
    PLAYERS_COMMAND_RESPONSE("discord.player-command-response"),
    PLAYERLIST_COMMAND_INVALID_SERVER("playerlist-command-invalid-server"),

    ;

    private final String path;

    DCMessage(String path) {
        this.path = path;
    }

    public String getMessage() {
        return DiscordBot.getInstance().getMessagesConfigManager().getString(this.path).replace("%newline%", "\n");
    }
}