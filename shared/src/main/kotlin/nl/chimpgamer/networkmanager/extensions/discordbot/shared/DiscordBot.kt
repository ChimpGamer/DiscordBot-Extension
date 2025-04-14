package nl.chimpgamer.networkmanager.extensions.discordbot.shared

import net.dv8tion.jda.api.entities.Guild
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin
import nl.chimpgamer.networkmanager.api.Scheduler
import nl.chimpgamer.networkmanager.api.models.languages.Language
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.commands.mc.*
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.*
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners.LiteBansListener
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners.NetworkManagerListeners
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.manager.DiscordManager
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.manager.DiscordUserManager
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks.ActivityUpdateTask
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks.TokenExpiryTask
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.DiscordPlaceholders
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.MySQL
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils
import java.io.File

class DiscordBot(val platform: Platform) {
    val dataFolder: File
        get() = platform.dataFolder

    val scheduler: Scheduler
        get() = platform.scheduler

    val networkManager: NetworkManagerPlugin
        get() = platform.networkManager

    // Configuration files
    val settings = Settings(this)
    val commandSettings = CommandSettings(this)
    val messages = Messages(this)

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
        instance = this
        Utils.initialize(this)

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
        discordManager.setOnlineStatus()
    }

    private fun registerListeners() {
        NetworkManagerListeners(this)
    }

    private fun registerCommands() {
        val commandManager = platform.cloudCommandManager.commandManager
        if (commandSettings.minecraftBugEnabled) {
            CloudBugCommand(this).registerCommands(commandManager, commandSettings.minecraftBugCommand)
        }
        if (commandSettings.minecraftSuggestionEnabled) {
            CloudSuggestionCommand(this)
                .registerCommands(commandManager, commandSettings.minecraftSuggestionCommand)
        }
        if (commandSettings.minecraftDiscordEnabled) {
            CloudDiscordCommand(this).registerCommands(commandManager, "discord")
        }

        if (commandSettings.minecraftRegisterEnabled) {
            CloudRegisterCommand(this)
                .registerCommands(commandManager, commandSettings.minecraftRegisterCommand, *commandSettings.minecraftRegisterAlias.split(", ").toTypedArray())
        }
        if (commandSettings.minecraftUnregisterEnabled) {
            CloudUnregisterCommand(this)
                .registerCommands(commandManager, commandSettings.minecraftUnregisterCommand, *commandSettings.minecraftUnregisterAlias.split(", ").toTypedArray())
        }

        CloudNetworkManagerBotCommand(this).registerCommands(commandManager)
    }

    private fun expireTokens() {
        for (token in this.discordUserManager.tokens) {
            this.scheduler.runSync(TokenExpiryTask(this, token))
        }
    }

    fun getDefaultLanguage(): Language {
        return networkManager.cacheManager.cachedLanguages.getDefaultLanguage()
    }

    companion object {
        lateinit var instance: DiscordBot
    }
}