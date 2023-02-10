package nl.chimpgamer.networkmanager.extensions.discordbot.listeners

import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.CommandSetting
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.CreateTokenTask
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils
import java.lang.management.ManagementFactory
import java.sql.SQLException
import java.util.UUID

class DiscordCommandsListener(private val discordBot: DiscordBot) : CoroutineEventListener {

    override suspend fun onEvent(event: GenericEvent) {
        if (event !is GuildReadyEvent && event !is GuildJoinEvent) return
        val jda = event.jda

        println("Updating Discord Slash Commands")
        handleCommands(jda)
    }

    private fun handleCommands(jda: JDA) {
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
        }.queue()

        jda.onCommand(registerCommandName) { event ->
            val discordUserManager = discordBot.discordUserManager
            try {
                checkNotNull(discordBot.guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
                val member = event.member ?: discordBot.guild.getMember(event.user)
                if (member == null) {
                    event.reply(discordBot.messages.getString(DCMessage.REGISTRATION_NOT_IN_SERVER)).setEphemeral(true).queue()
                    return@onCommand
                }
                if (member.isPending) {
                    event.reply(discordBot.messages.getString(DCMessage.REGISTRATION_MEMBERSHIP_SCREENING_REQUIREMENTS_NOT_MET)).setEphemeral(true).queue()
                    return@onCommand
                }

                if (discordUserManager.containsDiscordID(member.id)) {
                    val registrationInProcessMessage = discordBot.messages.getString(DCMessage.REGISTRATION_IN_PROCESS)
                    if (Utils.isJsonValid(registrationInProcessMessage)) {
                        val embed = JsonMessageEmbed.fromJson(registrationInProcessMessage).toMessageEmbed()
                        event.replyEmbeds(embed).setEphemeral(true).queue()
                    } else {
                        event.reply(registrationInProcessMessage).setEphemeral(true).queue()
                    }
                    return@onCommand
                }

                if (discordUserManager.checkUserByDiscordId(member.id)) {
                    val registrationCompletedMessage = discordBot.messages.getString(DCMessage.REGISTRATION_COMPLETED)
                    if (Utils.isJsonValid(registrationCompletedMessage)) {
                        val embed = JsonMessageEmbed.fromJson(registrationCompletedMessage).toMessageEmbed()
                        event.replyEmbeds(embed).setEphemeral(true).queue()
                    } else {
                        event.reply(registrationCompletedMessage).setEphemeral(true).queue()
                    }
                    //event.reply("You have already registered your account to a minecraft account.").setEphemeral(true).queue()
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
                event.reply(playerList).queue()
            } else {
                val serverName = event.getOption("servername")?.asString
                if (serverName == null || !discordBot.networkManager.getAllServerNames().contains(serverName)) {
                    event.reply(discordBot.messages.getString(DCMessage.COMMAND_PLAYERLIST_INVALID_SERVER)
                        .replace("%mention%", event.user.asMention)
                        .replace("%server%", serverName.toString())).queue()
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
                event.reply(playerList).queue()
            }
        }

        jda.onCommand(playersCommandName) { event ->
            if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_PLAYERS_ENABLED)) return@onCommand

            event.reply(
                discordBot.messages.getString(DCMessage.COMMAND_ONLINEPLAYERS_RESPONSE)
                    .replace("%mention%", event.user.asMention)
                    .replace("%players%", discordBot.networkManager.onlinePlayersCount.toString())
            ).queue()
        }

        jda.onCommand(playtimeCommandName) { event ->
            if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_PLAYTIME_ENABLED)) return@onCommand

            val uuid = discordBot.discordUserManager.getUuidByDiscordId(event.user.id)
            if (uuid == null) {
                discordBot.logger.warning("${event.user.name} tried to use the playtime command but is not registed!")
                return@onCommand
            }
            if (discordBot.networkManager.isPlayerOnline(uuid, true)) {
                val player = discordBot.networkManager.getPlayer(uuid) ?: return@onCommand
                var jsonMessageEmbed = JsonMessageEmbed.fromJson(discordBot.messages.getString(DCMessage.COMMAND_PLAYTIME_RESPONSE))
                jsonMessageEmbed = jsonMessageEmbed.toBuilder()
                    .title(jsonMessageEmbed.title?.replace("%playername%", player.name))
                    .parsePlaceholdersToFields(mapOf(
                        "%playername%" to player.name,
                        "%playtime%" to TimeUtils.getTimeString(player.language, player.playtime / 1000),
                        "%liveplaytime%" to TimeUtils.getTimeString(player.language, player.livePlaytime / 1000)
                    )).build()

                event.replyEmbeds(jsonMessageEmbed.toMessageEmbed()).queue()
            } else {
                discordBot.scheduler.runAsync({
                    val result = getOfflinePlayerPlaytime(uuid)
                    val userName = result.first
                    val playtime = result.second
                    if (userName.isEmpty() || playtime == 0L) {
                        return@runAsync
                    }
                    var jsonMessageEmbed = JsonMessageEmbed.fromJson(discordBot.messages.getString(DCMessage.COMMAND_PLAYTIME_RESPONSE))
                    jsonMessageEmbed = jsonMessageEmbed.toBuilder()
                        .title(jsonMessageEmbed.title?.replace("%playername%", userName))
                        .parsePlaceholdersToFields(mapOf(
                            "%playername%" to userName,
                            "%playtime%" to TimeUtils.getTimeString(1, playtime / 1000),
                            "%liveplaytime%" to TimeUtils.getTimeString(1, playtime / 1000)
                        )).build()

                    event.replyEmbeds(jsonMessageEmbed.toMessageEmbed()).queue()
                }, false)
            }
        }

        jda.onCommand(uptimeCommandName) { event ->
            if (!discordBot.commandSettings.getBoolean(CommandSetting.DISCORD_UPTIME_ENABLED)) return@onCommand

            val uptime = ManagementFactory.getRuntimeMXBean().startTime
            event.reply(TimeUtils.getTimeString(1, (System.currentTimeMillis() - uptime) / 1000)).queue()
        }
    }

    private fun getOfflinePlayerPlaytime(uuid: UUID): Pair<String, Long> {
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
        return pair
    }
}