package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import nl.chimpgamer.networkmanager.api.utils.FileUtils;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

import java.io.*;

public class MessagesConfigManager extends FileUtils {

    public MessagesConfigManager(DiscordBot discordBot) {
        super(discordBot.getDataFolder().getPath(), "messages.yml");
        InputStream orgFile = discordBot.getResource("messages.yml");
        if (!this.getFile().exists()) {
            Utils.copyInputStreamToFile(orgFile, this.getFile());
        }
    }
}