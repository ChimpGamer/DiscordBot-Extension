package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import java.util.Collection;

public class PlayerListCommand extends Command {
    private final DiscordBot discordBot;

    public PlayerListCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.name = "playerlist";
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        if (!this.getDiscordBot().getConfigManager().isDiscordCommandEnabled(this.getName())) {
            return;
        }

        if (event.getArgs().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Collection<ProxiedPlayer> players = this.getDiscordBot().getNetworkManager().getProxy().getPlayers();
            if (players.isEmpty()) {
                sb.append("There are currently no players online!");
            } else {
                for (ProxiedPlayer proxiedPlayer : this.getDiscordBot().getNetworkManager().getProxy().getPlayers()) {
                    sb.append(proxiedPlayer.getName()).append(" - ").append(proxiedPlayer.getServer().getInfo().getName()).append("\n");
                }
            }
            String playerList = sb.toString().trim();
            Utils.sendChannelMessage(event.getTextChannel(), playerList);
            return;
        }
        String[] args = event.getArgs().split(" ");
        if (args.length == 1) {
            String serverName = args[0];
            ServerInfo serverInfo = this.getDiscordBot().getNetworkManager().getProxy().getServerInfo(serverName);
            if (serverInfo == null) {
                Utils.sendChannelMessage(event.getTextChannel(), DCMessage.PLAYERLIST_COMMAND_INVALID_SERVER.getMessage()
                        .replace("%mention%", event.getAuthor().getAsMention())
                        .replace("%server%", serverName));
            }
            StringBuilder sb = new StringBuilder();
            for (ProxiedPlayer proxiedPlayer : this.getDiscordBot().getNetworkManager().getProxy().getServerInfo(serverName).getPlayers()) {
                sb.append(proxiedPlayer.getName()).append(" - ").append(proxiedPlayer.getServer().getInfo().getName()).append("\n");
            }
            if (sb.length() > 0) {
                sb = new StringBuilder(sb.toString().substring(0, sb.toString().length() - 2));
            }
            String playerList = sb.toString().trim();
            Utils.sendChannelMessage(event.getTextChannel(), playerList);
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}