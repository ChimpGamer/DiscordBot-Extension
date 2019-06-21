package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import com.google.common.collect.Maps;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.api.utils.Config;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigManager extends Config {
    private final DiscordBot discordBot;

    public ConfigManager(DiscordBot discordBot) {
        super(discordBot.getDataFolder() + File.separator + "settings.yml");
        this.discordBot = discordBot;

        this.addDefault("networkmanagerbot.discord.token", "your-token-here");
        this.addDefault("networkmanagerbot.discord.guildId", "your-guildid-here");
        this.addDefault("networkmanagerbot.discord.commandprefix", "!");
        this.addDefault("networkmanagerbot.discord.status.enabled", true);
        this.addDefault("networkmanagerbot.discord.status.type", "watching");
        this.addDefault("networkmanagerbot.discord.status.message", "players on Hypixel");
        this.addDefault("networkmanagerbot.discord.ownerId", "000000000000000000");

        this.addDefault("networkmanagerbot.discord.verify.addrole.enabled", false);
        this.addDefault("networkmanagerbot.discord.verify.addrole.role-name", "verified-rolename-here");

        this.addDefault("networkmanagerbot.discord.sync-username", false);
        this.addDefault("networkmanagerbot.discord.sync-ranks.enabled", false);
        this.addDefault("networkmanagerbot.discord.sync-ranks.ranks", Arrays.asList("VIP", "SuperVIP"));

        this.addDefault("networkmanagerbot.discord.commands.playerlist.enabled", true);
        this.addDefault("networkmanagerbot.discord.commands.players.enabled", true);

        this.addDefault("networkmanagerbot.discord.channels.staffchat", "your-staffchat-channelid-here");
        this.addDefault("networkmanagerbot.discord.channels.adminchat", "your-adminchat-channelid-here");
        this.addDefault("networkmanagerbot.discord.channels.events.server-status", "your-server-status-channelid-here");
        //this.addDefault("networkmanagerbot.discord.channels.events.server-status.all", "your-server-status-channelid-here");
        this.addDefault("networkmanagerbot.discord.channels.events.tickets", "your-tickets-channelid-here");
        this.addDefault("networkmanagerbot.discord.channels.events.helpop-alerts", "your-helpop-alerts-channelid-here");
        this.addDefault("networkmanagerbot.discord.channels.events.punishment-alerts", "your-punishment-alerts-channelid-here");
        this.addDefault("networkmanagerbot.discord.channels.events.report-alerts", "your-punishment-alerts-channelid-here");
        this.addDefault("networkmanagerbot.discord.channels.events.bugreport-alerts", "your-bugreport-alerts-channelid-here");
        this.addDefault("networkmanagerbot.discord.channels.events.suggestion-alerts", "your-bugreport-alerts-channelid-here");
        this.addDefault("networkmanagerbot.discord.channels.events.chatlog-alerts", "your-chatlog-alerts-channelid-here");

        this.save();
    }

    public String getDiscordToken() {
        return getString("networkmanagerbot.discord.token");
    }

    public String getGuildID() {
        return getString("networkmanagerbot.discord.guildId");
    }

    public String getCommandPrefix() {
        return getString("networkmanagerbot.discord.commandprefix");
    }

    public boolean isStatusEnabled() {
        return getBoolean("networkmanagerbot.discord.status.enabled");
    }

    public String getStatusType() {
        return getString("networkmanagerbot.discord.status.type");
    }

    public String getStatusMessage() {
        return getString("networkmanagerbot.discord.status.message");
    }

    public String getOwnerId() {
        return this.getString("networkmanagerbot.discord.ownerId");
    }

    public boolean isVerifyAddRole() {
        return this.getBoolean("networkmanagerbot.discord.verify.addrole.enabled");
    }

    public String getVerifyRole() {
        return this.getString("networkmanagerbot.discord.verify.addrole.role-name");
    }

    public boolean isSyncUserName() {
        return getBoolean("networkmanagerbot.discord.sync-username");
    }

    public boolean isSyncRanks() {
        return getBoolean("networkmanagerbot.discord.sync-ranks.enabled");
    }

    public List<String> getSyncRanks() {
        return getStringList("networkmanagerbot.discord.sync-ranks.ranks");
    }

    public boolean isPlayerListCommandEnabled() {
        return this.getBoolean("networkmanagerbot.discord.commands.playerlist.enabled");
    }

    public boolean isPlayersCommandEnabled() {
        return this.getBoolean("networkmanagerbot.discord.commands.players.enabled");
    }

    public String getStaffChatChannelID() {
        return getString("networkmanagerbot.discord.channels.staffchat");
    }

    public String getAdminChatChannelID() {
        return getString("networkmanagerbot.discord.channels.adminchat");
    }

    public String getTicketsChannelId() {
        return getString("networkmanagerbot.discord.channels.events.tickets");
    }

    public String getServerStatusEventChannelId() {
        return getString("networkmanagerbot.discord.channels.events.server-status");
    }

    public Map<String, String> get() {
        Map<String, String> map = Maps.newHashMap();
        for (String server : getConfiguration().getConfigurationSection("networkmanagerbot.discord.channels.events.server-status").getKeys(false)) {
            map.put(server, getString("networkmanagerbot.discord.channels.events.server-status." + server));
        }
        return map;
    }

    public String getHelpOPAlertsEventChannelId() {
        return getString("networkmanagerbot.discord.channels.events.helpop-alerts");
    }

    public String getPunishmentAlertsEventChannelId() {
        return getString("networkmanagerbot.discord.channels.events.punishment-alerts");
    }

    public String getReportAlertsEventChannelId() {
        return getString("networkmanagerbot.discord.channels.events.report-alerts");
    }

    public String getBugReportAlertsEventChannelId() {
        return getString("networkmanagerbot.discord.channels.events.bugreport-alerts");
    }

    public String getSuggestionAlertsEventChannelId() {
        return getString("networkmanagerbot.discord.channels.events.suggestion-alerts");
    }

    public String getChatLogEventChannelId() {
        return getString("networkmanagerbot.discord.channels.events.chatlog-alerts");
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}