package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import nl.chimpgamer.networkmanager.api.utils.TimeUtils;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import java.lang.management.ManagementFactory;

public class UptimeCommand extends Command {
    private final DiscordBot discordBot;

    public UptimeCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.name = "uptime";
        this.aliases = new String[]{"onlinetime"};
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
        long bungeeuptime = ManagementFactory.getRuntimeMXBean().getStartTime();

        Utils.sendChannelMessage(event.getTextChannel(),
                TimeUtils.getTimeString(1, (System.currentTimeMillis() - bungeeuptime) / 1000));
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}
