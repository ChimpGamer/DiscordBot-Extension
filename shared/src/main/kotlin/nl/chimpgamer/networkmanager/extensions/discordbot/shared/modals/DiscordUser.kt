package nl.chimpgamer.networkmanager.extensions.discordbot.shared.modals

import java.util.UUID

data class DiscordUser(val uuid: UUID, val discordId: String, val registered: Long = 0)