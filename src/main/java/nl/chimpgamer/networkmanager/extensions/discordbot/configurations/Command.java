package nl.chimpgamer.networkmanager.extensions.discordbot.configurations;

public enum Command {

    DISCORD_PLAYERLIST_ENABLED("commands.discord.playerlist.enabled", true),
    DISCORD_PLAYERS_ENABLED("commands.discord.players.enabled", true),
    DISCORD_PLAYTIME_ENABLED("commands.discord.playtime.enabled", true),

    MINECRAFT_DISCORD_ENABLED("commands.minecraft.discord.enabled", false),
    MINECRAFT_SUGGESTION_ENABLED("commands.minecraft.suggestion.enabled", false),
    MINECRAFT_BUG_ENABLED("commands.minecraft.bug.enabled", false),

    ;

    private String path;
    private Object defaultValue;

    Command(String path, Object defaultValue) {
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