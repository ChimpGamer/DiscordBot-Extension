package nl.chimpgamer.networkmanager.extensions.discordbot.configurations

import nl.chimpgamer.networkmanager.api.utils.FileUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

class CommandSettings(private val discordBot: DiscordBot) : FileUtils(discordBot.dataFolder.absolutePath, "commands.yml") {
    fun init() {
        setupFile(discordBot.getResource("commands.yml"))
        for (commandSetting in CommandSetting.values()) {
            addDefault(commandSetting.path, commandSetting.defaultValue)
        }
        copyDefaults(true)
        save()
    }

    fun getBoolean(commandSetting: CommandSetting): Boolean {
        return getBoolean(commandSetting.path, commandSetting.defaultValue as Boolean)
    }

    fun getString(commandSetting: CommandSetting): String {
        return getString(commandSetting.path, commandSetting.defaultValue as String)
    }

    fun getInt(commandSetting: CommandSetting): Int {
        return getInt(commandSetting.path, commandSetting.defaultValue as Int)
    }
}

enum class CommandSetting(val path: String, val defaultValue: Any) {
    DISCORD_PLAYERLIST_ENABLED("commands.discord.playerlist.enabled", true),
    DISCORD_PLAYERLIST_COMMAND("commands.discord.playerlist.command", "playerlist"),
    DISCORD_PLAYERS_ENABLED("commands.discord.players.enabled", true),
    DISCORD_PLAYERS_COMMAND("commands.discord.players.command", "players"),
    DISCORD_PLAYTIME_ENABLED("commands.discord.playtime.enabled", true),
    DISCORD_PLAYTIME_COMMAND("commands.discord.playtime.command", "playtime"),
    DISCORD_UPTIME_ENABLED("commands.discord.uptime.enabled", true),
    DISCORD_UPTIME_COMMAND("commands.discord.uptime.command", "uptime"),
    DISCORD_REGISTER_COMMAND("commands.discord.register.command", "register"),
    DISCORD_REGISTER_ALIASES("commands.discord.register.aliases", "register"),
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