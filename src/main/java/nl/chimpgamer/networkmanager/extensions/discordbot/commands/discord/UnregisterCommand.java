package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Role;
import nl.chimpgamer.networkmanager.common.utils.Methods;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import java.sql.SQLException;

public class UnregisterCommand extends Command {
    private final DiscordBot discordBot;

    public UnregisterCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.name = CommandSetting.DISCORD_UNREGISTER_COMMAND.getAsString();
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
            String message = DCMessage.UNREGISTER_COMPLETED.getMessage();
            if (Methods.isJsonValid(message)) {
                JsonEmbedBuilder jsonEmbedBuilder = JsonEmbedBuilder.fromJson(message);
                Utils.sendChannelMessage(event.getChannel(), jsonEmbedBuilder.build());
            } else {
                Utils.sendChannelMessage(event.getChannel(), message);
            }

            if (Setting.DISCORD_VERIFY_ADD_ROLE_ENABLED.getAsBoolean()) {
                Role verifiedRole = this.getDiscordBot().getDiscordManager().getVerifiedRole();
                if (verifiedRole != null) {
                    this.getDiscordBot().getGuild().getController().removeRolesFromMember(this.getDiscordBot().getGuild().getMemberById(event.getAuthor().getId()), verifiedRole).queue();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}