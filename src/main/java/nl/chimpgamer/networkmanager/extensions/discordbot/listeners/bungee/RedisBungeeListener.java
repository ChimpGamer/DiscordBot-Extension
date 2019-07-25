package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee;

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

import java.util.UUID;

@RequiredArgsConstructor
public class RedisBungeeListener implements Listener {
    private final DiscordBot discordBot;

    @EventHandler
    public void onPubSubMessage(PubSubMessageEvent event) {
        if (!event.getChannel().equals("NetworkManagerDiscordBot")) {
            return;
        }

        String message = event.getMessage();
        String[] split = message.split(" ");
        if (split[0].equalsIgnoreCase("load")) {
            UUID uuid = UUID.fromString(split[1]);
            this.getDiscordBot().getDiscordUserManager().load(uuid);
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}