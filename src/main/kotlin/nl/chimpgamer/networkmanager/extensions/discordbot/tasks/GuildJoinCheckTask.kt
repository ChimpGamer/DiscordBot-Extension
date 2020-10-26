package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.entities.Member
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import java.sql.SQLException
import java.util.*

class GuildJoinCheckTask(private val discordBot: DiscordBot, private val member: Member) : Runnable {

    override fun run() {
        val cachedPlayers = discordBot.networkManager.cacheManager.cachedPlayers
        discordBot.networkManager.storage.connection.use { connection ->
            try {
                connection.prepareStatement("SELECT DiscordID, UUID FROM nm_discordusers WHERE DiscordID=?;").use { ps ->
                    ps.setString(1, member.user.id)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            val player = cachedPlayers.getPlayer(UUID.fromString(rs.getString("UUID"))) ?: return
                            if (discordBot.settings.getBoolean(Setting.DISCORD_REGISTER_ADD_ROLE_ENABLED)) {
                                val verifiedRole = discordBot.discordManager.verifiedRole
                                if (verifiedRole != null) {
                                    discordBot.logger.info("Assigning the ${verifiedRole.name} role to ${member.effectiveName}")
                                    discordBot.discordManager.addRoleToMember(member, verifiedRole)
                                }
                            }
                            if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME)) {
                                discordBot.discordManager.setNickName(member, player.name)
                            }
                            if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
                                discordBot.scheduler.runSync(SyncRanksTask(discordBot, player))
                            }
                        } else {
                            val welcomeMessage = discordBot.messages.getString(DCMessage.EVENT_WELCOME)
                            if (welcomeMessage.isEmpty()) {
                                return
                            }
                            member.user.openPrivateChannel().queue { it.sendMessage(welcomeMessage.replace("%mention%", member.user.asTag)).queue() }
                        }
                    }
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }
    }
}