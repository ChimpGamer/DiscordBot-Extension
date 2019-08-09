package nl.chimpgamer.networkmanager.extensions.discordbot.configurations;

import java.util.Arrays;

public enum Setting {

    DISCORD_TOKEN("bot.discord.token", "YourBotTokenHere"),
    DISCORD_COMMAND_PREFIX("bot.discord.command_prefix", "!"),
    DISCORD_STATUS_ENABLED("bot.discord.status.enabled", true),
    DISCORD_STATUS_TYPE("bot.discord.status.type", "watching"),
    DISCORD_STATUS_MESSAGE("bot.discord.status.message", "players on Hypixel"),
    DISCORD_OWNER_ID("bot.discord.ownerId", "000000000000000000"),

    DISCORD_VERIFY_ADD_ROLE_ENABLED("bot.discord.verify.addRole.enabled", false),
    DISCORD_VERIFY_ADD_ROLE_ROLE_NAME("bot.discord.verify.addRole.roleName", "YourVerifiedRoleNameHere"),

    DISCORD_SYNC_USERNAME("bot.discord.sync.username", false),
    DISCORD_SYNC_RANKS_ENABLED("bot.discord.sync.ranks.enabled", false),
    DISCORD_SYNC_RANKS_LIST("bot.discord.sync.ranks.list", Arrays.asList("VIP", "SuperVIP")),


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
}