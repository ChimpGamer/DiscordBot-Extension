package nl.chimpgamer.networkmanager.extensions.discordbot.manager

import com.jagrosh.jdautilities.command.CommandClientBuilder
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord.*
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.DiscordListener
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils.getRoleByName
import javax.security.auth.login.LoginException

class DiscordManager(private val discordBot: DiscordBot) {
    private val commandClientBuilder: CommandClientBuilder = CommandClientBuilder()
    private lateinit var jDA: JDA
    lateinit var guild: Guild
    var verifiedRole: Role? = null

    fun init(): Boolean {
        var success = true
        try {
            initCommandBuilder()
            initJDA()
        } catch (ex: LoginException) {
            ex.printStackTrace()
            success = false
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
            success = false
        }
        val guilds = jDA.guilds

        when {
            guilds.isNullOrEmpty() -> {
                discordBot.logger.warning("The Bot is not a member of a guild!")
            }
            guilds.size > 1 -> {
                discordBot.logger.warning("The Bot is a member of too many guilds!")
                success = false
            }
            else -> {
                guild = guilds[0]
                success = true
            }
        }
        val roleName = Setting.DISCORD_REGISTER_ADD_ROLE_ROLE_NAME.asString
        val role = getRoleByName(roleName)
        if (role != null) {
            discordBot.logger.info("Verified Role is: '" + role.name + "' (" + role.id + ")")
        } else {
            discordBot.logger.info("No Verified Role found by the name: '$roleName'")
        }
        verifiedRole = role
        return success
    }

    @Throws(LoginException::class, InterruptedException::class)
    private fun initJDA() {
        jDA = JDABuilder(AccountType.BOT)
                .setToken(Setting.DISCORD_TOKEN.asString)
                .addEventListeners(DiscordListener(discordBot))
                .addEventListeners(commandClientBuilder.build())
                .setAutoReconnect(true)
                .setMaxReconnectDelay(180)
                .build()
                .awaitReady()
    }

    private fun initCommandBuilder() {
        commandClientBuilder
                .setPrefix(Setting.DISCORD_COMMAND_PREFIX.asString)
                .setOwnerId(Setting.DISCORD_OWNER_ID.asString)
                .addCommands(
                        PlayerListCommand(discordBot),
                        PlayersCommand(discordBot),
                        RegisterCommand(discordBot),
                        PlaytimeCommand(discordBot),
                        UptimeCommand()
                )
        if (Setting.DISCORD_STATUS_ENABLED.asBoolean) {
            val activityType = try {
                Activity.ActivityType.valueOf(Setting.DISCORD_STATUS_TYPE.asString.toUpperCase())
            } catch (ex: IllegalArgumentException) {
                discordBot.logger.warning("StatusType '" + Setting.DISCORD_STATUS_TYPE.asString + "' is invalid. Using DEFAULT.")
                Activity.ActivityType.DEFAULT
            }
            commandClientBuilder
                    .setActivity(Activity.of(activityType, Setting.DISCORD_STATUS_MESSAGE.asString
                            .replace("%player%", discordBot.networkManager.proxy.players.size.toString())))
        } else {
            commandClientBuilder.setActivity(null)
        }
    }

    fun shutdownJDA() {
        discordBot.logger.info("Shutting down JDA...")
        jDA.shutdown()
    }

    fun restartJDA(): Boolean {
        shutdownJDA()
        try {
            initJDA()
            return true
        } catch (e: LoginException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }
}