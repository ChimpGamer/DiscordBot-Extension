package nl.chimpgamer.networkmanager.extensions.discordbot.configurations;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

public enum CommandSetting {

    DISCORD_PLAYERLIST_ENABLED("commands.discord.playerlist.enabled", true),
    DISCORD_PLAYERLIST_COMMAND("commands.discord.playerlist.command", "playerlist"),
    DISCORD_PLAYERS_ENABLED("commands.discord.players.enabled", true),
    DISCORD_PLAYERS_COMMAND("commands.discord.players.command", "players"),
    DISCORD_PLAYTIME_ENABLED("commands.discord.playtime.enabled", true),
    DISCORD_PLAYTIME_COMMAND("commands.discord.playtime.command", "playtime"),
    DISCORD_UPTIME_ENABLED("commands.discord.uptime.enabled", true),
    DISCORD_UPTIME_COMMAND("commands.discord.uptime.command", "uptime"),

    DISCORD_REGISTER_COMMAND("commands.discord.register.command", "register"),
    DISCORD_UNREGISTER_COMMAND("commands.discord.unregister.command", "unregister"),

    MINECRAFT_DISCORD_ENABLED("commands.minecraft.discord.enabled", false),
    MINECRAFT_SUGGESTION_ENABLED("commands.minecraft.suggestion.enabled", false),
    MINECRAFT_SUGGESTION_COMMAND("commands.minecraft.suggestion.command", "suggestion"),
    MINECRAFT_BUG_ENABLED("commands.minecraft.bug.enabled", false),
    MINECRAFT_BUG_COMMAND("commands.minecraft.bug.command", "bug"),

    MINECRAFT_VERIFY_COMMAND("commands.minecraft.verify.command", "verify"),

    ;

    private String path;
    private Object defaultValue;

    CommandSetting(String path, Object defaultValue) {
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
        return DiscordBot.getInstance().getCommandSettings().getString(this.getPath());
    }

    public boolean getAsBoolean() {
        return DiscordBot.getInstance().getCommandSettings().getBoolean(this.getPath());
    }
}