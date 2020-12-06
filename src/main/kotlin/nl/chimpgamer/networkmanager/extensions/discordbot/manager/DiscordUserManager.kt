package nl.chimpgamer.networkmanager.extensions.discordbot.manager

import net.dv8tion.jda.api.entities.Message
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.DiscordUser
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.NMToken
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.DeleteUserTask
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.TokenExpiryTask
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.VerifyUserTask
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DiscordUserManager(private val discordBot: DiscordBot) {
    val discordUsers: MutableMap<UUID, DiscordUser> = ConcurrentHashMap()
    val tokens: MutableList<Token> = ArrayList()

    fun load() {
        discordBot.scheduler.runAsync({
            try {
                discordBot.mySQL.connection.use { connection ->
                    connection.prepareStatement("SELECT `UUID`, `DiscordID`, `registered` FROM nm_discordusers;").use { ps ->
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val uuid = UUID.fromString(rs.getString("UUID"))
                                val discordId = rs.getString("DiscordID")
                                val registered = rs.getLong("registered")
                                discordUsers[uuid] = DiscordUser(uuid, discordId, registered)
                            }
                        }
                    }
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }, false)
    }

    fun load(uuid: UUID) {
        discordBot.scheduler.runAsync({
            try {
                discordBot.mySQL.connection.use { connection ->
                    connection.prepareStatement("SELECT `UUID`, `DiscordID`, `registered` FROM nm_discordusers WHERE `UUID`=?;").use { ps ->
                        ps.setString(1, uuid.toString())
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val discordId = rs.getString("DiscordID")
                                val registered = rs.getLong("registered")
                                discordUsers[uuid] = DiscordUser(uuid, discordId, registered)
                            }
                        }
                    }
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }, false)
    }

    fun deleteUser(player: Player) {
        val dut = DeleteUserTask(discordBot, player)
        discordBot.scheduler.runAsync(dut, false)
    }

    @Throws(SQLException::class)
    fun insertUser(uuid: UUID, discordID: String) {
        val now = System.currentTimeMillis()
        discordBot.mySQL.connection.use { connection ->
            connection.prepareStatement("INSERT INTO nm_discordusers (UUID, DiscordID, registered) VALUES (?, ?, ?);").use { ps ->
                ps.setString(1, uuid.toString())
                ps.setString(2, discordID)
                ps.setLong(3, now)
                ps.executeUpdate()
            }
        }
        discordUsers[uuid] = DiscordUser(uuid, discordID, now)
    }

    @Throws(SQLException::class)
    fun deleteUserFromDatabase(uuid: UUID) {
        discordBot.mySQL.connection.use { connection ->
            connection.prepareStatement("DELETE FROM nm_discordusers WHERE UUID=?;").use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeUpdate()
            }
        }
    }

    @Throws(SQLException::class)
    fun existsInDatabase(id: String): Boolean {
        val isUUID = Utils.UUID_REGEX.containsMatchIn(id)
        discordBot.mySQL.connection.use { connection ->
            connection.prepareStatement("SELECT 1 FROM nm_discordusers WHERE " + if (isUUID) "UUID=?" else "DiscordID=?" + ";").use { ps ->
                ps.setString(1, id)
                return ps.executeQuery().next()
            }
        }
    }

    @Throws(SQLException::class)
    fun checkUserByDiscordId(discordId: String): Boolean {
        if (getUuidByDiscordId(discordId) == null) {
            discordBot.mySQL.connection.use { connection ->
                connection.prepareStatement("SELECT 1 FROM nm_discordusers WHERE DiscordID=?;").use { ps ->
                    ps.setString(1, discordId)
                    return ps.executeQuery().next()
                }
            }
        } else {
            return true
        }
    }

    fun getDiscordIdByUuid(uuid: UUID): String? {
        val discordUser = discordUsers[uuid]
        return discordUser?.discordId
    }

    fun getUuidByDiscordId(id: String): UUID? {
        return discordUsers.entries.filter { it.value.discordId == id }.map { it.key }.firstOrNull()
    }

    private fun getToken(token: String): Token? {
        return tokens.firstOrNull { it.token.equals(token, ignoreCase = true) }
    }

    fun verifyUser(player: Player, inputToken: String) {
        val token = getToken(inputToken) ?: return
        val vut = VerifyUserTask(discordBot, player, token)
        discordBot.scheduler.runAsync(vut, false)
    }

    fun insertToken(inputToken: String, uuid: String, message: Message) {
        val token: Token = NMToken(inputToken, uuid, message)
        tokens.add(token)
        discordBot.scheduler.runDelayed(TokenExpiryTask(discordBot, token), 60L)
    }

    fun containsDiscordID(discordID: String): Boolean {
        return tokens.any { it.discordID == discordID }
    }
}