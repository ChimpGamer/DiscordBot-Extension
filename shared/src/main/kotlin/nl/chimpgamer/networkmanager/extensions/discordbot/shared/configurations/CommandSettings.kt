package nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot

class CommandSettings(discordBot: DiscordBot) {
    private val config: YamlDocument

    val discordPlayerListEnabled: Boolean get() = config.getBoolean("commands.discord.playerlist.enabled", true)
    val discordPlayerListCommand: String get() = config.getString("commands.discord.playerlist.command", "playerlist")
    val discordPlayerListDescription: String get() = config.getString("commands.discord.playerlist.description", "List the players that are currently online on the minecraft server")
    val discordPlayerListOptionsServerName: String get() = config.getString("commands.discord.playerlist.options.servername.name", "servername")
    val discordPlayerListOptionsServerDescription: String get() = config.getString("commands.discord.playerlist.options.servername.description", "Name of a server on the minecraft server")

    val discordPlayersEnabled: Boolean get() = config.getBoolean("commands.discord.players.enabled", true)
    val discordPlayersCommand: String get() = config.getString("commands.discord.players.command", "players")
    val discordPlayersDescription: String get() = config.getString("commands.discord.players.description", "Shows the amount of players that are currently online on the minecraft server")

    val discordPlaytimeEnabled: Boolean get() = config.getBoolean("commands.discord.playtime.enabled", true)
    val discordPlaytimeCommand: String get() = config.getString("commands.discord.playtime.command", "playtime")
    val discordPlaytimeDescription: String get() = config.getString("commands.discord.playtime.description", "Shows your current playtime")

    val discordUptimeEnabled: Boolean get() = config.getBoolean("commands.discord.uptime.enabled", true)
    val discordUptimeCommand: String get() = config.getString("commands.discord.uptime.command", "uptime")
    val discordUptimeDescription: String get() = config.getString("commands.discord.uptime.description", "Shows the current uptime of the network.")

    val discordRegisterCommand: String get() = config.getString("commands.discord.register.command", "register")
    val discordRegisterDescription: String get() = config.getString("commands.discord.register.description", "Register your discord account with your minecraft account on our server")

    val discordTicketCommand: String get() = config.getString("commands.discord.ticket.command", "ticket")
    val discordTicketDescription: String get() = config.getString("commands.discord.ticket.description", "Create a new ticket.")

    val minecraftDiscordEnabled: Boolean get() = config.getBoolean("commands.minecraft.discord.enabled", false)

    val minecraftSuggestionEnabled: Boolean get() = config.getBoolean("commands.minecraft.suggestion.enabled", false)
    val minecraftSuggestionCommand: String get() = config.getString("commands.minecraft.suggestion.command", "suggestion")
    val minecraftSuggestionCooldown: Int get() = config.getInt("commands.minecraft.suggestion.cooldown", 60)

    val minecraftBugEnabled: Boolean get() = config.getBoolean("commands.minecraft.bug.enabled", false)
    val minecraftBugCommand: String get() = config.getString("commands.minecraft.bug.command", "bug")
    val minecraftBugCooldown: Int get() = config.getInt("commands.minecraft.bug.cooldown", 60)

    val minecraftRegisterCommand: String get() = config.getString("commands.minecraft.register.command", "register")
    val minecraftRegisterAlias: String get() = config.getString("commands.minecraft.register.alias", "link")

    val minecraftUnregisterCommand: String get() = config.getString("commands.minecraft.unregister.command", "unregister")
    val minecraftUnregisterAlias: String get() = config.getString("commands.minecraft.unregister.alias", "unlink")

    fun reload() = runCatching { config.reload() }

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
}