package nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot

class CommandSettings(private val discordBot: DiscordBot) {
    private val config: YamlDocument

    init {
        val file = discordBot.dataFolder.resolve("commands.yml")
        val inputStream = discordBot.platform.getResource("commands.yml")
        val loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build()
        val updaterSettings = UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        } else {
            YamlDocument.create(file, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        }
    }

    fun reload() = runCatching { config.reload() }

    fun getBoolean(commandSetting: CommandSetting): Boolean {
        return config.getBoolean(commandSetting.path, commandSetting.defaultValue as Boolean)
    }

    fun getString(commandSetting: CommandSetting): String {
        return config.getString(commandSetting.path, commandSetting.defaultValue as String)
    }

    fun getInt(commandSetting: CommandSetting): Int {
        return config.getInt(commandSetting.path, commandSetting.defaultValue as Int)
    }
}

enum class CommandSetting(val path: String, val defaultValue: Any) {
    DISCORD_PLAYERLIST_ENABLED("commands.discord.playerlist.enabled", true),
    DISCORD_PLAYERLIST_COMMAND("commands.discord.playerlist.command", "playerlist"),
    DISCORD_PLAYERLIST_DESCRIPTION("commands.discord.playerlist.description", "List the players that are currently online on the minecraft server"),
    DISCORD_PLAYERLIST_OPTIONS_SERVERNAME_NAME("commands.discord.playerlist.options.servername.name", "servername"),
    DISCORD_PLAYERLIST_OPTIONS_SERVERNAME_DESCRIPTION("commands.discord.playerlist.options.servername.description", "Name of a server on the minecraft server"),

    DISCORD_PLAYERS_ENABLED("commands.discord.players.enabled", true),
    DISCORD_PLAYERS_COMMAND("commands.discord.players.command", "players"),
    DISCORD_PLAYERS_DESCRIPTION("commands.discord.players.description", "Shows the amount of players that are currently online on the minecraft server"),

    DISCORD_PLAYTIME_ENABLED("commands.discord.playtime.enabled", true),
    DISCORD_PLAYTIME_COMMAND("commands.discord.playtime.command", "playtime"),
    DISCORD_PLAYTIME_DESCRIPTION("commands.discord.playtime.description", "Shows your current playtime"),

    DISCORD_UPTIME_ENABLED("commands.discord.uptime.enabled", true),
    DISCORD_UPTIME_COMMAND("commands.discord.uptime.command", "uptime"),
    DISCORD_UPTIME_DESCRIPTION("commands.discord.uptime.description", "Shows the current uptime of the network."),

    DISCORD_REGISTER_COMMAND("commands.discord.register.command", "register"),
    DISCORD_REGISTER_DESCRIPTION("commands.discord.register.description", "Register your discord account with your minecraft account on our server"),

    MINECRAFT_DISCORD_ENABLED("commands.minecraft.discord.enabled", false),
    MINECRAFT_SUGGESTION_ENABLED("commands.minecraft.suggestion.enabled", false),
    MINECRAFT_SUGGESTION_COMMAND("commands.minecraft.suggestion.command", "suggestion"),
    MINECRAFT_SUGGESTION_COOLDOWN("commands.minecraft.suggestion.cooldown", 60),
    MINECRAFT_BUG_ENABLED("commands.minecraft.bug.enabled", false),
    MINECRAFT_BUG_COMMAND("commands.minecraft.bug.command", "bug"),
    MINECRAFT_BUG_COOLDOWN("commands.minecraft.bug.cooldown", 60),
    MINECRAFT_REGISTER_COMMAND("commands.minecraft.register.command", "register"),
    MINECRAFT_REGISTER_ALIASES("commands.minecraft.register.aliases", "link"),
    MINECRAFT_UNREGISTER_COMMAND("commands.minecraft.unregister.command", "unregister"),
    MINECRAFT_UNREGISTER_ALIASES("commands.minecraft.unregister.aliases", "unlink"),
}