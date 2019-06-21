package nl.chimpgamer.networkmanager.extensions.discordbot.api.models;

import net.dv8tion.jda.core.entities.Message;

public interface Token {

    String getToken();

    String getDiscordID();

    long getCreated();

    Message getMessage();
}