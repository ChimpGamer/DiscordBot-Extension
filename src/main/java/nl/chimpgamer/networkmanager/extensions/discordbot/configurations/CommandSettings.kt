package nl.chimpgamer.networkmanager.extensions.discordbot.configurations

import nl.chimpgamer.networkmanager.api.utils.FileUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.io.IOException

class CommandSettings(private val discordBot: DiscordBot) : FileUtils(discordBot.dataFolder.absolutePath, "commands.yml") {
    fun init() {
        setupFile()
        for (commandSetting in CommandSetting.values()) {
            addDefault(commandSetting.path, commandSetting.defaultValue)
        }
        copyDefaults(true)
        save()
    }

    private fun setupFile() {
        if (!file.exists()) {
            try {
                saveToFile(discordBot.getResource("commands.yml"))
            } catch (ex: NullPointerException) {
                try {
                    file.createNewFile()
                } catch (ex1: IOException) {
                    ex1.printStackTrace()
                }
            }
        }
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
    MINECRAFT_BUG_ENABLED("commands.minecraft.bug.enabled", false),
    MINECRAFT_BUG_COMMAND("commands.minecraft.bug.command", "bug"),
    MINECRAFT_REGISTER_COMMAND("commands.minecraft.register.command", "register"),
    MINECRAFT_REGISTER_ALIASES("commands.minecraft.register.aliases", "link"),
    MINECRAFT_UNREGISTER_COMMAND("commands.minecraft.unregister.command", "unregister"),
    MINECRAFT_UNREGISTER_ALIASES("commands.minecraft.unregister.aliases", "unlink"),
    ;

    val asString: String
        get() = DiscordBot.instance!!.commandSettings.getString(path)

    val asBoolean: Boolean
        get() = DiscordBot.instance!!.commandSettings.getBoolean(path)
}