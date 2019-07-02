package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.api.utils.Config;

import java.io.*;
import java.nio.file.Files;

public class MessagesConfigManager extends Config {
    private final DiscordBot discordBot;

    public MessagesConfigManager(DiscordBot discordBot) {
        super(discordBot.getDataFolder() + File.separator + "messages.yml");
        this.discordBot = discordBot;
        InputStream orgFile = getDiscordBot().getResource("messages.yml");
        if (this.getFile().length() == 0) {
            Utils.copyInputStreamToFile(orgFile, this.getFile());
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}