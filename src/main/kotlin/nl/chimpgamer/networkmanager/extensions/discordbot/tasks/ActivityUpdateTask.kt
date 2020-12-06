package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

class ActivityUpdateTask(private val discordBot: DiscordBot) : Runnable {
    private var taskId = 0
    private var lastAmount = 0

    fun start() {
        taskId = discordBot.scheduler.runRepeating(this, 5)
    }

    fun stop() {
        discordBot.scheduler.stopRepeating(taskId)
    }

    override fun run() {
        val playerCount = discordBot.networkManager.onlinePlayersCount
        if (playerCount != lastAmount) {
            discordBot.discordManager.updateActivity(playerCount)
            lastAmount = playerCount
        }
    }
}