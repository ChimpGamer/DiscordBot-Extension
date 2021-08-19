package nl.chimpgamer.networkmanager.extensions.discordbot

import io.github.slimjar.app.builder.ApplicationBuilder
import net.dv8tion.jda.api.entities.Guild
import nl.chimpgamer.networkmanager.api.extensions.NMExtension
import nl.chimpgamer.networkmanager.api.utils.PlatformType
import nl.chimpgamer.networkmanager.bungeecord.NetworkManager
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc.*
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSettings
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Messages
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Settings
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.NetworkManagerListeners
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.ChatListener
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.JoinLeaveListener
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee.RedisBungeeListener
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
        if (networkManager.platformType !== PlatformType.BUNGEECORD) {
            logger.severe("Hey, this NetworkManager extension is for BungeeCord only!")
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
        ActivityUpdateTask(this).start()
        if (networkManager.isRedisBungee) {
            networkManager.registerListener(RedisBungeeListener(this))
            networkManager.redisBungee.registerPubSubChannels("NetworkManagerDiscordBot")
        }
        networkManager.placeholderManager.registerPlaceholder(DiscordPlaceholders(this))
    }

    public override fun onDisable() { // Extension shutdown logic
        expireTokens()

        networkManager.commandManager.unregisterAllBySource(info.name)
        this.discordManager.shutdownJDA()
    }

    override fun onConfigsReload() {
        this.settings.reload()
        this.commandSettings.reload()
        this.messages.reload()
    }

    private fun registerListeners() {
        NetworkManagerListeners(this)
        networkManager.registerListeners(
                JoinLeaveListener(this),
                ChatListener(this)
        )
    }

    private fun registerCommands() {
        val commandManager = networkManager.commandManager
        if (commandSettings.getBoolean(CommandSetting.MINECRAFT_BUG_ENABLED)) {
            commandManager.registerCommand(info.name, BugCommand(this, commandSettings.getString(CommandSetting.MINECRAFT_BUG_COMMAND)))
        }
        if (commandSettings.getBoolean(CommandSetting.MINECRAFT_SUGGESTION_ENABLED)) {
            commandManager.registerCommand(info.name, SuggestionCommand(this, commandSettings.getString(CommandSetting.MINECRAFT_SUGGESTION_COMMAND)))
        }
        if (commandSettings.getBoolean(CommandSetting.MINECRAFT_DISCORD_ENABLED)) {
            commandManager.registerCommand(info.name, DiscordCommand(this, "discord"))
        }
        commandManager.registerCommands(info.name,
                RegisterCommand(this, commandSettings.getString(CommandSetting.MINECRAFT_REGISTER_COMMAND), listOf(commandSettings.getString(CommandSetting.MINECRAFT_REGISTER_ALIASES))),
                UnregisterCommand(this, commandSettings.getString(CommandSetting.MINECRAFT_UNREGISTER_COMMAND), listOf(commandSettings.getString(CommandSetting.MINECRAFT_UNREGISTER_ALIASES))),
                NetworkManagerBotCommand(this, "networkmanagerbot")
        )
    }

    fun sendRedisBungee(message: String) {
        this.scheduler.runAsync({ networkManager.redisBungee.sendChannelMessage("NetworkManagerDiscordBot", message) }, false)
    }

    private fun expireTokens() {
        for (token in this.discordUserManager.tokens) {
            this.scheduler.runSync(TokenExpiryTask(this, token))
        }
    }

    val guild: Guild
        get() = this.discordManager.guild

    override val networkManager: NetworkManager
        get() = super.networkManager as NetworkManager

    companion object {
        @JvmStatic
        lateinit var instance: DiscordBot
    }
}