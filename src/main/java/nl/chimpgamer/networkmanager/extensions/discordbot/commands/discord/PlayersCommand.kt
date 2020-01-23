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
        if (!CommandSetting.DISCORD_PLAYERS_ENABLED.asBoolean) {
            return
        }
        sendChannelMessage(event.textChannel,
                DCMessage.PLAYERS_COMMAND_RESPONSE.message
                        .replace("%mention%", event.author.asMention)
                        .replace("%players%", discordBot.networkManager.onlinePlayersCount.toString()))
    }

    init {
        name = CommandSetting.DISCORD_PLAYERS_COMMAND.asString
        aliases = arrayOf("onlineplayers", "online")
        botPermissions = arrayOf(Permission.MESSAGE_WRITE)
        guildOnly = true
    }
}