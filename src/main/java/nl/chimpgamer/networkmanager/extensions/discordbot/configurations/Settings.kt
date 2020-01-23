package nl.chimpgamer.networkmanager.extensions.discordbot.configurations

import com.google.common.collect.Maps
import nl.chimpgamer.networkmanager.api.utils.FileUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.getRoleByName
import java.io.IOException

class Settings(private val discordBot: DiscordBot) : FileUtils(discordBot.dataFolder.absolutePath, "settings.yml") {
    fun init() {
        setupFile()
        for (setting in Setting.values()) {
            addDefault(setting.path, setting.defaultValue)
        }
        copyDefaults(true)
        save()
    }

    private fun setupFile() {
        if (!file.exists()) {
            try {
                saveToFile(discordBot.getResource("settings.yml"))
            } catch (ex: NullPointerException) {
                try {
                    file.createNewFile()
                } catch (ex1: IOException) {
                    ex1.printStackTrace()
                }
            }
        }
    }

    override fun reload() {
        super.reload()
        discordBot.discordManager.verifiedRole = getRoleByName(Setting.DISCORD_REGISTER_ADD_ROLE_ROLE_NAME.asString)
    }

    fun getString(setting: Setting): String {
        return getString(setting.path)
    }

    fun getBoolean(setting: Setting): Boolean {
        return getBoolean(setting.path)
    }
}

enum class Setting(val path: String, val defaultValue: Any) {
    DISCORD_TOKEN("bot.discord.token", "YourBotTokenHere"),
    DISCORD_OWNER_ID("bot.discord.ownerId", "000000000000000000"),
    DISCORD_COMMAND_PREFIX("bot.discord.command_prefix", "!"),
    DISCORD_STATUS_ENABLED("bot.discord.status.enabled", true),
    DISCORD_STATUS_TYPE("bot.discord.status.type", "watching"),
    DISCORD_STATUS_MESSAGE("bot.discord.status.message", "players on Hypixel"),
    DISCORD_REGISTER_ADD_ROLE_ENABLED("bot.discord.register.addRole.enabled", false),
    DISCORD_REGISTER_ADD_ROLE_ROLE_NAME("bot.discord.register.addRole.roleName", "YourVerifiedRoleNameHere"),
    DISCORD_UNREGISTER_KICK_ENABLED("bot.discord.unregister.kick.enabled", false),
    DISCORD_UNREGISTER_KICK_REASON("bot.discord.unregister.kick.reason", "You have been kicked from the ... discord because your account got unlinked!"),
    DISCORD_SYNC_USERNAME("bot.discord.sync.username", false),
    DISCORD_SYNC_RANKS_ENABLED("bot.discord.sync.ranks.enabled", false),
    DISCORD_SYNC_RANKS_LIST("bot.discord.sync.ranks.list", listOf("VIP", "SuperVIP")),
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
    DISCORD_EVENTS_SERVERSTATUS_CHANNELS("bot.discord.events.serverStatus", mapOf("all" to "000000000000000000"));

    val asString: String
        get() = DiscordBot.instance!!.settings.getString(path)

    val asBoolean: Boolean
        get() = DiscordBot.instance!!.settings.getBoolean(path)

    val asList: List<String>
        get() = DiscordBot.instance!!.settings.getStringList(path)

    val asMap: Map<String, String>
        get() {
            val map: MutableMap<String, String> = Maps.newHashMap()
            for (key in DiscordBot.instance!!.settings.config.getConfigurationSection(path).getKeys(false)) {
                map[key] = DiscordBot.instance!!.settings.getString("$path.$key")
            }
            return map
        }
}