package nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations

import nl.chimpgamer.networkmanager.api.utils.FileUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot

class Messages(private val discordBot: DiscordBot) : FileUtils(discordBot.dataFolder.absolutePath, "messages.yml") {
    fun init() {
        setupFile(discordBot.platform.getResource("messages.yml"))
    }

    fun getString(dcMessage: DCMessage): String {
        return getString(dcMessage.path) ?: error("The message ${dcMessage.path} does not exist!")
    }

    fun getString(mcMessage: MCMessage): String {
        return getString(mcMessage.path) ?: error("The message ${mcMessage.path} does not exist!")
    }
}

enum class DCMessage(val path: String) {
    EVENT_STAFFCHAT("discord.event.staffchat"),
    EVENT_ADMINCHAT("discord.event.adminchat"),
    EVENT_CHAT("discord.event.chat"),
    EVENT_JOIN("discord.event.join"),
    EVENT_AGREED_MEMBERSHIP_SCREENING_REQUIREMENTS("discord.event.agreed-membership-screening-requirements"),
    EVENT_PLAYERLOGIN("discord.event.playerlogin"),
    EVENT_FIRST_PLAYERLOGIN("discord.event.firstplayerlogin"),
    EVENT_DISCONNECT("discord.event.disconnect"),
    EVENT_SERVER_SWITCH("discord.event.server-switch"),

    COMMAND_ONLINEPLAYERS_RESPONSE("discord.command.onlineplayers.response"),
    COMMAND_PLAYERLIST_INVALID_SERVER("discord.command.playerlist.invalid-server"),
    COMMAND_PLAYTIME_RESPONSE("discord.command.playtime.response"),

    REGISTRATION_NOT_IN_SERVER("discord.registration.not-in-server"),
    REGISTRATION_MEMBERSHIP_SCREENING_REQUIREMENTS_NOT_MET("discord.registration.membership-screening-requirements-not-met"),
    REGISTRATION_TOKEN_RESPONSE("discord.registration.token.response"),
    REGISTRATION_TOKEN_EXPIRED("discord.registration.token.expired"),
    REGISTRATION_IN_PROCESS("discord.registration.in-process"),
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
    BUG_SUCCESS("minecraft.bug.success"),
    BUG_COOLDOWN("minecraft.bug.cooldown"),
    SUGGESTION_SUCCESS("minecraft.suggestion.success"),
    SUGGESTION_COOLDOWN("minecraft.suggestion.cooldown"),
    DISCORD_RESPONSE("minecraft.discord-response"),

    RELOAD_CONFIG("minecraft.reload.config"),
    RELOAD_MESSAGES("minecraft.reload.messages"),
    RELOAD_JDA_SUCCESS("minecraft.reload.jda.success"),
    RELOAD_JDA_FAILED("minecraft.reload.jda.success"),

    EVENT_CHAT("minecraft.event.chat")
}