package nl.chimpgamer.networkmanager.extensions.discordbot.shared.commands.discord

import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.CommandSetting

class RegisterCommand(private val discordBot: DiscordBot) : CoroutineEventListener {

    override suspend fun onEvent(event: GenericEvent) {
        if (event is SlashCommandInteractionEvent) {
            if (event.name == discordBot.commandSettings.getString(CommandSetting.DISCORD_REGISTER_COMMAND)) {

            }
        }
    }
}