package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

public class PlayersCommand extends Command {
    private final DiscordBot discordBot;

    public PlayersCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.name = CommandSetting.DISCORD_PLAYERS_COMMAND.getAsString();
        this.aliases = new String[]{"onlineplayers", "online"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        if (!CommandSetting.DISCORD_PLAYERS_ENABLED.getAsBoolean()) {
            return;
        }
        Utils.sendChannelMessage(event.getTextChannel(),
                DCMessage.PLAYERS_COMMAND_RESPONSE.getMessage()
                        .replace("%mention%", event.getAuthor().getAsMention())
                        .replace("%players%", String.valueOf(this.getDiscordBot().getNetworkManager().getOnlinePlayersCount())));
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}