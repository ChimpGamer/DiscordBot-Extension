package nl.chimpgamer.networkmanager.extensions.discordbot;

import lombok.Getter;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;

import nl.chimpgamer.networkmanager.bungeecord.NetworkManager;
import nl.chimpgamer.networkmanager.common.utils.Methods;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc.BugCommand;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc.NetworkManagerBotCommand;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc.SuggestionCommand;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc.VerifyCommand;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.DiscordListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.NetworkManagerListeners;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.JoinLeaveListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.RedisBungeeListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordUserManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.*;
import nl.chimpgamer.networkmanager.api.NMListener;
import nl.chimpgamer.networkmanager.api.extensions.NMExtension;
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token;

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

    @Override
    public void onEnable() {
        // Extension startup logic
        instance = this;

        if (!Methods.isBungee()) {
            this.getLogger().severe("Hey, this NetworkManager extension is for BungeeCord only!");
            return;
        }

        this.loadDependencies();

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
        this.getNetworkManager().registerListener(new JoinLeaveListener(this));
    }

    private void registerCommands() {
        this.getNetworkManager().getCommandManager().registerCommands(this.getInfo().getName(),
                new VerifyCommand(this, "verify"),
                new BugCommand(this, "bug"),
                new SuggestionCommand(this, "suggestion"),
                new NetworkManagerBotCommand(this, "networkmanagerbot")
        );
    }

    private void unregisterListeners() {
        for (NMListener listener : this.getListeners()) {
            this.getEventHandler().unregisterListener(listener);
        }
    }

    private void loadDependencies() {
        this.getNetworkManager().getDependencyLoader().downloadLib(
                "http://central.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.3.10/kotlin-stdlib-1.3.10.jar",
                "Kotlin StdLib 1.3.10",
                "Kotlin Standard Library for JVM",
                "kotlin-stdlib-1.3.10"
        );
        this.getNetworkManager().getDependencyLoader().downloadLib(
                "http://central.maven.org/maven2/com/squareup/okhttp3/okhttp/3.14.0/okhttp-3.14.0.jar",
                "OkHttp 3.14.0",
                "OkHttp",
                "okhttp-3.14.0"
        );
        this.getNetworkManager().getDependencyLoader().downloadLib(
                "http://central.maven.org/maven2/com/squareup/okio/okio/2.2.2/okio-2.2.2.jar",
                "OkIo 2.2.2",
                "A modern I/O API for Java",
                "okio-2.2.2"
        );
        this.getNetworkManager().getDependencyLoader().downloadLib(
                "https://jcenter.bintray.com/com/neovisionaries/nv-websocket-client/2.6/nv-websocket-client-2.6.jar",
                "WebSecket Client 2.6",
                "WebSocket client implementation in Java.",
                "nv-websocket-client-2.6"
        );
        this.getNetworkManager().getDependencyLoader().downloadLib(
                "http://central.maven.org/maven2/org/apache/commons/commons-collections4/4.0/commons-collections4-4.0.jar",
                "Commons Collections 4.4.0",
                "The Apache Commons Collections package contains types that extend and augment the Java Collections Framework.",
                "commons-collections4-4.0"
        );
        this.getNetworkManager().getDependencyLoader().downloadLib(
                "http://central.maven.org/maven2/org/json/json/20180813/json-20180813.jar",
                "Json 20180813",
                "JSON is a light-weight, language independent, data interchange format. See http://www.JSON.org/ The files in this package implement JSON encoders/decoders in Java. It also includes the capability to convert between JSON and XML, HTTP headers, Cookies, and CDL. This is a reference implementation. There is a large number of JSON packages in Java. Perhaps someday the Java community will standardize on one. Until then, choose carefully.",
                "json-20180813"
        );
        this.getNetworkManager().getDependencyLoader().downloadLib(
                "http://central.maven.org/maven2/net/sf/trove4j/trove4j/3.0.3/trove4j-3.0.3.jar",
                "Trove4j 3.0.3",
                "The Trove library provides high speed regular and primitive collections for Java.",
                "trove4j-3.0.3"
        );
        this.getNetworkManager().getDependencyLoader().downloadLib(
                "https://dl.bintray.com/dv8fromtheworld/maven/net/dv8tion/JDA/3.8.3_460/JDA-3.8.3_460.jar",
                "JDA 3.8.3_462",
                "A wrapping of the Discord REST api and its Websocket-Events for Java.",
                "JDA-3.8.3_462"
        );
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
            this. getNetworkManager().debug("&c|  &7" + ex.getMessage());
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

    public Guild getGuild() {
        return this.getJDA().getStatus().equals(JDA.Status.CONNECTED) ? this.getJDA().getGuildById(this.getConfigManager().getGuildID()) : null;
    }

    private JDA getJDA() {
        return jda;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return (NetworkManager) super.getNetworkManager();
    }

    public static DiscordBot getInstance() {
        return instance;
    }
}