package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee

import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.SyncRanksTask

class JoinLeaveListener(private val discordBot: DiscordBot) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onLogin(event: PostLoginEvent) {
        val proxiedPlayer = event.player ?: return
        val player = discordBot.networkManager.getPlayer(proxiedPlayer.uniqueId)
        if (player == null) {
            discordBot.logger.info("Could not load player...")
            return
        }
        val discordId = discordBot.discordUserManager.getDiscordIdByUuid(proxiedPlayer.uniqueId) ?: return
        checkNotNull(discordBot.guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
        val member = discordBot.guild.getMemberById(discordId) ?: return
        if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME)) {
            val format = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
            discordBot.discordManager.setNickName(member, format)
        }
        if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
            discordBot.scheduler.runSync(SyncRanksTask(discordBot, player))
        }
    }
}