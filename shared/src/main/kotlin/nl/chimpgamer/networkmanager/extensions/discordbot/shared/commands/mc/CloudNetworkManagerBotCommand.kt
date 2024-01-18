package nl.chimpgamer.networkmanager.extensions.discordbot.shared.commands.mc

import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.MCMessage

//TODO: Get rid of annotations
class CloudNetworkManagerBotCommand(private val discordBot: DiscordBot) {

    @CommandMethod("networkmanagerbot|nmbot reload config")
    @CommandPermission("networkmanagerbot.command.reload")
    fun networkManagerBotCommandReloadConfig(player: Player) {
        discordBot.settings.reload()
        player.sendRichMessage(discordBot.messages.getString(MCMessage.RELOAD_CONFIG))
    }

    @CommandMethod("networkmanagerbot|nmbot reload messages")
    @CommandPermission("networkmanagerbot.command.reload")
    fun networkManagerBotCommandReloadMessages(player: Player) {
        discordBot.messages.reload()
        player.sendRichMessage(discordBot.messages.getString(MCMessage.RELOAD_MESSAGES))
    }

    @CommandMethod("networkmanagerbot|nmbot reload jda")
    @CommandPermission("networkmanagerbot.command.reload")
    fun networkManagerBotCommandReloadJDA(player: Player) {
        val success = discordBot.discordManager.restartJDA()
        if (success) {
            player.sendRichMessage(discordBot.messages.getString(MCMessage.RELOAD_JDA_SUCCESS))
        } else {
            player.sendRichMessage(discordBot.messages.getString(MCMessage.RELOAD_JDA_FAILED))
        }
    }
}