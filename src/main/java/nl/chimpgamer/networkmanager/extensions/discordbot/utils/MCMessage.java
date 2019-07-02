package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

public enum MCMessage {

    VERIFY_HELP("minecraft.verify-help"),
    VERIFY_INVALID_TOKEN("minecraft.verify-invalid-token"),
    VERIFY_TOKEN_EXPIRED("minecraft.verify-token-expired"),
    VERIFY_ACCOUNT_ALREADY_LINKED("minecraft.verify-account-already-linked"),
    VERIFY_COMPLETED("minecraft.verify-completed"),
    VERIFY_NOT_IN_SERVER("minecraft.verify-not-in-server"),
    VERIFY_ERROR("minecraft.verify-error"),
    BUG_HELP("minecraft.bug-help"),
    SUGGESTION_HELP("minecraft.suggestion-help"),
    BUG_SUCCESS("minecraft.bug-success"),
    SUGGESTION_SUCCESS("minecraft.suggestion-success"),
    DISCORD_RESPONSE("minecraft.discord-response"),
    RELOAD_CONFIG("minecraft.reload.config"),
    RELOAD_MESSAGES("minecraft.reload.messages"),
    RELOAD_JDA_SUCCESS("minecraft.reload.jda-success"),
    RELOAD_JDA_FAILED("minecraft.reload.jda-success");

    private final String path;

    MCMessage(String path) {
        this.path = path;
    }

    public String getMessage() {
        return DiscordBot.getInstance().getMessagesConfigManager().getString(this.path).replace("%newline%", "\n");
    }
}