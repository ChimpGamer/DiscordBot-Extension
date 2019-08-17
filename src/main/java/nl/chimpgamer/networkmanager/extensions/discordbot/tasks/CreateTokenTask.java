package nl.chimpgamer.networkmanager.extensions.discordbot.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import nl.chimpgamer.networkmanager.common.utils.Methods;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder;
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
        final String msgStr = DCMessage.REGISTRATION_TOKEN.getMessage()
                .replace("%newline%", "\n")
                .replace("%token%", token);

        Message message;

        if (Methods.isJsonValid(msgStr)) {
            JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(msgStr);
            message = Utils.sendMessageComplete(this.getChannel(), jsonEmbedBuilder.build());
        } else {
            message = Utils.sendMessageComplete(this.getChannel(), msgStr);
        }

        this.getDiscordBot().getDiscordUserManager().insertToken(token, this.getDiscordID(), message);
    }
}