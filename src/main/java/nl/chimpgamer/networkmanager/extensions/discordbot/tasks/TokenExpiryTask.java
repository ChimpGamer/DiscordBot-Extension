package nl.chimpgamer.networkmanager.extensions.discordbot.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.chimpgamer.networkmanager.common.utils.Methods;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token;

@RequiredArgsConstructor
@Getter
public class TokenExpiryTask implements Runnable {
    private final DiscordBot discordBot;
    private final Token token;

    @Override
    public void run() {
        if (!this.getDiscordBot().getDiscordUserManager().getTokens().contains(this.getToken())) {
            return;
        }

        String msgStr = DCMessage.REGISTRATION_TOKEN_EXPIRED.getMessage()
                .replace("%command_prefix%", Setting.DISCORD_COMMAND_PREFIX.getAsString());
        if (Methods.isJsonValid(msgStr)) {
            JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(msgStr);
            Utils.editMessage(getToken().getMessage(), jsonEmbedBuilder.build());
        } else {
            Utils.editMessage(getToken().getMessage(), msgStr);
        }

        this.getDiscordBot().getDiscordUserManager().getTokens().remove(getToken());
        this.getDiscordBot().getNetworkManager().debug("Token: " + getToken().getToken() + " has been removed!");
    }
}