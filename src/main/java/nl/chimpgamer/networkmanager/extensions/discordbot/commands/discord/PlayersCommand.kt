package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage

class PlayersCommand(private val discordBot: DiscordBot) : Command() {
    override fun execute(event: CommandEvent) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return
        }
        if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_PLAYERS_ENABLED)) {
            return
        }
        sendChannelMessage(event.textChannel,
                discordBot.messages.getString(DCMessage.COMMAND_ONLINEPLAYERS_RESPONSE)
                        .replace("%mention%", event.author.asMention)
                        .replace("%players%", discordBot.networkManager.onlinePlayersCount.toString()))
    }

    init {
        name = discordBot.commandSettings.getString(CommandSetting.DISCORD_PLAYERS_COMMAND)
        aliases = arrayOf("onlineplayers", "online")
        botPermissions = arrayOf(Permission.MESSAGE_WRITE)
        guildOnly = true
    }
}