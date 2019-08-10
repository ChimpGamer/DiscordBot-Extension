package nl.chimpgamer.networkmanager.extensions.discordbot.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum Setting {

    DISCORD_TOKEN("bot.discord.token", "YourBotTokenHere"),
    DISCORD_OWNER_ID("bot.discord.ownerId", "000000000000000000"),
    DISCORD_COMMAND_PREFIX("bot.discord.command_prefix", "!"),
    DISCORD_STATUS_ENABLED("bot.discord.status.enabled", true),
    DISCORD_STATUS_TYPE("bot.discord.status.type", "watching"),
    DISCORD_STATUS_MESSAGE("bot.discord.status.message", "players on Hypixel"),

    DISCORD_VERIFY_ADD_ROLE_ENABLED("bot.discord.verify.addRole.enabled", false),
    DISCORD_VERIFY_ADD_ROLE_ROLE_NAME("bot.discord.verify.addRole.roleName", "YourVerifiedRoleNameHere"),

    DISCORD_SYNC_USERNAME("bot.discord.sync.username", false),
    DISCORD_SYNC_RANKS_ENABLED("bot.discord.sync.ranks.enabled", false),
    DISCORD_SYNC_RANKS_LIST("bot.discord.sync.ranks.list", Arrays.asList("VIP", "SuperVIP")),

    DISCORD_EVENTS_STAFFCHAT_CHANNEL("bot.discord.events.staffchat.channel", "000000000000000000"),
    DISCORD_EVENTS_ADMINCHAT_CHANNEL("bot.discord.events.adminchat.channel", "000000000000000000"),

    DISCORD_EVENTS_TICKETS_CHANNEL("bot.discord.events.tickets.channel", "000000000000000000"),
    DISCORD_EVENTS_HELPOP_CHANNEL("bot.discord.events.helpop.channel", "000000000000000000"),
    DISCORD_EVENTS_PUNISHMENT_CHANNEL("bot.discord.events.punishment.channel", "000000000000000000"),
    DISCORD_EVENTS_REPORT_CHANNEL("bot.discord.events.report.channel", "000000000000000000"),
    DISCORD_EVENTS_CHATLOG_CHANNEL("bot.discord.events.chatlog.channel", "000000000000000000"),
    DISCORD_EVENTS_SUGGESTION_CHANNEL("bot.discord.events.suggestion.channel", "000000000000000000"),
    DISCORD_EVENTS_BUGREPORT_CHANNEL("bot.discord.events.bugreport.channel", "000000000000000000"),

    DISCORD_EVENTS_CHAT_CHANNELS("bot.discord.events.chat", ImmutableMap.of("all", "000000000000000000")),
    DISCORD_EVENTS_SERVERSTATUS_CHANNELS("bot.discord.events.serverStatus", ImmutableMap.of("all", "000000000000000000")),

    ;

    private String path;
    private Object defaultValue;

    Setting(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    public String getPath() {
        return path;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getAsString() {
        return DiscordBot.getInstance().getSettings().getString(this.getPath());
    }

    public boolean getAsBoolean() {
        return DiscordBot.getInstance().getSettings().getBoolean(this.getPath());
    }

    public List<String> getAsList() {
        return DiscordBot.getInstance().getSettings().getStringList(this.getPath());
    }

    public Map<String, String> getAsMap() {
        Map<String, String> map = Maps.newHashMap();
        for (String key : DiscordBot.getInstance().getSettings().getConfig().getConfigurationSection(this.getPath()).getKeys(false)) {
            map.put(key, DiscordBot.getInstance().getSettings().getString(this.getPath() + "." + key));
        }
        return map;
    }
}