package nl.chimpgamer.networkmanager.extensions.discordbot.shared.commands.mc

import cloud.commandframework.CommandManager
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.MCMessage

class CloudNetworkManagerBotCommand(private val discordBot: DiscordBot) {

    fun registerCommands(commandManager: CommandManager<Sender>) {
        val builder = commandManager.commandBuilder("networkmanagerbot", "nmbot")

        commandManager.command(builder
            .literal("reload")
            .literal("config")
            .permission("networkmanagerbot.command.reload")
            .handler { context ->
                val sender = context.sender
                discordBot.settings.reload()
                sender.sendRichMessage(discordBot.messages.getString(MCMessage.RELOAD_CONFIG))
            }
        )

        commandManager.command(builder
            .literal("reload")
            .literal("messages")
            .permission("networkmanagerbot.command.reload")
            .handler { context ->
                val sender = context.sender
                discordBot.messages.reload()
                sender.sendRichMessage(discordBot.messages.getString(MCMessage.RELOAD_MESSAGES))
            }
        )

        commandManager.command(builder
            .literal("reload")
            .literal("jda")
            .permission("networkmanagerbot.command.reload")
            .handler { context ->
                val sender = context.sender
                val success = discordBot.discordManager.restartJDA()
                if (success) {
                    sender.sendRichMessage(discordBot.messages.getString(MCMessage.RELOAD_JDA_SUCCESS))
                } else {
                    sender.sendRichMessage(discordBot.messages.getString(MCMessage.RELOAD_JDA_FAILED))
                }
            }
        )
    }
}