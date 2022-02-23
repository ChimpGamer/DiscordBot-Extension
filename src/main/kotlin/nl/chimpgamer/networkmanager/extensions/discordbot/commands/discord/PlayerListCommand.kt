package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage

class PlayerListCommand(private val discordBot: DiscordBot) : Command() {
    override fun execute(event: CommandEvent) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return
        }
        if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_PLAYERLIST_ENABLED)) {
            return
        }
        if (event.args.isEmpty()) {
            val sb = StringBuilder()
            val players = discordBot.networkManager.cacheManager.cachedPlayers.players.values
            if (players.isEmpty()) {
                sb.append("There are currently no players online!")
            } else {
                for (player in players) {
                    if (!player.isOnline) continue
                    if (player.vanished) continue
                    sb.append(player.name).append(" - ").append(player.server).append("\n")
                }
            }
            val playerList = sb.toString().trim()
            sendChannelMessage(event.textChannel, playerList)
            return
        }
        val args = event.args.split(" ").toTypedArray()
        if (args.size == 1) {
            val serverName = args[0]
            if (!discordBot.networkManager.getAllServerNames().contains(serverName)) {
                sendChannelMessage(
                    event.textChannel, discordBot.messages.getString(DCMessage.COMMAND_PLAYERLIST_INVALID_SERVER)
                        .replace("%mention%", event.author.asMention)
                        .replace("%server%", serverName)
                )
            }
            var sb = StringBuilder()
            for (player in discordBot.networkManager.cacheManager.cachedPlayers.players.values) {
                if (player.vanished) continue
                sb.append(player.name).append(" - ").append(player.server).append("\n")
            }
            if (sb.isNotEmpty()) {
                sb = StringBuilder(sb.toString().substring(0, sb.toString().length - 2))
            }
            val playerList = sb.toString().trim()
            sendChannelMessage(event.textChannel, playerList)
        }
    }

    init {
        name = discordBot.commandSettings.getString(CommandSetting.DISCORD_PLAYERLIST_COMMAND)
        botPermissions = arrayOf(Permission.MESSAGE_WRITE)
        guildOnly = true
    }
}