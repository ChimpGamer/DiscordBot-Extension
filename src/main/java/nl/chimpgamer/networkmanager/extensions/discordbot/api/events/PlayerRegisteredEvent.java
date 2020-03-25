package nl.chimpgamer.networkmanager.extensions.discordbot.api.events;

import net.dv8tion.jda.api.entities.Member;
import nl.chimpgamer.networkmanager.api.event.Event;
import nl.chimpgamer.networkmanager.api.models.player.Player;

public class PlayerRegisteredEvent extends Event {
    private final Player player;
    private final Member member;

    public PlayerRegisteredEvent(Player player, Member member) {
        this.player = player;
        this.member = member;
    }

    public Player getPlayer() {
        return player;
    }

    public Member getMember() {
        return member;
    }
}