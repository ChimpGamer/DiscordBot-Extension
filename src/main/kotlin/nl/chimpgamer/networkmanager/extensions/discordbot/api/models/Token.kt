package nl.chimpgamer.networkmanager.extensions.discordbot.api.models

import net.dv8tion.jda.api.interactions.InteractionHook

interface Token {
    val token: String
    val discordID: String
    val created: Long
    val interaction: InteractionHook
}