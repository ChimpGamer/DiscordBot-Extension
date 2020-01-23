package nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord

import com.google.common.base.Preconditions
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
        if (event.author.isBot) {
            return
        }
        val discordUserManager = discordBot.discordUserManager
        if (!event.isFromType(ChannelType.PRIVATE)) {
            return
        }
        try {
            Preconditions.checkNotNull(discordBot.guild, "The discord bot has not been connected to a discord server. Connect it to a discord server.")
            if (discordBot.guild.getMember(event.author) == null) {
                sendChannelMessage(event.channel, DCMessage.REGISTRATION_NOT_IN_SERVER.message)
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
        name = CommandSetting.DISCORD_REGISTER_COMMAND.asString
        aliases = arrayOf(discordBot.commandSettings.getString(CommandSetting.DISCORD_REGISTER_ALIASES.path))
        guildOnly = false
    }
}