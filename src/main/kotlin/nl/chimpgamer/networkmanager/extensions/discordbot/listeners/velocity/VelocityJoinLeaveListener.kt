package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.SyncRanksTask

class VelocityJoinLeaveListener(private val discordBot: DiscordBot) {

    @Subscribe
    fun onPostLogin(event: PostLoginEvent) {
        val player = discordBot.networkManager.cacheManager.cachedPlayers.getIfLoaded(event.player.uniqueId) ?: return

        val discordId = discordBot.discordUserManager.getDiscordIdByUuid(player.uuid) ?: return
        checkNotNull(discordBot.guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
        val member = discordBot.guild.getMemberById(discordId) ?: return
        if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME_ENABLED)) {
            val format = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
            discordBot.discordManager.setNickName(member, format)
        }
        if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
            discordBot.scheduler.runSync(SyncRanksTask(discordBot, player))
        }
    }
}