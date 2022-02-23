package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.ChannelType
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.CreateTokenTask
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.sendChannelMessage
import java.sql.SQLException

class RegisterCommand(private val discordBot: DiscordBot) : Command() {
    override fun execute(event: CommandEvent) {
        if (event.author.isBot) return
        if (!event.isFromType(ChannelType.PRIVATE)) return

        val discordUserManager = discordBot.discordUserManager
        try {
            checkNotNull(discordBot.guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
            if (discordBot.guild.getMember(event.author) == null) {
                sendChannelMessage(event.channel, discordBot.messages.getString(DCMessage.REGISTRATION_NOT_IN_SERVER))
                return
            }
            if (!discordUserManager.containsDiscordID(event.author.id) && !discordBot.discordUserManager.checkUserByDiscordId(event.author.id)) {
                val createTokenTask = CreateTokenTask(discordBot, event.channel, event.author.id)
                discordBot.scheduler.runAsync(createTokenTask, false)
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }

    init {
        name = discordBot.commandSettings.getString(CommandSetting.DISCORD_REGISTER_COMMAND)
        aliases = arrayOf(discordBot.commandSettings.getString(CommandSetting.DISCORD_REGISTER_ALIASES))
        guildOnly = false
    }
}