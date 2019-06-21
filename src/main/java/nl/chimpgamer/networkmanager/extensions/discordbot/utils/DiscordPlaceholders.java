package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.api.placeholders.PlaceholderHook;

public class DiscordPlaceholders extends PlaceholderHook {
    private final DiscordBot discordBot;

    public DiscordPlaceholders(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        switch (identifier.toLowerCase()) {
            case "users":
                return String.valueOf(this.getDiscordBot().getDiscordUserManager().getDiscordUsers().size());
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "discordbot";
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}