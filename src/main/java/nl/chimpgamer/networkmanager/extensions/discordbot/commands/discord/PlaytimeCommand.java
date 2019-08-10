package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageEmbed;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.api.utils.TimeUtils;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlaytimeCommand extends Command {
    private final DiscordBot discordBot;

    public PlaytimeCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.name = CommandSetting.DISCORD_PLAYTIME_COMMAND.getAsString();
        this.cooldown = 3;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        if (!CommandSetting.DISCORD_PLAYTIME_ENABLED.getAsBoolean()) {
            return;
        }

        if (event.getArgs().isEmpty()) {
            UUID uuid = this.getDiscordBot().getDiscordUserManager().getUuidByDiscordId(event.getAuthor().getId());
            if (uuid == null) {
                this.getDiscordBot().getLogger().warning(event.getAuthor().getName() + " tried to use the playtime command but is not registed!");
                return;
            }
            if (this.getDiscordBot().getNetworkManager().isPlayerOnline(uuid, true)) {
                Player player = this.getDiscordBot().getNetworkManager().getPlayer(uuid);
                JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.PLAYTIME_RESPONSE.getMessage());
                jsonEmbedBuilder.setTitle(jsonEmbedBuilder.getTitle().replace("%playername%", player.getName()));
                final List<MessageEmbed.Field> fields = new LinkedList<>();
                for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
                    String name = field.getName()
                            .replace("%playername%", player.getName())
                            .replace("%playtime%", TimeUtils.getTimeString(1, player.getPlaytime() / 1000))
                            .replace("%liveplaytime%", TimeUtils.getTimeString(player.getLanguage(), player.getPlaytime() / 1000));
                    String value = field.getValue()
                            .replace("%playername%", player.getName())
                            .replace("%playtime%", TimeUtils.getTimeString(1, player.getPlaytime() / 1000))
                            .replace("%liveplaytime%", TimeUtils.getTimeString(player.getLanguage(), player.getPlaytime() / 1000));
                    MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
                    fields.add(field1);
                }
                jsonEmbedBuilder.setFields(fields);
                Utils.sendChannelMessage(event.getTextChannel(),
                        jsonEmbedBuilder.build());
            } else {
                this.getDiscordBot().getScheduler().runAsync(() -> {
                    String[] result = getOfflinePlayerPlaytime(uuid);
                    JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.PLAYTIME_RESPONSE.getMessage());
                    jsonEmbedBuilder.setTitle(jsonEmbedBuilder.getTitle().replace("%playername%", result[0]));
                    final List<MessageEmbed.Field> fields = new LinkedList<>();
                    for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
                        String name = field.getName()
                                .replace("%playername%", result[0])
                                .replace("%playtime%", TimeUtils.getTimeString(1, Long.parseLong(result[1]) / 1000))
                                .replace("%liveplaytime%", TimeUtils.getTimeString(1, Long.parseLong(result[1]) / 1000));
                        String value = field.getValue()
                                .replace("%playername%", result[0])
                                .replace("%playtime%", TimeUtils.getTimeString(1, Long.parseLong(result[1]) / 1000))
                                .replace("%liveplaytime%", TimeUtils.getTimeString(1, Long.parseLong(result[1]) / 1000));
                        MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
                        fields.add(field1);
                    }
                    jsonEmbedBuilder.setFields(fields);
                    Utils.sendChannelMessage(event.getTextChannel(),
                            jsonEmbedBuilder.build());
                }, false);
            }
        }
    }

    private String[] getOfflinePlayerPlaytime(UUID uuid) {
        String[] result = new String[]{};
        try (Connection connection = this.getDiscordBot().getMySQL().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT username, playtime FROM nm_players WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    long playtime = rs.getLong("playtime");
                    result = new String[]{username, String.valueOf(playtime)};
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}