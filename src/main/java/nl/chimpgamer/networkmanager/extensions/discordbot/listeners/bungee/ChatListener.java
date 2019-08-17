package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;

import java.util.Map;

@RequiredArgsConstructor
public class ChatListener implements Listener {
    private final DiscordBot discordBot;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer) || event.isCancelled() || event.isCommand()) {
            return;
        }
        final ProxiedPlayer proxiedPlayer = (ProxiedPlayer) event.getSender();

        String currentServer = proxiedPlayer.getServer().getInfo().getName();

        Map<String, String> chatEventChannels = Setting.DISCORD_EVENTS_CHAT_CHANNELS.getAsMap();
        String globalId = "000000000000000000";
        if (chatEventChannels.containsKey("all")) {
            globalId = chatEventChannels.get("all");
        }
        TextChannel globalChatTextChannel = this.getDiscordBot().getGuild().getTextChannelById(globalId);
        if (globalId != null && globalChatTextChannel != null) {
            globalChatTextChannel.sendMessage(DCMessage.CHAT_EVENT_FORMAT.getMessage()
                    .replace("%playername%", proxiedPlayer.getName())
                    .replace("%server%", currentServer)
                    .replace("%message%", event.getMessage())).queue();
        }

        String serverId = "000000000000000000";
        if (chatEventChannels.containsKey(currentServer)) {
            serverId = chatEventChannels.get(currentServer);
        }
        TextChannel serverChatTextChannel = this.getDiscordBot().getGuild().getTextChannelById(serverId);
        if (serverId != null && serverChatTextChannel != null) {
            serverChatTextChannel.sendMessage(DCMessage.CHAT_EVENT_FORMAT.getMessage()
                    .replace("%playername%", proxiedPlayer.getName())
                    .replace("%server%", currentServer)
                    .replace("%message%", event.getMessage())).queue();
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}