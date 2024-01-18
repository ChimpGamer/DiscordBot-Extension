package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import nl.chimpgamer.networkmanager.api.storage.Storage
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.sql.Connection
import java.sql.SQLException

class MySQL(private val discordBot: DiscordBot) {
    lateinit var nmStorage: Storage

    fun initialize(): Boolean {
        var result = false
        if (discordBot.networkManager.storage.ping()) {
            discordBot.networkManager.debug("&c| &cSuccessfully connected with MySQL! (NetworkManagerBot)")
            nmStorage = discordBot.networkManager.storage
            try {
                create()
                discordBot.networkManager.debug("&c| &cDone with Creating MySQL things! (NetworkManagerBot)")
                result = true
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        } else {
            discordBot.error("&c| &cNo connection to Database (NetworkManagerBot)")
        }
        return result
    }

    @Throws(SQLException::class)
    private fun create() {
        val nmDiscordUsers = "CREATE TABLE IF NOT EXISTS nm_discordusers(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, UUID VARCHAR(36), DiscordID VARCHAR(64), registered LONG) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_unicode_ci;"
        nmStorage.createTable(nmDiscordUsers)
    }

    @get:Throws(SQLException::class)
    val connection: Connection
            get() = nmStorage.connection
}