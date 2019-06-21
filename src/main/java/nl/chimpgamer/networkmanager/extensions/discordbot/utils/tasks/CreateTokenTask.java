package nl.chimpgamer.networkmanager.extensions.discordbot.utils.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

@RequiredArgsConstructor
@Getter
public class CreateTokenTask implements Runnable {
    private final DiscordBot discordBot;
    private final MessageChannel channel;
    private final String discordID;

    @Override
    public void run() {
        final String token = Utils.generateToken();
        final Message message = Utils.sendMessageComplete(this.getChannel(), DCMessage.REGISTRATION_TOKEN.getMessage()
                .replace("%newline%", "\n")
                .replace("%token%", token));

        this.getDiscordBot().getDiscordUserManager().insertToken(token, this.getDiscordID(), message);
    }
}