package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import nl.chimpgamer.networkmanager.api.communication.IMySQL
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.sql.Connection
import java.sql.SQLException

class MySQL(discordBot: DiscordBot) {
    private var nmMySQL: IMySQL? = null

    @Throws(SQLException::class)
    private fun create() {
        val nmDiscordUsers = "CREATE TABLE IF NOT EXISTS nm_discordusers(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, UUID VARCHAR(36), DiscordID VARCHAR(64), registered LONG) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_unicode_ci;"
        createTable(nmDiscordUsers)
    }

    @Throws(SQLException::class)
    private fun createTable(sql: String) {
        nmMySQL!!.connection.use { connection -> connection.prepareStatement(sql).use { ps -> ps.executeUpdate() } }
    }

    @get:Throws(SQLException::class)
    val connection: Connection
            get() = nmMySQL!!.connection

    init {
        if (discordBot.networkManager.mySQL.ping()) {
            discordBot.networkManager.debug("&c| &cSuccessfully connected with MySQL! (NetworkManagerBot)")
            nmMySQL = discordBot.networkManager.mySQL
            try {
                create()
                discordBot.networkManager.debug("&c| &cDone with Creating MySQL things! (NetworkManagerBot)")
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        } else {
            discordBot.error("&c| &cNo connection to Database (NetworkManagerBot)")
        }
    }
}