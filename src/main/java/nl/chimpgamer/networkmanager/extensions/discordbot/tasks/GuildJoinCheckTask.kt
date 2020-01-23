package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.PrivateChannel
import nl.chimpgamer.networkmanager.api.cache.modules.CachedPlayers
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.addRoleToMember
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.setNickName
import java.sql.SQLException
import java.util.*

class GuildJoinCheckTask(private val discordBot: DiscordBot, private val member: Member) : Runnable {

    override fun run() {
        val cachedPlayers: CachedPlayers = discordBot.networkManager.cacheManager.cachedPlayers
        try {
            discordBot.networkManager.mySQL.connection.use { connection ->
                connection.prepareStatement("SELECT DiscordID, UUID FROM nm_discordusers WHERE DiscordID=?;").use { ps ->
                    ps.setString(1, member.user.id)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            val player = cachedPlayers.getPlayer(UUID.fromString(rs.getString("UUID"))) ?: return
                            if (Setting.DISCORD_REGISTER_ADD_ROLE_ENABLED.asBoolean) {
                                val verifiedRole = discordBot.discordManager.verifiedRole
                                if (verifiedRole != null) {
                                    discordBot.logger.info("Assigning the " + verifiedRole.name + " role to " + member.effectiveName)
                                    addRoleToMember(member, verifiedRole)
                                }
                            }
                            if (Setting.DISCORD_SYNC_USERNAME.asBoolean) {
                                setNickName(member, player.name)
                            }
                            if (Setting.DISCORD_SYNC_RANKS_ENABLED.asBoolean) {
                                discordBot.scheduler.runSync(SyncRanksTask(discordBot, player))
                            }
                        } else {
                            val welcomeMessage = DCMessage.WELCOME_NEW_MEMBER.message
                            if (welcomeMessage.isEmpty()) {
                                return
                            }
                            member.user.openPrivateChannel().queue { channel: PrivateChannel -> channel.sendMessage(welcomeMessage.replace("%mention%", member.user.asTag)).queue() }
                        }
                    }
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }
}