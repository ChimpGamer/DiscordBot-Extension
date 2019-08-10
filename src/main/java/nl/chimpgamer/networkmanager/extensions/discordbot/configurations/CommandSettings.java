package nl.chimpgamer.networkmanager.extensions.discordbot.configurations;

import nl.chimpgamer.networkmanager.api.utils.FileUtils;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

import java.io.IOException;

public class CommandSettings extends FileUtils {
    private final DiscordBot discordBot;

    public CommandSettings(DiscordBot discordBot) {
        super(discordBot.getDataFolder().getAbsolutePath(), "commands.yml");
        this.discordBot = discordBot;
    }

    public void init() {
        this.setupFile();

        for (CommandSetting commandSetting : CommandSetting.values()) {
            this.addDefault(commandSetting.getPath(), commandSetting.getDefaultValue());
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