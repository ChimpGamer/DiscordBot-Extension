package nl.chimpgamer.networkmanager.extensions.discordbot.configurations

import nl.chimpgamer.networkmanager.api.utils.FileUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.io.IOException

class Messages(private val discordBot: DiscordBot) : FileUtils(discordBot.dataFolder.absolutePath, "messages.yml") {
    fun init() {
        setupFile()
    }

    private fun setupFile() {
        if (!file.exists()) {
            try {
                saveToFile(discordBot.getResource("messages.yml"))
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

enum class DCMessage(private val path: String) {
    STAFFCHAT_RECEIVE("discord.staffchat-receive"),
    ADMINCHAT_RECEIVE("discord.adminchat-receive"),
    REGISTRATION_TOKEN("discord.registration-token"),
    REGISTRATION_TOKEN_EXPIRED("discord.registration-token-expired"),
    REGISTRATION_NOT_IN_SERVER("registration-not-in-server"),
    REGISTRATION_COMPLETED("discord.registration-completed"),
    UNREGISTER_COMPLETED("discord.unregister-completed"),
    SERVER_STATUS_ONLINE("discord.server-status-online"),
    SERVER_STATUS_OFFLINE("discord.server-status-offline"),
    HELPOP_ALERT("discord.helpop-alert"),
    TICKET_CREATE_ALERT("discord.ticket-create-alert"),
    PUNISHMENT_ALERT("discord.punishment-alert"),
    UNPUNISHMENT_ALERT("discord.unpunishment-alert"),
    REPORT_ALERT("discord.report-alert"),
    BUGREPORT_ALERT("discord.bugreport-alert"),
    SUGGESTION_ALERT("discord.suggestion-alert"),
    CHATLOG_ALERT("discord.chatlog-alert"),
    PLAYERS_COMMAND_RESPONSE("discord.player-command-response"),
    PLAYERLIST_COMMAND_INVALID_SERVER("discord.playerlist-command-invalid-server"),
    CHAT_EVENT_FORMAT("discord.chat-event-format"),
    PLAYTIME_RESPONSE("discord.playtime-response"),
    WELCOME_NEW_MEMBER("discord.welcome-new-member");

    val message: String
        get() = DiscordBot.instance!!.messages.getString(path).replace("%newline%", "\n")

}

enum class MCMessage(private val path: String) {
    REGISTER_HELP("minecraft.register.help"),
    REGISTER_INVALID_TOKEN("minecraft.verify.token.invalid"),
    REGISTER_TOKEN_EXPIRED("minecraft.verify.token.expired"),
    REGISTER_ACCOUNT_ALREADY_LINKED("minecraft.verify.account-already-linked"),
    REGISTER_COMPLETED("minecraft.verify.completed"),
    REGISTER_NOT_IN_SERVER("minecraft.verify.not-in-server"),
    REGISTER_ERROR("minecraft.verify.error"),
    BUG_HELP("minecraft.bug.help"),
    BUG_SUCCESS("minecraft.bug.success"),
    SUGGESTION_HELP("minecraft.suggestion.help"),
    SUGGESTION_SUCCESS("minecraft.suggestion.success"),
    DISCORD_RESPONSE("minecraft.discord-response"),
    RELOAD_CONFIG("minecraft.reload.config"),
    RELOAD_MESSAGES("minecraft.reload.messages"),
    RELOAD_JDA_SUCCESS("minecraft.reload.jda-success"),
    RELOAD_JDA_FAILED("minecraft.reload.jda-success");

    val message: String
        get() = DiscordBot.instance!!.messages.getString(path).replace("%newline%", "\n")

}