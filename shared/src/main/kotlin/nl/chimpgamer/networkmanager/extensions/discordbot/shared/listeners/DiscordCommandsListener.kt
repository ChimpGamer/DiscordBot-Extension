package nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.utils.data.DataObject
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.common.utils.ExpiringMap
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks.CreateTokenTask
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.parsePlaceholdersToFields
import java.lang.management.ManagementFactory
import java.sql.SQLException
import java.util.UUID
import java.util.concurrent.TimeUnit

class DiscordCommandsListener(private val discordBot: DiscordBot) : CoroutineEventListener {
    private val playtimeCache = ExpiringMap<UUID, Pair<String, Long>>(1L, TimeUnit.MINUTES)

    override suspend fun onEvent(event: GenericEvent) {
        if (event !is GuildReadyEvent && event !is GuildJoinEvent) return
        val jda = event.jda

        println("Updating Discord Slash Commands")
        handleCommands(jda)
    }

    private suspend fun handleCommands(jda: JDA) {
        val commandSettings = discordBot.commandSettings
        val registerCommandName = commandSettings.getString(CommandSetting.DISCORD_REGISTER_COMMAND)
        val registerCommandDescription = commandSettings.getString(CommandSetting.DISCORD_REGISTER_DESCRIPTION)

        val playerListCommandName = commandSettings.getString(CommandSetting.DISCORD_PLAYERLIST_COMMAND)
        val playerListCommandDescription = commandSettings.getString(CommandSetting.DISCORD_PLAYERLIST_DESCRIPTION)
        val playerListCommandOptionServerName = commandSettings.getString(CommandSetting.DISCORD_PLAYERLIST_OPTIONS_SERVERNAME_NAME)
        val playerListCommandOptionServerNameDescription = commandSettings.getString(CommandSetting.DISCORD_PLAYERLIST_OPTIONS_SERVERNAME_DESCRIPTION)

        val playersCommandName = commandSettings.getString(CommandSetting.DISCORD_PLAYERS_COMMAND)
        val playersCommandDescription = commandSettings.getString(CommandSetting.DISCORD_PLAYERS_DESCRIPTION)

        val playtimeCommandName = commandSettings.getString(CommandSetting.DISCORD_PLAYTIME_COMMAND)
        val playtimeCommandDescription = commandSettings.getString(CommandSetting.DISCORD_PLAYTIME_DESCRIPTION)

        val uptimeCommandName = commandSettings.getString(CommandSetting.DISCORD_UPTIME_COMMAND)
        val uptimeCommandDescription = commandSettings.getString(CommandSetting.DISCORD_UPTIME_DESCRIPTION)

        jda.updateCommands {
            slash(registerCommandName, registerCommandDescription)

            slash(playerListCommandName, playerListCommandDescription) {
                restrict(guild = true, Permission.MESSAGE_SEND)
                option<String>(playerListCommandOptionServerName, playerListCommandOptionServerNameDescription)
            }

            slash(playersCommandName, playersCommandDescription) {
                restrict(guild = true, Permission.MESSAGE_SEND)
            }

            slash(playtimeCommandName, playtimeCommandDescription) {
                restrict(guild = true, Permission.MESSAGE_SEND)
            }

            slash(uptimeCommandName, uptimeCommandDescription) {
                restrict(guild = true, Permission.MESSAGE_SEND)
            }
        }.await()

        jda.onCommand(registerCommandName) { event ->
            val discordUserManager = discordBot.discordUserManager
            try {
                val guild = event.guild
                checkNotNull(guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
                val member = event.member ?: guild.getMember(event.user)
                if (member == null) {
                    event.reply(discordBot.messages.getString(DCMessage.REGISTRATION_NOT_IN_SERVER)).setEphemeral(true).await()
                    return@onCommand
                }
                if (member.isPending) {
                    event.reply(discordBot.messages.getString(DCMessage.REGISTRATION_MEMBERSHIP_SCREENING_REQUIREMENTS_NOT_MET)).setEphemeral(true).await()
                    return@onCommand
                }

                if (discordUserManager.containsDiscordID(member.id)) {
                    val registrationInProcessMessage = discordBot.messages.getString(DCMessage.REGISTRATION_IN_PROCESS)
                    if (Utils.isJsonValid(registrationInProcessMessage)) {

                        val data = DataObject.fromJson(registrationInProcessMessage)
                        val embedBuilder = EmbedBuilder.fromData(data)
                        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).await()
                    } else {
                        event.reply(registrationInProcessMessage).setEphemeral(true).await()
                    }
                    return@onCommand
                }

                if (discordUserManager.checkUserByDiscordId(member.id)) {
                    val registrationCompletedMessage = discordBot.messages.getString(DCMessage.REGISTRATION_COMPLETED)
                    if (Utils.isJsonValid(registrationCompletedMessage)) {
                        val data = DataObject.fromJson(registrationCompletedMessage)
                        val embedBuilder = EmbedBuilder.fromData(data)
                        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).await()
                    } else {
                        event.reply(registrationCompletedMessage).setEphemeral(true).await()
                    }
                    return@onCommand
                }

                val createTokenTask = CreateTokenTask(discordBot, event)
                discordBot.scheduler.runAsync(createTokenTask, false)
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }

        jda.onCommand(playerListCommandName) { event ->
            if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_PLAYERLIST_ENABLED)) return@onCommand

            if (event.options.isEmpty()) {
                val sb = StringBuilder()
                val players = discordBot.networkManager.cacheManager.cachedPlayers.players.values
                if (players.isEmpty()) {
                    sb.append("There are currently no players online!")
                } else {
                    for (player in players) {
                        if (!player.isOnline) continue
                        if (player.vanished) continue
                        sb.append(player.name).append(" - ").append(player.server).append("\n")
                    }
                }
                val playerList = sb.toString().trim()
                event.reply(playerList).await()
            } else {
                val serverName = event.getOption("servername")?.asString
                if (serverName == null || !discordBot.networkManager.getAllServerNames().contains(serverName)) {
                    event.reply(discordBot.messages.getString(DCMessage.COMMAND_PLAYERLIST_INVALID_SERVER)
                        .replace("%mention%", event.user.asMention)
                        .replace("%server%", serverName.toString())).await()
                    return@onCommand
                }

                var sb = StringBuilder()
                for (player in discordBot.networkManager.cacheManager.cachedPlayers.players.values) {
                    if (player.vanished) continue
                    sb.append(player.name).append(" - ").append(player.server).append("\n")
                }
                if (sb.isNotEmpty()) {
                    sb = StringBuilder(sb.toString().substring(0, sb.toString().length - 2))
                }
                val playerList = sb.toString().trim()
                event.reply(playerList).await()
            }
        }

        jda.onCommand(playersCommandName) { event ->
            if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_PLAYERS_ENABLED)) return@onCommand

            event.reply(
                discordBot.messages.getString(DCMessage.COMMAND_ONLINEPLAYERS_RESPONSE)
                    .replace("%mention%", event.user.asMention)
                    .replace("%players%", discordBot.networkManager.onlinePlayersCount.toString())
            ).await()
        }

        jda.onCommand(playtimeCommandName) { event ->
            if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_PLAYTIME_ENABLED)) return@onCommand

            val uuid = discordBot.discordUserManager.getUuidByDiscordId(event.user.id)
            if (uuid == null) {
                discordBot.platform.warn("${event.user.name} tried to use the playtime command but is not registered!")
                event.reply_("You need to link your account before you can use this command!").setEphemeral(true).await()
                return@onCommand
            }
            val language = discordBot.getDefaultLanguage()
            if (discordBot.networkManager.isPlayerOnline(uuid, true)) {
                val player = discordBot.networkManager.getPlayer(uuid) ?: return@onCommand

                val data = DataObject.fromJson(discordBot.messages.getString(DCMessage.COMMAND_PLAYTIME_RESPONSE))
                val embedBuilder = EmbedBuilder.fromData(data).apply {
                    val title = data.getString("title", null)?.replace("%playername%", player.name)
                    setTitle(title)
                    parsePlaceholdersToFields { text ->
                        val formattedPlaytime = TimeUtils.getTimeString(language, player.playtime / 1000)
                        text.replace("%playername%", player.name)
                            .replace("%playtime%", formattedPlaytime)
                            .replace("%liveplaytime%", formattedPlaytime)
                    }
                }

                event.hook.sendMessageEmbeds(embedBuilder.build()).await()
            } else {
                event.deferReply().queue()
                val result = if (playtimeCache.contains(uuid)) {
                    playtimeCache[uuid]!!
                } else {
                    val playtimeData = getOfflinePlayerPlaytime(uuid)
                    playtimeCache[uuid] = playtimeData
                    playtimeData
                }
                val userName = result.first
                val playtime = result.second
                if (userName.isEmpty() || playtime == 0L) {
                    event.reply_("Something went wrong trying to get your playtime data.").setEphemeral(true).await()
                    return@onCommand
                }

                val data = DataObject.fromJson(discordBot.messages.getString(DCMessage.COMMAND_PLAYTIME_RESPONSE))
                val embedBuilder = EmbedBuilder.fromData(data).apply {
                    val title = data.getString("title", null)?.replace("%playername%", userName)
                    setTitle(title)
                    parsePlaceholdersToFields { text ->
                        val formattedPlaytime = TimeUtils.getTimeString(language, playtime / 1000)
                        text.replace("%playername%", userName)
                            .replace("%playtime%", formattedPlaytime)
                            .replace("%liveplaytime%", formattedPlaytime)
                    }
                }

                event.hook.sendMessageEmbeds(embedBuilder.build()).await()
            }
        }

        jda.onCommand(uptimeCommandName) { event ->
            if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_UPTIME_ENABLED)) return@onCommand

            val language = discordBot.getDefaultLanguage()
            val uptime = ManagementFactory.getRuntimeMXBean().startTime
            event.reply(TimeUtils.getTimeString(language, (System.currentTimeMillis() - uptime) / 1000)).await()
        }
    }

    private suspend fun getOfflinePlayerPlaytime(uuid: UUID): Pair<String, Long> {
        return withContext(Dispatchers.IO) {
            var pair = Pair("", 0L)
            try {
                discordBot.mySQL.connection.use { connection ->
                    connection.prepareStatement("SELECT `username`, `playtime` FROM nm_players WHERE `uuid`=?;").use { ps ->
                        ps.setString(1, uuid.toString())
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                val username = rs.getString("username")
                                val playtime = rs.getLong("playtime")
                                pair = Pair(username, playtime)
                            }
                        }
                    }
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
            pair
        }
    }
}