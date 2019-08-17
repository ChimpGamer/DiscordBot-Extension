package nl.chimpgamer.networkmanager.extensions.discordbot.api.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import nl.chimpgamer.networkmanager.api.event.Event;
import nl.chimpgamer.networkmanager.api.models.player.Player;

@Getter
@RequiredArgsConstructor
public class PlayerVerifyEvent extends Event {
    private final Player player;
    private final Member member;
}