package nl.chimpgamer.networkmanager.extensions.discordbot

import io.github.slimjar.app.builder.ApplicationBuilder
import net.dv8tion.jda.api.entities.Guild
import nl.chimpgamer.networkmanager.api.extensions.NMExtension
import nl.chimpgamer.networkmanager.api.utils.PlatformType
import nl.chimpgamer.networkmanager.common_proxy.plugin.NetworkManagerPluginProxyBase
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc.*
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSettings
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Messages
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Settings
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.NetworkManagerListeners
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.BungeeCordJoinLeaveListener
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.RedisBungeeListener
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.velocity.VelocityJoinLeaveListener
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordManager
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordUserManager
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.ActivityUpdateTask
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.TokenExpiryTask
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DiscordPlaceholders
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.MySQL
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.logging.Level

class DiscordBot : NMExtension() {

    // Configuration files
    val settings = Settings(this)
    val commandSettings = CommandSettings(this)
    lateinit var messages: Messages

    val mySQL = MySQL(this)
    val discordUserManager = DiscordUserManager(this)
    lateinit var discordManager: DiscordManager

    private val activityUpdateTask = ActivityUpdateTask(this)

    public override fun onEnable() { // Extension startup logic
        val dependencyDirectory = File(networkManagerPlugin.dataFolder, "libraries")
        logger.log(Level.INFO, "Loading Libraries...")
        logger.log(Level.INFO, "Note: This might take a few minutes on first run. Kindly ensure internet connectivity.")
        val startInstant = Instant.now()
        try {
            val slimJarJsonUrl = javaClass.classLoader.getResource("slimjar.json")
            ApplicationBuilder
                .appending("DiscordBot")
                .dependencyFileUrl(slimJarJsonUrl)
                .downloadDirectoryPath(dependencyDirectory.toPath())
                .logger { s, anies -> logger.log(Level.INFO, s, anies) }
                .build()
            val endInstant = Instant.now()
            val timeTaken = Duration.between(startInstant, endInstant).toMillis()
            val timeTakenSeconds = timeTaken / 1000.0
            logger.log(Level.INFO, "Loaded libraries in {0} seconds", timeTakenSeconds)
        } catch (exception: Exception) {
            logger.log(
                Level.SEVERE,
                "Unable to load dependencies... Please ensure an active Internet connection on first run!"
            )
            exception.printStackTrace()
            disable()
            return
        }

        instance = this
        if (networkManager.platformType.isProxy.not()) {
            logger.severe("Hey, this NetworkManager extension is for BungeeCord and Velocity only!")
            return
        }

        // Initialize MySQL. If False stop plugin.
        if (mySQL.initialize().not()) {
            disable()
            return
        }

        // Initialize configuration files
        settings.init()
        commandSettings.init()
        messages = Messages(this)
        messages.init()

        discordUserManager.load()

        discordManager = DiscordManager(this)
        if (!discordManager.init()) {
            disable()
            return
        }

        registerCommands()
        registerListeners()
        activityUpdateTask.start()
        if (networkManager.isRedisBungee) {
            networkManager.registerListener(RedisBungeeListener(this))
        }
        networkManager.placeholderManager.registerPlaceholder(DiscordPlaceholders(this))
    }

    public override fun onDisable() { // Extension shutdown logic
        expireTokens()
        activityUpdateTask.stop()

        this.discordManager.shutdownJDA()
    }

    override fun onConfigsReload() {
        this.settings.reload()
        this.commandSettings.reload()
        this.messages.reload()
    }

    private fun registerListeners() {
        NetworkManagerListeners(this)
        if (networkManager.platformType === PlatformType.BUNGEECORD) {
            networkManager.registerListeners(
                BungeeCordJoinLeaveListener(this)
            )
        } else if (networkManager.platformType === PlatformType.VELOCITY) {
            networkManager.registerListeners(
                VelocityJoinLeaveListener(this)
            )
        }
    }

    private fun registerCommands() {
        val cloudCommandManager = networkManager.cloudCommandManager

        if (commandSettings.getBoolean(CommandSetting.MINECRAFT_BUG_ENABLED)) {
            cloudCommandManager.commandManager.command(CloudBugCommand(this)
                .getCommand(commandSettings.getString(CommandSetting.MINECRAFT_BUG_COMMAND)))
        }
        if (commandSettings.getBoolean(CommandSetting.MINECRAFT_SUGGESTION_ENABLED)) {
            cloudCommandManager.commandManager.command(CloudSuggestionCommand(this)
                .getCommand(commandSettings.getString(CommandSetting.MINECRAFT_SUGGESTION_COMMAND)))
        }
        if (commandSettings.getBoolean(CommandSetting.MINECRAFT_DISCORD_ENABLED)) {
            cloudCommandManager.commandManager.command(CloudDiscordCommand(this).getCommand("discord"))
        }

        cloudCommandManager.commandManager.command(CloudRegisterCommand(this)
            .getCommand(commandSettings.getString(CommandSetting.MINECRAFT_REGISTER_COMMAND), *commandSettings.getString(CommandSetting.MINECRAFT_REGISTER_ALIASES).split(", ").toTypedArray()))
        cloudCommandManager.commandManager.command(CloudUnregisterCommand(this)
            .getCommand(commandSettings.getString(CommandSetting.MINECRAFT_UNREGISTER_COMMAND), *commandSettings.getString(CommandSetting.MINECRAFT_UNREGISTER_ALIASES).split(", ").toTypedArray()))
        cloudCommandManager.annotationParser.parse(CloudNetworkManagerBotCommand(this))
    }

    private fun expireTokens() {
        for (token in this.discordUserManager.tokens) {
            this.scheduler.runSync(TokenExpiryTask(this, token))
        }
    }

    val guild: Guild
        get() = this.discordManager.guild

    override val networkManager: NetworkManagerPluginProxyBase
        get() = super.networkManager as NetworkManagerPluginProxyBase

    fun isDiscordManagerInitialized(): Boolean = this::discordManager.isInitialized

    companion object {
        @JvmStatic
        lateinit var instance: DiscordBot
    }
}