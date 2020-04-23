package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage
import java.lang.management.ManagementFactory

class UptimeCommand(private val discordBot: DiscordBot) : Command() {
    override fun execute(event: CommandEvent) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return
        }
        if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_UPTIME_ENABLED)) {
            return
        }
        val bungeeuptime = ManagementFactory.getRuntimeMXBean().startTime
        sendChannelMessage(event.textChannel,
                TimeUtils.getTimeString(1, (System.currentTimeMillis() - bungeeuptime) / 1000))
    }

    init {
        name = discordBot.commandSettings.getString(CommandSetting.DISCORD_UPTIME_COMMAND)
        aliases = arrayOf("onlinetime")
        botPermissions = arrayOf(Permission.MESSAGE_WRITE)
        guildOnly = true
    }
}