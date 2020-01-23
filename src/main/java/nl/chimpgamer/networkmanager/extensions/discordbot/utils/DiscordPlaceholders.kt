package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.placeholders.PlaceholderHook
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

class DiscordPlaceholders(private val discordBot: DiscordBot) : PlaceholderHook() {
    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        when (identifier.toLowerCase()) {
            "users" -> return discordBot.discordUserManager.discordUsers.size.toString()
        }
        return null
    }

    override fun getIdentifier(): String {
        return "discordbot"
    }
}