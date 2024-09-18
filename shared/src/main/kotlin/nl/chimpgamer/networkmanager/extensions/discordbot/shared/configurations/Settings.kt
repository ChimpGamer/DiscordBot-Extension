package nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot

class Settings(private val discordBot: DiscordBot) {
    private val config: YamlDocument

    val botDiscordOnlineStatus: String get() = config.getString("bot.discord.online-status")

    init {
        val file = discordBot.dataFolder.resolve("settings.yml")
        val inputStream = discordBot.platform.getResource("settings.yml")
        val generalSettings = GeneralSettings.builder().setUseDefaults(false).build()
        val loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build()
        val updaterSettings = UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
        config = if (inputStream != null) {
            YamlDocument.create(file, inputStream, generalSettings, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        } else {
            YamlDocument.create(file, generalSettings, loaderSettings, DumperSettings.DEFAULT, updaterSettings)
        }

        convertRolesListToMap()
    }

    private fun convertRolesListToMap() {
        val rolesList = config.getStringList("bot.discord.sync.ranks.list")
        if (rolesList.isEmpty()) {
            config.set("bot.discord.sync.ranks.list", null)
            return
        }
        val rolesMap = getMap(Setting.DISCORD_SYNC_RANKS_MAP).toMutableMap()
        for (role in rolesList) {
            rolesMap[role] = role
        }
        config.set(Setting.DISCORD_SYNC_RANKS_MAP.path, rolesMap)
        config.set("bot.discord.sync.ranks.list", null)
        config.save()
    }

    fun reload() {
        kotlin.runCatching { config.reload() }
        if (discordBot.isDiscordManagerInitialized()) {
            val roleName = getString(Setting.DISCORD_REGISTER_ADD_ROLE_ROLE_NAME)
            val role = discordBot.discordManager.getRole(roleName)
            if (role != null) {
                discordBot.platform.info("Verified Role is: '${role.name}' (${role.id})")
            } else {
                discordBot.platform.info("No Verified Role found by the name: '$roleName'")
            }
            discordBot.discordManager.verifiedRole = role
        }
    }

    fun getString(setting: Setting): String = config.getString(setting.path, setting.defaultValue as String)
    fun getInt(setting: Setting): Int = config.getInt(setting.path, setting.defaultValue as Int)
    fun getBoolean(setting: Setting): Boolean = config.getBoolean(setting.path, setting.defaultValue as Boolean)
    fun getStringList(setting: Setting): List<String> = config.getStringList(setting.path)

    @Suppress("UNCHECKED_CAST")
    fun getMap(setting: Setting): Map<String, String> {
        val mapSection = config.getSection(setting.path) ?: return setting.defaultValue as Map<String, String>
        return mapSection.getStringRouteMappedValues(false).mapValues { it.value.toString() }
    }
}

enum class Setting(val path: String, val defaultValue: Any) {
    DISCORD_TOKEN("bot.discord.token", "YourBotTokenHere"),
    DISCORD_MAX_GUILDS("bot.discord.max-guilds", 1),
    DISCORD_MAIN_GUILD("bot.discord.main-guild", "000000000000000000"),
    DISCORD_STATUS_ENABLED("bot.discord.status.enabled", true),
    DISCORD_STATUS_TYPE("bot.discord.status.type", "watching"),
    DISCORD_STATUS_MESSAGE("bot.discord.status.message", "%players% players on your server"),
    DISCORD_REGISTER_ADD_ROLE_ENABLED("bot.discord.register.addRole.enabled", false),
    DISCORD_REGISTER_ADD_ROLE_ROLE_NAME("bot.discord.register.addRole.roleName", "YourVerifiedRoleNameHere"),
    DISCORD_REGISTER_EXECUTE_COMMANDS("bot.discord.register.executeCommands", "[]"),
    DISCORD_UNREGISTER_KICK_ENABLED("bot.discord.unregister.kick.enabled", false),
    DISCORD_UNREGISTER_KICK_REASON(
        "bot.discord.unregister.kick.reason",
        "You have been kicked from the ... discord because your account got unlinked!"
    ),
    DISCORD_UNREGISTER_EXECUTE_COMMANDS("bot.discord.unregister.executeCommands", "[]"),
    DISCORD_SYNC_USERNAME_ENABLED("bot.discord.sync.username.enabled", false),
    DISCORD_SYNC_USERNAME_FORMAT("bot.discord.sync.username.format", "%playername%"),
    DISCORD_SYNC_RANKS_ENABLED("bot.discord.sync.ranks.enabled", false),
    DISCORD_SYNC_RANKS_MAP("bot.discord.sync.ranks.map", mapOf("vip" to "VIP", "supervip" to "SuperVIP")),
    DISCORD_EVENTS_STAFFCHAT_CHANNEL("bot.discord.events.staffchat.channel", "000000000000000000"),
    DISCORD_EVENTS_ADMINCHAT_CHANNEL("bot.discord.events.adminchat.channel", "000000000000000000"),
    DISCORD_EVENTS_TICKETS_CHANNEL("bot.discord.events.tickets.channel", "000000000000000000"),
    DISCORD_EVENTS_HELPOP_CHANNEL("bot.discord.events.helpop.channel", "000000000000000000"),
    DISCORD_EVENTS_PUNISHMENT_CHANNEL("bot.discord.events.punishment.channel", "000000000000000000"),
    DISCORD_EVENTS_REPORT_CHANNEL("bot.discord.events.report.channel", "000000000000000000"),
    DISCORD_EVENTS_CHATLOG_CHANNEL("bot.discord.events.chatlog.channel", "000000000000000000"),
    DISCORD_EVENTS_SUGGESTION_CHANNEL("bot.discord.events.suggestion.channel", "000000000000000000"),
    DISCORD_EVENTS_BUGREPORT_CHANNEL("bot.discord.events.bugreport.channel", "000000000000000000"),
    DISCORD_EVENTS_CHAT_CHANNELS("bot.discord.events.chat", mapOf("all" to "000000000000000000")),
    DISCORD_EVENTS_SERVERSTATUS_CHANNELS("bot.discord.events.serverStatus", mapOf("all" to "000000000000000000")),
    DISCORD_EVENTS_LOGIN_CHANNEL("bot.discord.events.login.channel", "000000000000000000"),
    DISCORD_EVENTS_FIRST_LOGIN_CHANNEL("bot.discord.events.firstlogin.channel", "000000000000000000"),
    DISCORD_EVENTS_DISCONNECT_CHANNEL("bot.discord.events.disconnect.channel", "000000000000000000"),
    DISCORD_EVENTS_SERVER_SWITCH_CHANNEL("bot.discord.events.server-switch.channel", "000000000000000000"),
    DISCORD_EVENTS_MAINTENANCE_MODE_CHANNEL("bot.discord.events.maintenance-mode.channel", "000000000000000000"),
    DISCORD_EVENTS_REGISTRATION_ALERTS_CHANNEL("bot.discord.events.registration-alerts.channel", "000000000000000000"),
}