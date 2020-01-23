package nl.chimpgamer.networkmanager.extensions.discordbot.modals;

import net.dv8tion.jda.api.entities.Message;
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token;

public class NMToken implements Token {
    private final String token;
    private final String discordID;
    private final long created;
    private final Message message;

    public NMToken(String token, String discordID, Message message) {
        this.token = token;
        this.discordID = discordID;
        this.created = System.currentTimeMillis();
        this.message = message;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getDiscordID() {
        return discordID;
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public Message getMessage() {
        return message;
    }
}