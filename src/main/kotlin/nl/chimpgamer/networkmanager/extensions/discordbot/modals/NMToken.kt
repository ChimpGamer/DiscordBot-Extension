package nl.chimpgamer.networkmanager.extensions.discordbot.modals

import net.dv8tion.jda.api.interactions.InteractionHook
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token

data class NMToken(override val token: String, override val discordID: String, override val interaction: InteractionHook) : Token {
    override val created: Long = System.currentTimeMillis()
}