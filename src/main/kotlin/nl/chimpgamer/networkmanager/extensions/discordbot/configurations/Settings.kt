package nl.chimpgamer.networkmanager.extensions.discordbot.configurations

import nl.chimpgamer.networkmanager.api.utils.FileUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.io.IOException

class Settings(private val discordBot: DiscordBot) : FileUtils(discordBot.dataFolder.absolutePath, "settings.yml") {
    fun init() {
        setupFile()
        for (setting in Setting.values()) {
            addDefault(setting.path, setting.defaultValue)
        }
        convertRolesListToMap()
        copyDefaults(true)
        save()
    }

    private fun convertRolesListToMap() {
        val rolesList = getStringList("bot.discord.sync.ranks.list")
        if (rolesList.isEmpty()) {
            set("bot.discord.sync.ranks.list", null)
            return
        }
        val rolesMap = getMap(Setting.DISCORD_SYNC_RANKS_MAP).toMutableMap()
        for (role in rolesList) {
            rolesMap[role] = role
        }
        set(Setting.DISCORD_SYNC_RANKS_MAP.path, rolesMap)
        set("bot.discord.sync.ranks.list", null)
    }

    private fun setupFile() {
        if (!file.exists()) {
            discordBot.getResource("settings.yml")?.let {
                saveToFile(it)
                reload()
            } ?: run {
                try {
                    file.createNewFile()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun reload() {
        super.reload()
        val roleName = getString(Setting.DISCORD_REGISTER_ADD_ROLE_ROLE_NAME)
        val role = discordBot.discordManager.getRoleByName(roleName)
        if (role != null) {
            discordBot.logger.info("Verified Role is: '${role.name}' (${role.id})")
        } else {
            discordBot.logger.info("No Verified Role found by the name: '$roleName'")
        }
        discordBot.discordManager.verifiedRole = role
    }

    fun getString(setting: Setting): String {
        return getString(setting.path, setting.defaultValue as String)
    }

    fun getBoolean(setting: Setting): Boolean {
        return getBoolean(setting.path, setting.defaultValue as Boolean)
    }

    fun getStringList(setting: Setting): List<String> {
        return getStringList(setting.path)
    }

    @Suppress("UNCHECKED_CAST")
    fun getMap(setting: Setting): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        val mapSection = config.getConfigurationSection(setting.path) ?: return setting.defaultValue as Map<String, String>
        for (key in mapSection.getKeys(false)) {
            map[key] = getString("${setting.path}.$key")!!
        }
        return map
    }
}

enum class Setting(val path: String, val defaultValue: Any) {
    DISCORD_TOKEN("bot.discord.token", "YourBotTokenHere"),
    DISCORD_OWNER_ID("bot.discord.ownerId", "000000000000000000"),
    DISCORD_COMMAND_PREFIX("bot.discord.command_prefix", "!"),
    DISCORD_STATUS_ENABLED("bot.discord.status.enabled", true),
    DISCORD_STATUS_TYPE("bot.discord.status.type", "watching"),
    DISCORD_STATUS_MESSAGE("bot.discord.status.message", "%players% players on your server"),
    DISCORD_REGISTER_ADD_ROLE_ENABLED("bot.discord.register.addRole.enabled", false),
    DISCORD_REGISTER_ADD_ROLE_ROLE_NAME("bot.discord.register.addRole.roleName", "YourVerifiedRoleNameHere"),
    DISCORD_UNREGISTER_KICK_ENABLED("bot.discord.unregister.kick.enabled", false),
    DISCORD_UNREGISTER_KICK_REASON("bot.discord.unregister.kick.reason", "You have been kicked from the ... discord because your account got unlinked!"),
    DISCORD_SYNC_USERNAME("bot.discord.sync.username", false),
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
    DISCORD_EVENTS_SERVERSTATUS_CHANNELS("bot.discord.events.serverStatus", mapOf("all" to "000000000000000000"))
}