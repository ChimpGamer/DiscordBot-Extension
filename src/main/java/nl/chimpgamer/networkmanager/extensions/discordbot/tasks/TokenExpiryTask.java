package nl.chimpgamer.networkmanager.extensions.discordbot.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
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
        Utils.editMessage(getToken().getMessage(), ":x: Token has been expired. Ask me for a new one :D :x:");
        this.getDiscordBot().getDiscordUserManager().getTokens().remove(getToken());
        this.getDiscordBot().getNetworkManager().debug("Token: " + getToken().getToken() + " has been removed!");
    }
}