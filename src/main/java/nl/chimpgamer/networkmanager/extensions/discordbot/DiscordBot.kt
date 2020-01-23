package nl.chimpgamer.networkmanager.extensions.discordbot

import net.dv8tion.jda.api.entities.Guild
import nl.chimpgamer.networkmanager.api.NMListener
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
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.TokenExpiryTask
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DependencyDownloader
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DiscordPlaceholders
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.MySQL
import kotlin.collections.ArrayList

class DiscordBot : NMExtension() {
    private var listeners: MutableList<NMListener> = ArrayList()
    // Configuration files
    lateinit var settings: Settings
    lateinit var commandSettings: CommandSettings
    lateinit var messages: Messages

    lateinit var mySQL: MySQL
    lateinit var discordUserManager: DiscordUserManager
    lateinit var discordManager: DiscordManager
    public override fun onEnable() { // Extension startup logic
        instance = this
        if (networkManager.platformType != PlatformType.BUNGEECORD) {
            logger.severe("Hey, this NetworkManager extension is for BungeeCord only!")
            return
        }
        val dd = DependencyDownloader(this)
        dd.downloadDependency(
                "https://github.com/DV8FromTheWorld/JDA/releases/download/v4.0.0/JDA-4.0.0_39-withDependencies-no-opus.jar",
                "JDA",
                "JDA-4.0.0_39-withDependencies-no-opus")
        listeners = ArrayList()
        // Initialize configuration files
        initSettings()
        initCommands()
        initMessages()
        mySQL = MySQL(this)
        discordUserManager = DiscordUserManager(this)
        discordUserManager.load()
        discordManager = DiscordManager(this)
        if (!discordManager.init()) {
            disable()
            return
        }
        registerCommands()
        registerListeners()
        if (networkManager.isRedisBungee) {
            networkManager.registerListener(RedisBungeeListener(this))
            networkManager.redisBungee.registerPubSubChannels("NetworkManagerDiscordBot")
        }
        networkManager.placeholderManager.registerPlaceholder(DiscordPlaceholders(this))
    }

    public override fun onDisable() { // Extension shutdown logic
        expireTokens()
        unregisterListeners()
        networkManager.commandManager.unregisterAllBySource(info.name)
        this.discordManager.shutdownJDA()
    }

    override fun onConfigsReload() {
        this.settings.reload()
        this.commandSettings.reload()
        this.messages.reload()
    }

    private fun initSettings() {
        settings = Settings(this)
        settings.init()
    }

    private fun initCommands() {
        commandSettings = CommandSettings(this)
        commandSettings.init()
    }

    private fun initMessages() {
        messages = Messages(this)
        messages.init()
    }

    private fun registerListeners() {
        this.listeners.add(NetworkManagerListeners(this))
        for (listener in this.listeners) {
            this.eventHandler.registerListener(listener)
        }
        networkManager.registerListeners(
                JoinLeaveListener(this),
                ChatListener(this)
        )
    }

    private fun registerCommands() {
        val commandManager = networkManager.commandManager
        if (CommandSetting.MINECRAFT_BUG_ENABLED.asBoolean) {
            commandManager.registerCommand(info.name, BugCommand(this, CommandSetting.MINECRAFT_BUG_COMMAND.asString))
        }
        if (CommandSetting.MINECRAFT_SUGGESTION_ENABLED.asBoolean) {
            commandManager.registerCommand(info.name, SuggestionCommand(this, CommandSetting.MINECRAFT_SUGGESTION_COMMAND.asString))
        }
        if (CommandSetting.MINECRAFT_DISCORD_ENABLED.asBoolean) {
            commandManager.registerCommand(info.name, DiscordCommand(this, "discord"))
        }
        commandManager.registerCommands(info.name,
                RegisterCommand(this, CommandSetting.MINECRAFT_REGISTER_COMMAND.asString, arrayOf(CommandSetting.MINECRAFT_REGISTER_ALIASES.asString)),
                UnregisterCommand(this, CommandSetting.MINECRAFT_UNREGISTER_COMMAND.asString, arrayOf(CommandSetting.MINECRAFT_UNREGISTER_ALIASES.asString)),
                NetworkManagerBotCommand(this, "networkmanagerbot")
        )
    }

    private fun unregisterListeners() {
        for (listener in this.listeners) {
            this.eventHandler.unregisterListener(listener)
        }
    }

    fun sendRedisBungee(message: String?) {
        this.scheduler.runAsync({ networkManager.redisBungee.sendChannelMessage("NetworkManagerDiscordBot", message!!) }, false)
    }

    private fun expireTokens() {
        for (token in this.discordUserManager.tokens) {
            this.scheduler.runSync(token.let { TokenExpiryTask(this, it) })
        }
    }

    val guild: Guild
            get() = this.discordManager.guild

    override fun getNetworkManager(): NetworkManager {
        return super.getNetworkManager() as NetworkManager
    }

    companion object {
        @JvmStatic
        var instance: DiscordBot? = null
    }
}