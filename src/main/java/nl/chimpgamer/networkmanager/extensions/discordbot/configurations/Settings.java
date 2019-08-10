package nl.chimpgamer.networkmanager.extensions.discordbot.configurations;

import nl.chimpgamer.networkmanager.api.utils.FileUtils;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import java.io.IOException;

public class Settings extends FileUtils {
    private final DiscordBot discordBot;

    public Settings(DiscordBot discordBot) {
        super(discordBot.getDataFolder().getAbsolutePath(), "settings.yml");
        this.discordBot = discordBot;
    }

    public void init() {
        this.setupFile();

        for (Setting setting : Setting.values()) {
            this.addDefault(setting.getPath(), setting.getDefaultValue());
        }
        this.copyDefaults(true);
        this.save();
    }

    private void setupFile() {
        if (!this.getFile().exists()) {
            try {
                this.saveToFile(this.getDiscordBot().getResource("settings.yml"));
            } catch (NullPointerException ex) {
                try {
                    this.getFile().createNewFile();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void reload() {
        super.reload();
        this.getDiscordBot().getDiscordManager().setVerifiedRole(Utils.getRoleByName(Setting.DISCORD_VERIFY_ADD_ROLE_ROLE_NAME.getAsString()));
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}