package nl.chimpgamer.networkmanager.extensions.discordbot.shared

import net.dv8tion.jda.api.entities.Guild
import nl.chimpgamer.networkmanager.api.Scheduler
import nl.chimpgamer.networkmanager.common_proxy.plugin.NetworkManagerPluginProxyBase
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.commands.mc.*
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Settings
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.CommandSettings
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Messages
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners.LiteBansListener
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners.NetworkManagerListeners
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.manager.DiscordManager
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.manager.DiscordUserManager
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks.ActivityUpdateTask
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks.TokenExpiryTask
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.DiscordPlaceholders
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.MySQL
import java.io.File

class DiscordBot(val platform: Platform) {
    val dataFolder: File
        get() = platform.dataFolder

    val scheduler: Scheduler
        get() = platform.scheduler

    val networkManager: NetworkManagerPluginProxyBase
        get() = platform.networkManager as NetworkManagerPluginProxyBase

    // Configuration files
    val settings = Settings(this)
    val commandSettings = CommandSettings(this)
    lateinit var messages: Messages

    val mySQL = MySQL(this)
    val discordUserManager = DiscordUserManager(this)
    lateinit var discordManager: DiscordManager

    private val activityUpdateTask = ActivityUpdateTask(this)

    private var liteBansListener: LiteBansListener? = null

    val guild: Guild
        get() = this.discordManager.guild

    fun isDiscordManagerInitialized(): Boolean = this::discordManager.isInitialized

    fun enable() {
        if (platform.networkManager.platformType.isProxy.not()) {
            platform.error("Hey, this NetworkManager extension is for BungeeCord and Velocity only!")
            platform.disable()
            return
        }

        if (mySQL.initialize().not()) {
            platform.disable()
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
            platform.disable()
            return
        }

        registerCommands()
        registerListeners()
        activityUpdateTask.start()
        networkManager.placeholderManager.registerPlaceholder(DiscordPlaceholders(this))

        if (networkManager.isPluginEnabled("LiteBans")) {
            liteBansListener = LiteBansListener(this)
            liteBansListener?.registerListeners()
            platform.info("Successfully hooked into LiteBans!")
        }
    }

    fun disable() {
        expireTokens()
        activityUpdateTask.stop()
        liteBansListener?.unregisterListeners()

        this.discordManager.shutdownJDA()
    }

    fun reloadConfigs() {
        this.settings.reload()
        this.commandSettings.reload()
        this.messages.reload()
    }

    private fun registerListeners() {
        NetworkManagerListeners(this)
    }

    private fun registerCommands() {
        if (commandSettings.getBoolean(CommandSetting.MINECRAFT_BUG_ENABLED)) {
            platform.cloudCommandManager.commandManager.command(
                CloudBugCommand(this)
                .getCommand(commandSettings.getString(CommandSetting.MINECRAFT_BUG_COMMAND)))
        }
        if (commandSettings.getBoolean(CommandSetting.MINECRAFT_SUGGESTION_ENABLED)) {
            platform.cloudCommandManager.commandManager.command(
                CloudSuggestionCommand(this)
                .getCommand(commandSettings.getString(CommandSetting.MINECRAFT_SUGGESTION_COMMAND)))
        }
        if (commandSettings.getBoolean(CommandSetting.MINECRAFT_DISCORD_ENABLED)) {
            platform.cloudCommandManager.commandManager.command(CloudDiscordCommand(this).getCommand("discord"))
        }

        platform.cloudCommandManager.commandManager.command(
            CloudRegisterCommand(this)
            .getCommand(commandSettings.getString(CommandSetting.MINECRAFT_REGISTER_COMMAND), *commandSettings.getString(CommandSetting.MINECRAFT_REGISTER_ALIASES).split(", ").toTypedArray()))
        platform.cloudCommandManager.commandManager.command(
            CloudUnregisterCommand(this)
            .getCommand(commandSettings.getString(CommandSetting.MINECRAFT_UNREGISTER_COMMAND), *commandSettings.getString(CommandSetting.MINECRAFT_UNREGISTER_ALIASES).split(", ").toTypedArray()))
        platform.cloudCommandManager.annotationParser.parse(CloudNetworkManagerBotCommand(this))
    }

    private fun expireTokens() {
        for (token in this.discordUserManager.tokens) {
            this.scheduler.runSync(TokenExpiryTask(this, token))
        }
    }
}