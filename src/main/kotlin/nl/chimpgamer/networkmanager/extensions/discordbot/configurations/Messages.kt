package nl.chimpgamer.networkmanager.extensions.discordbot.configurations

import nl.chimpgamer.networkmanager.api.utils.FileUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.io.IOException

class Messages(private val discordBot: DiscordBot) : FileUtils(discordBot.dataFolder.absolutePath, "messages.yml") {
    fun init() {
        setupFile()
    }

    fun getString(dcMessage: DCMessage): String {
        return getString(dcMessage.path)
    }

    fun getString(mcMessage: MCMessage): String {
        return getString(mcMessage.path)
    }

    private fun setupFile() {
        if (!file.exists()) {
            try {
                saveToFile(discordBot.getResource("messages.yml"))
                reload()
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

enum class DCMessage(val path: String) {
    EVENT_STAFFCHAT("discord.event.staffchat"),
    EVENT_ADMINCHAT("discord.event.adminchat"),
    EVENT_CHAT("discord.event.chat"),
    EVENT_WELCOME("discord.event.join"),

    COMMAND_ONLINEPLAYERS_RESPONSE("discord.command.onlineplayers.response"),
    COMMAND_PLAYERLIST_INVALID_SERVER("discord.command.playerlist.invalid-server"),
    COMMAND_PLAYTIME_RESPONSE("discord.command.playtime.response"),

    REGISTRATION_NOT_IN_SERVER("discord.registration.not-in-server"),
    REGISTRATION_TOKEN_RESPONSE("discord.registration.token.response"),
    REGISTRATION_TOKEN_EXPIRED("discord.registration.token.expired"),
    REGISTRATION_COMPLETED("discord.registration.completed"),
    REGISTRATION_UNREGISTER_NOTIFICATION("discord.registration.unregistered.notification"),

    SERVER_STATUS_ONLINE("discord.server-status.online"),
    SERVER_STATUS_OFFLINE("discord.server-status.offline"),
    HELPOP_ALERT("discord.helpop-alert"),
    TICKET_CREATE_ALERT("discord.ticket-create-alert"),
    PUNISHMENT_ALERT("discord.punishment-alert"),
    UNPUNISHMENT_ALERT("discord.unpunishment-alert"),
    REPORT_ALERT("discord.report-alert"),
    BUGREPORT_ALERT("discord.bugreport-alert"),
    SUGGESTION_ALERT("discord.suggestion-alert"),
    CHATLOG_ALERT("discord.chatlog-alert"),

    ;
}

enum class MCMessage(val path: String) {
    REGISTER_HELP("minecraft.register.help"),
    REGISTER_INVALID_TOKEN("minecraft.register.token.invalid"),
    REGISTER_TOKEN_EXPIRED("minecraft.register.token.expired"),
    REGISTER_ACCOUNT_ALREADY_LINKED("minecraft.register.verify.account-already-linked"),
    REGISTER_COMPLETED("minecraft.register.verify.completed"),
    REGISTER_NOT_IN_SERVER("minecraft.register.verify.not-in-server"),
    REGISTER_ERROR("minecraft.register.verify.error"),
    UNREGISTER_SUCCESS("minecraft.unregister.success"),
    UNREGISTER_NOT_REGISTERED("minecraft.unregister.not-registered"),
    BUG_HELP("minecraft.bug.help"),
    BUG_SUCCESS("minecraft.bug.success"),
    SUGGESTION_HELP("minecraft.suggestion.help"),
    SUGGESTION_SUCCESS("minecraft.suggestion.success"),
    DISCORD_RESPONSE("minecraft.discord-response"),
    RELOAD_CONFIG("minecraft.reload.config"),
    RELOAD_MESSAGES("minecraft.reload.messages"),
    RELOAD_JDA_SUCCESS("minecraft.reload.jda.success"),
    RELOAD_JDA_FAILED("minecraft.reload.jda.success")
    ;
}