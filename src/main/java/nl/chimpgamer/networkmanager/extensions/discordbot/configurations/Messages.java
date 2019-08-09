package nl.chimpgamer.networkmanager.extensions.discordbot.configurations;

import nl.chimpgamer.networkmanager.api.utils.FileUtils;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

import java.io.IOException;

public class Messages extends FileUtils {
    private final DiscordBot discordBot;

    public Messages(DiscordBot discordBot) {
        super(discordBot.getDataFolder().getAbsolutePath(), "messages.yml");
        this.discordBot = discordBot;
    }

    public void init() {
        this.setupFile();
    }

    private void setupFile() {
        if (!this.getFile().exists()) {
            try {
                this.saveToFile(this.getDiscordBot().getResource("messages.yml"));
            } catch (NullPointerException ex) {
                try {
                    this.getFile().createNewFile();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
            }
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}