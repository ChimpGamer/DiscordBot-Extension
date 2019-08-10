package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord;

import com.google.common.base.Preconditions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.core.entities.ChannelType;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting;
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordUserManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.CreateTokenTask;

import java.sql.SQLException;

public class RegisterCommand extends Command {
    private final DiscordBot discordBot;

    public RegisterCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.name = CommandSetting.DISCORD_REGISTER_COMMAND.getAsString();
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        final DiscordUserManager discordUserManager = this.getDiscordBot().getDiscordUserManager();
        if (!event.isFromType(ChannelType.PRIVATE)) {
            return;
        }
        try {
            Preconditions.checkNotNull(this.getDiscordBot().getGuild(), "The discord bot has not been connected to a discord server. Connect it to a discord server.");

            if (this.getDiscordBot().getGuild().getMember(event.getAuthor()) == null) {
                Utils.sendChannelMessage(event.getChannel(), DCMessage.REGISTRATION_NOT_IN_SERVER.getMessage());
                return;
            }
            if (!discordUserManager.containsDiscordID(event.getAuthor().getId()) && !this.getDiscordBot().getDiscordUserManager().checkUserByDiscordId(event.getAuthor().getId())) {
                CreateTokenTask createTokenTask = new CreateTokenTask(this.getDiscordBot(), event.getChannel(), event.getAuthor().getId());
                this.getDiscordBot().getScheduler().runAsync(createTokenTask, false);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}