package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import nl.chimpgamer.networkmanager.api.utils.TimeUtils;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import java.lang.management.ManagementFactory;

public class UptimeCommand extends Command {

    public UptimeCommand() {
        this.name = CommandSetting.DISCORD_UPTIME_COMMAND.getAsString();
        this.aliases = new String[]{"onlinetime"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        if (!CommandSetting.DISCORD_UPTIME_ENABLED.getAsBoolean()) {
            return;
        }
        long bungeeuptime = ManagementFactory.getRuntimeMXBean().getStartTime();

        Utils.sendChannelMessage(event.getTextChannel(),
                TimeUtils.getTimeString(1, (System.currentTimeMillis() - bungeeuptime) / 1000));
    }
}