package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.core.entities.ChannelType;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import java.sql.SQLException;

public class UnregisterCommand extends Command {
    private final DiscordBot discordBot;

    public UnregisterCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.name = "unregister";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (!event.isFromType(ChannelType.PRIVATE)) {
            return;
        }
        try {
            if (!this.getDiscordBot().getDiscordUserManager().checkUserByDiscordId(event.getAuthor().getId())) {
                return;
            }
            this.getDiscordBot().getScheduler().runAsync(() -> this.getDiscordBot().getDiscordUserManager().deleteUser(event.getAuthor().getId()), false);
            Utils.sendChannelMessage(event.getChannel(), DCMessage.UNREGISTER_COMPLETED.getMessage());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}