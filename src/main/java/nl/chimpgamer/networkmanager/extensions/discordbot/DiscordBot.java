package nl.chimpgamer.networkmanager.extensions.discordbot;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import nl.chimpgamer.networkmanager.api.NMListener;
import nl.chimpgamer.networkmanager.api.extensions.NMExtension;
import nl.chimpgamer.networkmanager.api.utils.PlatformType;
import nl.chimpgamer.networkmanager.bungeecord.NetworkManager;
import nl.chimpgamer.networkmanager.bungeecord.commands.NMCommandManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc.*;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSettings;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Messages;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Settings;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.NetworkManagerListeners;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.ChatListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.JoinLeaveListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.RedisBungeeListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordUserManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.TokenExpiryTask;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DependencyDownloader;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DiscordPlaceholders;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.MySQL;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class DiscordBot extends NMExtension {
    private static DiscordBot instance;

    private List<Integer> tasks;
    private List<NMListener> listeners;

    // Configuration files
    private Settings settings;
    private CommandSettings commandSettings;
    private Messages messages;

    /*private ConfigManager configManager;
    private MessagesConfigManager messagesConfigManager;*/
    private MySQL mySQL;
    private DiscordUserManager discordUserManager;
    private DiscordManager discordManager;

    public static DiscordBot getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Extension startup logic
        instance = this;

        if (this.getNetworkManager().getPlatformType() != PlatformType.BUNGEECORD) {
            this.getLogger().severe("Hey, this NetworkManager extension is for BungeeCord only!");
            return;
        }
        DependencyDownloader dd = new DependencyDownloader(this);
        dd.downloadDependency(
                "https://github.com/DV8FromTheWorld/JDA/releases/download/v4.0.0/JDA-4.0.0_39-withDependencies-no-opus.jar",
                "JDA",
                "JDA-4.0.0_39-withDependencies-no-opus");

        this.tasks = new ArrayList<>();
        this.listeners = new ArrayList<>();

        // Initialize configuration files
        this.initSettings();
        this.initCommands();
        this.initMessages();

        this.mySQL = new MySQL(this);
        this.discordUserManager = new DiscordUserManager(this);
        this.discordUserManager.load();

        this.discordManager = new DiscordManager(this);
        if (!this.discordManager.init()) {
            this.disable();
            return;
        }

        this.registerCommands();
        this.registerListeners();

        if (this.getNetworkManager().isRedisBungee()) {
            this.getNetworkManager().registerListener(new RedisBungeeListener(this));
            this.getNetworkManager().getRedisBungee().registerPubSubChannels("NetworkManagerDiscordBot");
        }

        this.getNetworkManager().getPlaceholderManager().registerPlaceholder(new DiscordPlaceholders(this));
    }

    @Override
    public void onDisable() {
        // Extension shutdown logic
        this.expireTokens();
        this.unregisterListeners();
        this.getNetworkManager().getCommandManager().unregisterAllBySource(this.getInfo().getName());
        this.getDiscordManager().shutdownJDA();
    }

    @Override
    protected void onConfigsReload() {
        this.getSettings().reload();
        this.getCommandSettings().reload();
        this.getMessages().reload();
    }

    private void initSettings() {
        this.settings = new Settings(this);
        this.settings.init();
    }

    private void initCommands() {
        this.commandSettings = new CommandSettings(this);
        this.commandSettings.init();
    }

    private void initMessages() {
        this.messages = new Messages(this);
        this.messages.init();
    }

    private void registerListeners() {
        this.getListeners().add(new NetworkManagerListeners(this));
        for (NMListener listener : this.getListeners()) {
            this.getEventHandler().registerListener(listener);
        }
        this.getNetworkManager().registerListeners(
                new JoinLeaveListener(this),
                new ChatListener(this)
        );
    }

    private void registerCommands() {
        NMCommandManager commandManager = this.getNetworkManager().getCommandManager();
        if (CommandSetting.MINECRAFT_BUG_ENABLED.getAsBoolean()) {
            commandManager.registerCommand(this.getInfo().getName(), new BugCommand(this, CommandSetting.MINECRAFT_BUG_COMMAND.getAsString()));
        }
        if (CommandSetting.MINECRAFT_SUGGESTION_ENABLED.getAsBoolean()) {
            commandManager.registerCommand(this.getInfo().getName(), new SuggestionCommand(this, CommandSetting.MINECRAFT_SUGGESTION_COMMAND.getAsString()));
        }
        if (CommandSetting.MINECRAFT_DISCORD_ENABLED.getAsBoolean()) {
            commandManager.registerCommand(this.getInfo().getName(), new DiscordCommand(this, "discord"));
        }
        commandManager.registerCommands(this.getInfo().getName(),
                new VerifyCommand(this, CommandSetting.MINECRAFT_VERIFY_COMMAND.getAsString()),
                new NetworkManagerBotCommand(this, "networkmanagerbot")
        );
    }

    private void unregisterListeners() {
        for (NMListener listener : this.getListeners()) {
            this.getEventHandler().unregisterListener(listener);
        }
    }

    public void sendRedisBungee(String message) {
        this.getScheduler().runAsync(() -> this.getNetworkManager().getRedisBungee().sendChannelMessage("NetworkManagerDiscordBot", message), false);
    }

    private void expireTokens() {
        if (this.getDiscordUserManager() != null) {
            for (Token token : this.getDiscordUserManager().getTokens()) {
                this.getScheduler().runSync(new TokenExpiryTask(this, token));
            }
        }
    }

    public @NonNull Guild getGuild() {
        return this.getDiscordManager().getGuild();
    }

    @Override
    public NetworkManager getNetworkManager() {
        return (NetworkManager) super.getNetworkManager();
    }
}