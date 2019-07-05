package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageEmbed;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.api.utils.TimeUtils;
import nl.chimpgamer.networkmanager.bungeecord.modules.NMCachedPlayers;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import java.util.*;

public class PlaytimeCommand extends Command {
    private final DiscordBot discordBot;

    public PlaytimeCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.name = "playtime";
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        this.getDiscordBot().getScheduler().runAsync(() -> {
            JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.getMessage("discord.playtimetop-response"));
            final List<MessageEmbed.Field> fields = new LinkedList<>();
            for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
                String name = insertPlaytimeTopPlaceholders(field.getName());
                String value = insertPlaytimeTopPlaceholders(field.getValue());
                MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
                fields.add(field1);
            }
            jsonEmbedBuilder.setFields(fields);
            Utils.sendChannelMessage(event.getTextChannel(),
                    jsonEmbedBuilder.build());
        }, false);
    }

    private String insertPlaytimeTopPlaceholders(String field) {
        final NMCachedPlayers cachedPlayers = (NMCachedPlayers) this.getDiscordBot().getNetworkManager().getCacheManager().getCachedPlayers();
        int i = 1;
        for (Map.Entry<String, Long> entries : cachedPlayers.getTopPlayTimesUUID().entrySet()) {
            Optional<Player> opPlayer = this.getDiscordBot().getNetworkManager().getPlayerSafe(UUID.fromString(entries.getKey()));
            if (!opPlayer.isPresent()) {
                continue;
            }
            Player player = opPlayer.get();
            field
                    .replace("%position%", String.valueOf(i))
                    .replace("%playername%", player.getRealName())
                    .replace("%username%", player.getUserName())
                    .replace("%nickname%", player.getNicknameOrUserName())
                    .replace("%playtime%", TimeUtils.getTimeString(1, entries.getValue() / 1000))
                    .replace("%liveplaytime%", TimeUtils.getTimeString(1, player.getLivePlaytime() / 1000));
            i++;
        }
        return field;
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}