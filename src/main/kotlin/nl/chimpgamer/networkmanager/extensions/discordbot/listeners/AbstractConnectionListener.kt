package nl.chimpgamer.networkmanager.extensions.discordbot.listeners

import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.SyncRanksTask
import java.util.UUID

abstract class AbstractConnectionListener(private val discordBot: DiscordBot) {

    fun onLogin(uuid: UUID) {
        val player = discordBot.networkManager.getPlayer(uuid)
        if (player == null) {
            discordBot.logger.info("Could not load player...")
            return
        }
        val discordId = discordBot.discordUserManager.getDiscordIdByUuid(uuid) ?: return
        checkNotNull(discordBot.guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
        val member = discordBot.guild.getMemberById(discordId) ?: return
        if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME_ENABLED)) {
            val format = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
            discordBot.discordManager.setNickName(member, format)
        }
        if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
            // Was sync before. Trying to run it async now.
            discordBot.scheduler.runAsync(SyncRanksTask(discordBot, player), false)
        }
    }
}