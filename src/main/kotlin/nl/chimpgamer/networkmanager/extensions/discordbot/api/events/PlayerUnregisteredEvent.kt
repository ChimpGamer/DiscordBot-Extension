package nl.chimpgamer.networkmanager.extensions.discordbot.api.events

import net.dv8tion.jda.api.entities.Member
import nl.chimpgamer.networkmanager.api.event.Event
import nl.chimpgamer.networkmanager.api.models.player.Player

class PlayerUnregisteredEvent(val player: Player, val member: Member?) : Event()