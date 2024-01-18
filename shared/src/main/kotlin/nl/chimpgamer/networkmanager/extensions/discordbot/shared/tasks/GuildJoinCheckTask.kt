package nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks

import net.dv8tion.jda.api.entities.Member
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Setting
import java.sql.SQLException
import java.util.UUID

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

                            val syncUsername = discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME_ENABLED)
                            val addVerifiedRole = discordBot.settings.getBoolean(Setting.DISCORD_REGISTER_ADD_ROLE_ENABLED) && discordBot.discordManager.verifiedRole != null

                            if (syncUsername && addVerifiedRole) {
                                val nickname = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
                                discordBot.discordManager.setNickNameAndAddRole(member, nickname, discordBot.discordManager.verifiedRole!!)
                            } else {
                                if (syncUsername) {
                                    val nickname = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
                                    discordBot.discordManager.setNickName(member, nickname)
                                }
                                if (addVerifiedRole) {
                                    discordBot.discordManager.verifiedRole?.let { discordBot.discordManager.addRoleToMember(member, it) }
                                }
                            }

                            if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
                                discordBot.scheduler.runSync(SyncRanksTask(discordBot, player))
                            }
                        } else {
                            val welcomeMessage = discordBot.messages.getString(DCMessage.EVENT_JOIN)
                            if (welcomeMessage.isEmpty()) {
                                return
                            }
                            member.user.openPrivateChannel().queue { it.sendMessage(welcomeMessage.replace("%mention%", member.user.asMention)).queue() }
                        }
                    }
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }
    }
}