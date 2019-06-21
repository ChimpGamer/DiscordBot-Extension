package nl.chimpgamer.networkmanager.extensions.discordbot.modals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class DiscordUser {
    private final UUID uuid;
    private final String discordId;
    private final long registered;
}