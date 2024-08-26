package nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners.modals

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.utils.data.DataObject
import nl.chimpgamer.networkmanager.api.event.events.ticket.TicketCreateEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.parsePlaceholdersToFields
import kotlin.jvm.optionals.getOrNull

class TicketModalListener(private val discordBot: DiscordBot) : CoroutineEventListener {

    override suspend fun onEvent(event: GenericEvent) {
        if (event is ModalInteractionEvent) {
            if (event.modalId != "create-ticket") return
            val member = event.member ?: return
            val playerUUID = discordBot.discordUserManager.getUuidByDiscordId(member.id) ?: return
            val playerName = discordBot.networkManager.cacheManager.cachedPlayers.getName(playerUUID) ?: return

            val title = event.getValue("title")?.asString ?: return
            val message = event.getValue("message")?.asString ?: return

            val cachedTickets = discordBot.networkManager.cacheManager.cachedTickets
            val ticket = cachedTickets.createTicketBuilder()
                .creator(playerUUID)
                .creatorName(playerName)
                .title(title)
                .message(message)
                .creation(System.currentTimeMillis())
                .build()

            val updatedTicket = cachedTickets.createTicket(ticket)
            val ticketCreateEvent = TicketCreateEvent(updatedTicket, false)
            discordBot.platform.eventBus.post(ticketCreateEvent)
            val data = DataObject.fromJson(discordBot.messages.discordCommandTicketSuccess)
            val embedBuilder = EmbedBuilder.fromData(data).apply {
                val embedTitle = data.getString("title", null)
                    ?.replace("%discord_member_name%", member.user.name)
                    ?.replace("%ticket_id%", updatedTicket.id.toString())
                    ?.replace("%ticket_title%", updatedTicket.title)
                    ?.replace("%ticket_message%", updatedTicket.message)
                val embedDescription = data.getString("description", null)
                    ?.replace("%discord_member_name%", member.user.name)
                    ?.replace("%ticket_id%", updatedTicket.id.toString())
                    ?.replace("%ticket_title%", updatedTicket.title)
                    ?.replace("%ticket_message%", updatedTicket.message)
                data.optObject("thumbnail").map { it.getString("url") }.getOrNull()
                val thumbnail = data.optObject("thumbnail").map { it.getString("url") }.getOrNull()
                    ?.replace("%discord_member_effective_avatar_url%", member.effectiveAvatarUrl)
                    ?.replace("%ticket_id%", updatedTicket.id.toString())
                    ?.replace("%ticket_title%", updatedTicket.title)
                    ?.replace("%ticket_message%", updatedTicket.message)
                setTitle(embedTitle)
                setDescription(embedDescription)
                setThumbnail(thumbnail)
                parsePlaceholdersToFields { text ->
                    text
                            .replace("%discord_member_name%", member.user.name)
                            .replace("%discord_member_mention%", member.asMention)
                            .replace("%discord_member_roles%", member.roles.joinToString { it.name })
                            .replace("%discord_member_highest_role%", member.roles.maxByOrNull { it.position }?.name ?: "No Roles")
                            .replace("%ticket_id%", updatedTicket.id.toString())
                            .replace("%ticket_title%", updatedTicket.title)
                            .replace("%ticket_message%", updatedTicket.message)
                }
            }

            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).await()
        }
    }
}