package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import nl.chimpgamer.networkmanager.api.utils.Cooldown;
import nl.chimpgamer.networkmanager.api.utils.TimeUtils;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.MCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;
import nl.chimpgamer.networkmanager.api.commands.NMBungeeCommand;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.api.sender.Sender;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BugCommand extends NMBungeeCommand {
    private final DiscordBot discordBot;

    public BugCommand(DiscordBot discordBot, String cmd) {
        super(discordBot.getNetworkManager(), cmd, Arrays.asList("networkmanager.bot.bug", "networkmanager.admin"));
        this.discordBot = discordBot;
        this.setPlayerOnly(true);
    }

    @Override
    public void onExecute(Sender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (Cooldown.isInCooldown(player.getUuid(), "BugCMD")) {
                player.sendMessage("&7You have to wait &c%cooldown% &7before you can send a new bug report."
                        .replace("%cooldown%", TimeUtils.getTimeString(player.getLanguage(), Cooldown.getTimeLeft(player.getUuid(), this.getName()))));
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            String bug = sb.toString().trim();
            TextChannel bugReportChannel = getDiscordBot().getGuild().getTextChannelById(Setting.DISCORD_EVENTS_BUGREPORT_CHANNEL.getAsString());
            if (bugReportChannel == null) {
                return;
            }
            JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(DCMessage.BUGREPORT_ALERT.getMessage());
            final List<MessageEmbed.Field> fields = new LinkedList<>();
            for (MessageEmbed.Field field : jsonEmbedBuilder.getFields()) {
                String name = insertBugReportPlaceholders(field.getName(), player, player.getServer(), bug);
                String value = insertBugReportPlaceholders(field.getValue(), player, player.getServer(), bug);
                MessageEmbed.Field field1 = new MessageEmbed.Field(name, value, field.isInline());
                fields.add(field1);
            }
            jsonEmbedBuilder.setFields(fields);
            Utils.sendChannelMessage(bugReportChannel,
                    jsonEmbedBuilder.build());
            player.sendMessage(MCMessage.BUG_SUCCESS.getMessage());
            new Cooldown(player.getUuid(), "BugCMD", 60).start();
        } else {
            player.sendMessage(MCMessage.BUG_HELP.getMessage());
        }
    }

    @Override
    public List<String> onTabComplete(Sender sender, String[] args) {
        return Collections.emptyList();
    }

    private String insertBugReportPlaceholders(String s, Player player, String serverName, String bug) {
        return s.replace("%playername%", player.getName())
                .replace("%server%", serverName)
                .replace("%bug%", bug);
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}