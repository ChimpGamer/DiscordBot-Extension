package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import cloud.commandframework.annotations.CommandMethod
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage

class CloudNetworkManagerBotCommand(private val discordBot: DiscordBot) {

    @CommandMethod("networkmanagerbot|nmbot reload config")
    fun networkManagerBotCommandReloadConfig(player: Player) {
        discordBot.settings.reload()
        player.sendMessage(discordBot.messages.getString(MCMessage.RELOAD_CONFIG))
    }

    @CommandMethod("networkmanagerbot|nmbot reload messages")
    fun networkManagerBotCommandReloadMessages(player: Player) {
        discordBot.messages.reload()
        player.sendMessage(discordBot.messages.getString(MCMessage.RELOAD_MESSAGES))
    }

    @CommandMethod("networkmanagerbot|nmbot reload jda")
    fun networkManagerBotCommandReloadJDA(player: Player) {
        val success = discordBot.discordManager.restartJDA()
        if (success) {
            player.sendMessage(discordBot.messages.getString(MCMessage.RELOAD_JDA_SUCCESS))
        } else {
            player.sendMessage(discordBot.messages.getString(MCMessage.RELOAD_JDA_FAILED))
        }
    }
}