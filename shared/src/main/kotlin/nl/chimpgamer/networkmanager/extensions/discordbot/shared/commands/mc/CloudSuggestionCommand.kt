package nl.chimpgamer.networkmanager.extensions.discordbot.shared.commands.mc

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.data.DataObject
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.models.sender.Sender
import nl.chimpgamer.networkmanager.api.utils.Cooldown
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.api.utils.adventure.parse
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.parsePlaceholdersToFields
import org.incendo.cloud.CommandManager
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser

class CloudSuggestionCommand(private val discordBot: DiscordBot) {

    fun registerCommands(commandManager: CommandManager<Sender>, name: String, vararg aliases: String) {
        commandManager.command(
            commandManager.commandBuilder(name, *aliases)
                .senderType(Player::class.java)
                .required("message", greedyStringParser())
                .handler { context ->
                    val player = context.sender()
                    val message = context.get<String>("message")

                    if (Cooldown.isInCooldown(player.uuid, name)) {
                        player.sendMessage(
                            discordBot.messages.getString(MCMessage.SUGGESTION_COOLDOWN).parse(
                                mapOf(
                                    "cooldown" to TimeUtils.getTimeString(
                                        player.language,
                                        Cooldown.getTimeLeft(player.uuid, name).toLong()
                                    )
                                )
                            )
                        )
                        return@handler
                    }

                    val suggestionsChannel = discordBot.discordManager.getTextChannelById(Setting.DISCORD_EVENTS_SUGGESTION_CHANNEL)
                    if (suggestionsChannel == null) {
                        player.sendRichMessage("<red>Invalid TextChannel Id")
                        return@handler
                    }

                    val data = DataObject.fromJson(discordBot.messages.getString(DCMessage.SUGGESTION_ALERT))
                    val embedBuilder = EmbedBuilder.fromData(data).apply {
                        val title = data.getString("title", null)?.replace("%playername%", player.name)
                        setTitle(title)
                        parsePlaceholdersToFields { text ->
                            Placeholders.setPlaceholders(
                                player, text
                                    .replace("%suggestion%", message)
                                    .replace("%server%", player.server ?: "null")
                            )
                        }
                    }

                    Utils.sendChannelMessage(suggestionsChannel, embedBuilder.build())
                    player.sendRichMessage(discordBot.messages.getString(MCMessage.SUGGESTION_SUCCESS))
                    Cooldown(
                        player.uuid,
                        name,
                        discordBot.commandSettings.minecraftSuggestionCooldown
                    ).start()
                }
        )
    }
}