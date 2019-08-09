package nl.chimpgamer.networkmanager.extensions.discordbot.configurations;

import nl.chimpgamer.networkmanager.api.utils.FileUtils;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

import java.io.IOException;

public class Commands extends FileUtils {
    private final DiscordBot discordBot;

    public Commands(DiscordBot discordBot) {
        super(discordBot.getDataFolder().getAbsolutePath(), "commands.yml");
        this.discordBot = discordBot;
    }

    public void init() {
        this.setupFile();

        for (Command command : Command.values()) {
            this.addDefault(command.getPath(), command.getDefaultValue());
        }
        this.copyDefaults(true);
        this.save();
    }

    private void setupFile() {
        if (!this.getFile().exists()) {
            try {
                this.saveToFile(this.getDiscordBot().getResource("commands.yml"));
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