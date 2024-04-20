package nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners

import litebans.api.Entry
import litebans.api.Events
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.data.DataObject
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.api.utils.stripColors
import nl.chimpgamer.networkmanager.api.values.Message
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.parsePlaceholdersToFields
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class LiteBansListener(private val discordBot: DiscordBot) {

    private val eventsListener = object : Events.Listener() {
        override fun entryAdded(entry: Entry?) {
            if (entry == null) return

            val dcMessage = DCMessage.PUNISHMENT_ALERT
            sendMessageToPunishmentChannel(entry, dcMessage)
        }

        override fun entryRemoved(entry: Entry?) {
            if (entry == null) return

            val dcMessage = DCMessage.UNPUNISHMENT_ALERT
            sendMessageToPunishmentChannel(entry, dcMessage)
        }
    }

    fun registerListeners() {
        Events.get().register(eventsListener)
    }

    fun unregisterListeners() {
        Events.get().unregister(eventsListener)
    }

    private fun sendMessageToPunishmentChannel(entry: Entry, dcMessage: DCMessage) {
        val punishmentsChannel = discordBot.discordManager.getTextChannelById(Setting.DISCORD_EVENTS_PUNISHMENT_CHANNEL) ?: return

        val data = DataObject.fromJson(discordBot.messages.getString(dcMessage))
        val embedBuilder = EmbedBuilder.fromData(data).apply {
            val title = insertPlaceholders(data.getString("title", null), entry)
            val description = insertPlaceholders(data.getString("description", "").takeIf { it.isNotEmpty() }, entry)
            setTitle(title)
            setDescription(description)
            parsePlaceholdersToFields { text -> insertPlaceholders(text, entry) }
        }

        Utils.sendChannelMessage(punishmentsChannel, embedBuilder.build())
    }

    private fun insertPlaceholders(s: String?, entry: Entry): String {
        if (s == null) return ""
        val networkManager = discordBot.networkManager
        val cachedPlayers = networkManager.cacheManager.cachedPlayers
        val playerUUID: UUID? = try {
            if (entry.uuid != null) UUID.fromString(entry.uuid) else null
        } catch (ex: IllegalArgumentException) {
            null
        }
        val languageId = 1

        val parsed = s
            .replace("%id%", entry.id.toString())
            .replace("%type", entry.type)
            .replace("%uuid%", entry.uuid.toString())
            .replace(
                "%playername%",
                (if (playerUUID != null) cachedPlayers.getIfLoaded(playerUUID)?.name
                    ?: "Player Offline" else "Invalid UUID")
            )
            .replace(
                "%username%",
                (if (playerUUID != null) cachedPlayers.getIfLoaded(playerUUID)?.name
                    ?: "Player Offline" else "Invalid UUID")
            )
            .replace("%ip%", entry.ip.toString())
            .replace("%server%", entry.serverOrigin)
            .replace("%reason%", entry.reason)
            .replace("%unbanreason%", entry.removalReason.toString())
            .replace("%punisher%", entry.executorName.toString())
            .replace(
                "%time%", SimpleDateFormat(networkManager.getMessage(languageId, Message.PUNISHMENT_DATETIME_FORMAT))
                    .format(Date(entry.dateStart))
            )
            .replace(
                "%ends%", SimpleDateFormat(networkManager.getMessage(languageId, Message.PUNISHMENT_DATETIME_FORMAT))
                    .format(Date(entry.dateEnd))
            )
            .replace(
                "%expires%",
                if (entry.dateEnd == -1L) networkManager.getMessage(languageId, Message.NEVER)
                else TimeUtils.getTimeString(
                    languageId,
                    Utils.ceilDiv(entry.dateEnd - System.currentTimeMillis(), 1000)
                )
            )

        return parsed.stripColors()
    }
}