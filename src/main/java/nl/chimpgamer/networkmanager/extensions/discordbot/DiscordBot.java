package nl.chimpgamer.networkmanager.extensions.discordbot;

import lombok.Getter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import nl.chimpgamer.networkmanager.api.NMListener;
import nl.chimpgamer.networkmanager.api.extensions.NMExtension;
import nl.chimpgamer.networkmanager.api.manager.CommandManager;
import nl.chimpgamer.networkmanager.bungeecord.NetworkManager;
import nl.chimpgamer.networkmanager.common.utils.Methods;
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc.*;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.DiscordListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.NetworkManagerListeners;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.ChatListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.JoinLeaveListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.RedisBungeeListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordUserManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class DiscordBot extends NMExtension {
    private static DiscordBot instance;

    private List<Integer> tasks;
    private List<NMListener> listeners;

    private ConfigManager configManager;
    private MessagesConfigManager messagesConfigManager;
    private MySQL mySQL;
    private DiscordUserManager discordUserManager;
    private DiscordManager discordManager;
    private JDA jda;

    public static DiscordBot getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Extension startup logic
        instance = this;

        if (!Methods.isBungee()) {
            this.getLogger().severe("Hey, this NetworkManager extension is for BungeeCord only!");
            return;
        }
        DependencyDownloader dd = new DependencyDownloader(this);
        dd.downloadDependency(
                "https://github.com/DV8FromTheWorld/JDA/releases/download/v3.8.3/JDA-3.8.3_464-withDependencies-no-opus.jar",
                "JDA",
                "JDA-3.8.3_464-withDependencies-no-opus");

        this.tasks = new ArrayList<>();
        this.listeners = new ArrayList<>();

        this.configManager = new ConfigManager(this);
        this.messagesConfigManager = new MessagesConfigManager(this);
        this.mySQL = new MySQL(this);
        this.discordUserManager = new DiscordUserManager(this);
        this.discordUserManager.load();

        this.discordManager = new DiscordManager(this);
        this.discordManager.init();

        if (!this.loadJDA()) {
            this.disable();
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
        this.unregisterListeners();
        this.getNetworkManager().getCommandManager().unregisterAllBySource(this.getInfo().getName());
        this.stopJDA();
    }

    @Override
    protected void onConfigsReload() {
        this.getConfigManager().reload();
        this.getMessagesConfigManager().reload();
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
        CommandManager commandManager = this.getNetworkManager().getCommandManager();
        if (this.getConfigManager().isMinecraftCommandEnabled("bug")) {
            commandManager.registerCommand(this.getInfo().getName(), new BugCommand(this, "bug"));
        }
        if (this.getConfigManager().isMinecraftCommandEnabled("suggestion")) {
            commandManager.registerCommand(this.getInfo().getName(), new SuggestionCommand(this, "suggestion"));
        }
        if (this.getConfigManager().isMinecraftCommandEnabled("discord")) {
            commandManager.registerCommand(this.getInfo().getName(), new DiscordCommand(this, "discord"));
        }
        commandManager.registerCommands(this.getInfo().getName(),
                new VerifyCommand(this, "verify"),
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

    private boolean loadJDA() {
        try {
            this.jda = new JDABuilder(AccountType.BOT)
                    .setToken(getConfigManager().getDiscordToken())
                    .setAutoReconnect(true)
                    .addEventListener(new DiscordListener(this))
                    .addEventListener(this.getDiscordManager().getCommandClientBuilder().build())
                    .build();
            return true;
        } catch (LoginException ex) {
            this.getNetworkManager().debug("&c|  &7" + ex.getMessage());
            return false;
        }
    }

    private void stopJDA() {
        if (this.getJDA() != null) {
            this.expireTokens();
            this.getJDA().shutdown();
        }
    }

    public boolean restartJDA() {
        this.stopJDA();
        return this.loadJDA();
    }

    private void expireTokens() {
        if (this.getDiscordUserManager() != null) {
            for (Token token : this.getDiscordUserManager().getTokens()) {
                Utils.editMessage(token.getMessage(), ":x: Token has been expired. Ask me for a new one :D :x:");
            }
        }
    }

    public @NonNull Guild getGuild() {
        return this.getJDA().getGuildById(this.getConfigManager().getGuildID());
    }

    private JDA getJDA() {
        return jda;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return (NetworkManager) super.getNetworkManager();
    }
}