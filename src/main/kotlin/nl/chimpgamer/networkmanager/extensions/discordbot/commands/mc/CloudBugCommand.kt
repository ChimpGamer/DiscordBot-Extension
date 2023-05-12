package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc

import cloud.commandframework.Command
import cloud.commandframework.arguments.standard.StringArgument
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.api.utils.Cooldown
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.api.utils.adventure.parse
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils

class CloudBugCommand(private val discordBot: DiscordBot) {

    fun getCommand(name: String, vararg aliases: String): Command.Builder<Sender> {
        val messageArgument = StringArgument.of<Sender>("message", StringArgument.StringMode.GREEDY)
        return discordBot.networkManager.cloudCommandManager.commandManager.commandBuilder(name, *aliases)
            .senderType(Player::class.java)
            .argument(messageArgument)
            .handler { context ->
                val player = context.sender as Player
                val message = context[messageArgument]

                if (Cooldown.isInCooldown(player.uuid, name)) {
                    player.sendMessage(discordBot.messages.getString(MCMessage.BUG_COOLDOWN).parse(mapOf("cooldown" to TimeUtils.getTimeString(player.language, Cooldown.getTimeLeft(player.uuid, name).toLong()))))
                    return@handler
                }

                val bugReportChannel =
                    discordBot.guild.getTextChannelById(discordBot.settings.getString(Setting.DISCORD_EVENTS_BUGREPORT_CHANNEL))
                if (bugReportChannel == null) {
                    player.sendRichMessage("<red>Invalid TextChannel Id")
                    return@handler
                }

                var jsonMessageEmbed = JsonMessageEmbed.fromJson(discordBot.messages.getString(DCMessage.BUGREPORT_ALERT))
                jsonMessageEmbed = jsonMessageEmbed.toBuilder()
                    .title(jsonMessageEmbed.title?.replace("%playername%", player.name))
                    .parsePlaceholdersToFields { text -> Placeholders.setPlaceholders(player, text
                        .replace("%bug%", message)
                        .replace("%server%", player.server ?: "null")) }
                    .build()

                Utils.sendChannelMessage(bugReportChannel, jsonMessageEmbed.toMessageEmbed())
                player.sendRichMessage(discordBot.messages.getString(MCMessage.BUG_SUCCESS))
                Cooldown(player.uuid, name, discordBot.commandSettings.getInt(CommandSetting.MINECRAFT_BUG_COOLDOWN)).start()
            }
    }
}