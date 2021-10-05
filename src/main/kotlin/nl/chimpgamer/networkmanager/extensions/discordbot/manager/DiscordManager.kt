package nl.chimpgamer.networkmanager.extensions.discordbot.manager

import com.jagrosh.jdautilities.command.CommandClientBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord.*
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.DiscordListener
import javax.security.auth.login.LoginException

class DiscordManager(private val discordBot: DiscordBot) {
    private val commandClientBuilder = CommandClientBuilder()
    private lateinit var jda: JDA
    lateinit var guild: Guild
    var verifiedRole: Role? = null

    fun init(): Boolean {
        var success = false
        try {
            initCommandBuilder()
            initJDA()
        } catch (ex: LoginException) {
            ex.printStackTrace()
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
        val guilds = jda.guilds

        when {
            guilds.isEmpty() -> {
                discordBot.logger.warning("The Bot is not a member of a guild!")
            }
            guilds.size > 1 -> {
                discordBot.logger.warning("The Bot is a member of too many guilds!")
            }
            else -> {
                guild = guilds[0]
                success = true
            }
        }
        if (success) {
            val roleName = discordBot.settings.getString(Setting.DISCORD_REGISTER_ADD_ROLE_ROLE_NAME)
            val role = getRoleByName(roleName)
            if (role != null) {
                discordBot.logger.info("Verified Role is: '${role.name}' (${role.id})")
            } else {
                discordBot.logger.info("No Verified Role found by the name: '$roleName'")
            }
            verifiedRole = role
        }
        return success
    }

    @Throws(LoginException::class, InterruptedException::class)
    private fun initJDA() {
        jda = JDABuilder.createDefault(discordBot.settings.getString(Setting.DISCORD_TOKEN))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
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
                .useHelpBuilder(false)
                .addCommands(
                        PlayerListCommand(discordBot),
                        PlayersCommand(discordBot),
                        RegisterCommand(discordBot),
                        PlaytimeCommand(discordBot),
                        UptimeCommand(discordBot)
                )
        if (discordBot.settings.getBoolean(Setting.DISCORD_STATUS_ENABLED)) {
            val activityType = try {
                Activity.ActivityType.valueOf(discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE).uppercase())
            } catch (ex: IllegalArgumentException) {
                discordBot.logger.warning("StatusType '${discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE)}' is invalid. Using DEFAULT.")
                Activity.ActivityType.DEFAULT
            }
            commandClientBuilder
                    .setActivity(Activity.of(activityType, discordBot.settings.getString(Setting.DISCORD_STATUS_MESSAGE)
                            .replace("%players%", discordBot.networkManager.onlinePlayersCount.toString())))
        } else {
            commandClientBuilder.setActivity(null)
        }
    }

    fun shutdownJDA() {
        discordBot.logger.info("Shutting down JDA...")
        if (this::jda.isInitialized) {
            jda.shutdownNow()
        }
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

    private fun setActivity(activity: Activity) {
        jda.presence.activity = activity
    }

    fun updateActivity(players: Int) {
        if (discordBot.settings.getBoolean(Setting.DISCORD_STATUS_ENABLED)) {
            val activityType = try {
                Activity.ActivityType.valueOf(discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE).uppercase())
            } catch (ex: IllegalArgumentException) {
                discordBot.logger.warning("StatusType '${discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE)}' is invalid. Using DEFAULT.")
                Activity.ActivityType.DEFAULT
            }
            setActivity(Activity.of(activityType, Placeholders.Companion.setPlaceholders(null, discordBot.settings.getString(Setting.DISCORD_STATUS_MESSAGE)
                    .replace("%players%", players.toString()))))
        }
    }

    fun setNickName(member: Member?, nickName: String) {
        if (member == null) {
            discordBot.logger.info("Can't set the nickname of a null member")
            return
        }
        var finalNickname = nickName
        if (nickName.length > 32) {
            discordBot.logger.info("The new nickname of ${member.effectiveName} exceeds the maximum limit of 32 characters.")
            finalNickname = finalNickname.substring(0, 32)
        }
        discordBot.logger.info("Setting nickname for " + member.effectiveName)
        try {
            member.guild.modifyNickname(member, finalNickname).queue()
        } catch (ex: PermissionException) {
            if (ex.permission === Permission.UNKNOWN) {
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
            if (ex.permission === Permission.UNKNOWN) {
                discordBot.logger.warning("Could not set the role for " + member.effectiveName + " because " + ex.message)
            } else {
                discordBot.logger.warning("Could not set the role for " + member.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
            }
        }
    }

    fun getRoleByName(roleName: String): Role? {
        val roles = guild.getRolesByName(roleName, true)
        return roles.firstOrNull { it.name.equals(roleName, ignoreCase = true) }
    }
}