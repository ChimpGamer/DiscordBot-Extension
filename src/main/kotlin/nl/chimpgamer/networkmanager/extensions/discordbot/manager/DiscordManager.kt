package nl.chimpgamer.networkmanager.extensions.discordbot.manager

import com.jagrosh.jdautilities.command.CommandClientBuilder
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.exceptions.PermissionException
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord.*
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.DiscordListener
import javax.security.auth.login.LoginException

class DiscordManager(private val discordBot: DiscordBot) {
    private val commandClientBuilder: CommandClientBuilder = CommandClientBuilder()
    private lateinit var jda: JDA
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
        val guilds = jda.guilds

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
        val roleName = discordBot.settings.getString(Setting.DISCORD_REGISTER_ADD_ROLE_ROLE_NAME)
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
        jda = JDABuilder(AccountType.BOT)
                .setToken(discordBot.settings.getString(Setting.DISCORD_TOKEN))
                .addEventListeners(DiscordListener(discordBot))
                .addEventListeners(commandClientBuilder.build())
                .setAutoReconnect(true)
                .setMaxReconnectDelay(180)
                .build()
                .awaitReady()
    }

    private fun initCommandBuilder() {
        commandClientBuilder
                .setPrefix(discordBot.settings.getString(Setting.DISCORD_COMMAND_PREFIX))
                .setOwnerId(discordBot.settings.getString(Setting.DISCORD_OWNER_ID))
                .addCommands(
                        PlayerListCommand(discordBot),
                        PlayersCommand(discordBot),
                        RegisterCommand(discordBot),
                        PlaytimeCommand(discordBot),
                        UptimeCommand(discordBot)
                )
        if (discordBot.settings.getBoolean(Setting.DISCORD_STATUS_ENABLED)) {
            val activityType = try {
                Activity.ActivityType.valueOf(discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE).toUpperCase())
            } catch (ex: IllegalArgumentException) {
                discordBot.logger.warning("StatusType '${discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE)}' is invalid. Using DEFAULT.")
                Activity.ActivityType.DEFAULT
            }
            commandClientBuilder
                    .setActivity(Activity.of(activityType, discordBot.settings.getString(Setting.DISCORD_STATUS_MESSAGE)
                            .replace("%players%", discordBot.networkManager.proxy.players.size.toString())))
        } else {
            commandClientBuilder.setActivity(null)
        }
    }

    fun shutdownJDA() {
        discordBot.logger.info("Shutting down JDA...")
        jda.shutdown()
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

    fun setActivity(activity: Activity) {
        jda.presence.activity = activity
    }

    fun updateActivity() {
        if (discordBot.settings.getBoolean(Setting.DISCORD_STATUS_ENABLED)) {
            val activityType = try {
                Activity.ActivityType.valueOf(discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE).toUpperCase())
            } catch (ex: IllegalArgumentException) {
                discordBot.logger.warning("StatusType '${discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE)}' is invalid. Using DEFAULT.")
                Activity.ActivityType.DEFAULT
            }
            setActivity(Activity.of(activityType, discordBot.settings.getString(Setting.DISCORD_STATUS_MESSAGE)
                    .replace("%players%", discordBot.networkManager.proxy.players.size.toString())))
        }
    }

    fun setNickName(member: Member?, nickName: String) {
        if (member == null) {
            discordBot.logger.info("Can't set the nickname of a null member")
            return
        }
        discordBot.logger.info("Setting nickname for " + member.effectiveName)
        try {
            member.guild.modifyNickname(member, nickName).queue()
        } catch (ex: PermissionException) {
            if (ex.permission == Permission.UNKNOWN) {
                discordBot.logger.warning("Could not set the nickname for " + member.effectiveName + " because " + ex.message)
            } else {
                discordBot.logger.warning("Could not set the nickname for " + member.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
            }
        }
    }

    fun addRoleToMember(member: Member, role: Role) {
        try {
            member.guild.addRoleToMember(member, role).queue()
        } catch (ex: PermissionException) {
            if (ex.permission == Permission.UNKNOWN) {
                discordBot.logger.warning("Could not set the role for " + member.effectiveName + " because " + ex.message)
            } else {
                discordBot.logger.warning("Could not set the role for " + member.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
            }
        }
    }

    fun getRoleByName(roleName: String): Role? {
        val roles = guild.getRolesByName(roleName, true)
        return roles.first { it.name.equals(roleName, ignoreCase = true) }
    }
}