package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files

class DependencyDownloader(val discordBot: DiscordBot) {
    fun downloadDependency(url: String, name: String, fileName: String) {
        val localPath = discordBot.networkManager.dataFolder.path + File.separator + "lib" + File.separator + fileName + ".jar"
        val file = File(localPath)
        if (!file.exists()) {
            discordBot.logger.info("Downloading $name ...")
            try {
                downloadFile(url, localPath)
            } catch (ex: IOException) {
                discordBot.logger.severe("An error occured while downloading a required lib.")
                ex.printStackTrace()
            }
        }
        discordBot.logger.info("Loading dependency $name ...")
        discordBot.networkManager.pluginClassLoader.loadJar(file.toPath())
    }

    @Throws(IOException::class)
    private fun downloadFile(url: String, location: String) {
        val website = URL(url)
        val connection = website.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)")
        val yourFile = File(location)
        yourFile.parentFile.mkdirs()
        Files.copy(connection.inputStream, yourFile.toPath())
    }
}