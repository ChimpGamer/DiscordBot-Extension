package nl.chimpgamer.networkmanager.extensions.discordbot.shared.manager

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners.DiscordCommandsListener
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners.DiscordListener
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.listeners.modals.TicketModalListener
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils
import java.util.logging.Level
import javax.security.auth.login.LoginException

class DiscordManager(private val discordBot: DiscordBot) {
    lateinit var jda: JDA
    lateinit var guild: Guild
    var verifiedRole: Role? = null

    fun init(): Boolean {
        var success = false
        try {
            initJDA()
            initializeActivity()
        } catch (ex: LoginException) {
            ex.printStackTrace()
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
        val guilds = jda.guilds

        when {
            guilds.isEmpty() -> {
                discordBot.platform.warn("The Bot is not a member of a guild!")
            }
            guilds.size > discordBot.settings.getInt(Setting.DISCORD_MAX_GUILDS) -> {
                discordBot.platform.warn("The Bot is a member of too many guilds!")
            }

            else -> {
                guild = jda.getGuildById(discordBot.settings.getString(Setting.DISCORD_MAIN_GUILD)) ?: guilds[0]
                success = true
            }
        }
        if (success) {
            val roleName = discordBot.settings.getString(Setting.DISCORD_REGISTER_ADD_ROLE_ROLE_NAME)
            val role = getRole(roleName)
            if (role != null) {
                discordBot.platform.info("Verified Role is: '${role.name}' (${role.id})")
            } else {
                discordBot.platform.info("No Verified Role found by the name: '$roleName'")
            }
            verifiedRole = role
        }
        return success
    }

    @Throws(LoginException::class, InterruptedException::class)
    private fun initJDA() {
        jda = light(
            discordBot.settings.getString(Setting.DISCORD_TOKEN),
            intents = listOf(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
            )
        ) {
            setMemberCachePolicy(MemberCachePolicy.ALL)
            addEventListeners(
                DiscordListener(discordBot),
                DiscordCommandsListener(discordBot),
                TicketModalListener(discordBot)
            )
            setAutoReconnect(true)
            setMaxReconnectDelay(180)
        }.awaitReady()

        jda.listener<ReadyEvent> { discordBot.platform.info("Bot is ready!")}
    }

    fun setOnlineStatus() = jda.presence.setStatus(OnlineStatus.fromKey(discordBot.settings.botDiscordOnlineStatus))

    private fun initializeActivity() {
        setOnlineStatus()
        if (discordBot.settings.getBoolean(Setting.DISCORD_STATUS_ENABLED)) {
            val activityType = try {
                Activity.ActivityType.valueOf(discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE).uppercase())
            } catch (ex: IllegalArgumentException) {
                discordBot.platform.warn("StatusType '${discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE)}' is invalid. Using PLAYING.")
                Activity.ActivityType.PLAYING
            }
            val statusMessage = Placeholders.setPlaceholders(
                null, discordBot.settings.getString(Setting.DISCORD_STATUS_MESSAGE)
                    .replace("%players%", discordBot.networkManager.onlinePlayersCount.toString())
            )

            setActivity(Activity.of(activityType, statusMessage))
        }
    }

    fun shutdownJDA() {
        discordBot.platform.info("Shutting down JDA...")
        if (this::jda.isInitialized) {
            jda.shutdownNow()
        }
    }

    fun restartJDA(): Boolean {
        shutdownJDA()
        try {
            initJDA()
            initializeActivity()
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
                discordBot.platform.warn("StatusType '${discordBot.settings.getString(Setting.DISCORD_STATUS_TYPE)}' is invalid. Using PLAYING.")
                Activity.ActivityType.PLAYING
            }
            val statusMessage = Placeholders.setPlaceholders(
                null, discordBot.settings.getString(Setting.DISCORD_STATUS_MESSAGE)
                    .replace("%players%", players.toString())
            )

            setActivity(Activity.of(activityType, statusMessage))
        }
    }

    fun setNickName(member: Member?, nickName: String) {
        if (member == null) {
            discordBot.platform.info("Can't set the nickname of a null member")
            return
        }
        var finalNickname = nickName
        if (nickName.length > 32) {
            discordBot.platform.info("The new nickname of ${member.effectiveName} exceeds the maximum limit of 32 characters.")
            finalNickname = finalNickname.substring(0, 32)
        }
        discordBot.platform.info("Setting nickname for " + member.effectiveName)
        try {
            member.guild.modifyNickname(member, finalNickname)
                .queue({ discordBot.platform.info("Successfully changed nickname of ${member.user.name} to $nickName") })
            { discordBot.platform.logger.log(Level.WARNING, "Failed to change nickname of ${member.effectiveName} to $nickName", it) }
        } catch (ex: PermissionException) {
            if (ex.permission === Permission.UNKNOWN) {
                discordBot.platform.warn("Could not set the nickname for " + member.effectiveName + " because " + ex.message)
            } else {
                discordBot.platform.warn("Could not set the nickname for " + member.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
            }
        }
    }

    fun addRoleToMember(member: Member, role: Role) {
        try {
            member.guild.addRoleToMember(member, role)
                .queue({ return@queue discordBot.platform.info("Successfully assigned ${role.name} role to ${member.effectiveName}") })
                { discordBot.platform.logger.log(Level.WARNING, "Failed to assign ${role.name} role to ${member.effectiveName}", it) }
        } catch (ex: PermissionException) {
            if (ex.permission === Permission.UNKNOWN) {
                discordBot.platform.warn("Could not set the role for " + member.effectiveName + " because " + ex.message)
            } else {
                discordBot.platform.warn("Could not set the role for " + member.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
            }
        }
    }

    fun setNickNameAndAddRole(member: Member, nickName: String, role: Role) {
        var finalNickname = nickName
        if (nickName.length > 32) {
            discordBot.platform.warn("The new nickname of ${member.user.name} exceeds the maximum limit of 32 characters.")
            finalNickname = finalNickname.substring(0, 32)
        }
        discordBot.platform.info("Setting nickname and role for " + member.user.name)

        try {
            member.guild.modifyNickname(member, finalNickname).and(member.guild.addRoleToMember(member, role)).queue({ })
            { discordBot.platform.logger.log(Level.WARNING, "Something went wrong trying to set nickname and role", it)}
        } catch (ex: PermissionException) {
            if (ex.permission === Permission.UNKNOWN) {
                discordBot.platform.warn("Could not set the role for " + member.effectiveName + " because " + ex.message)
            } else {
                discordBot.platform.warn("Could not set the role for " + member.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
            }
        }
    }

    fun getRole(query: String): Role? {
        return if (Utils.DISCORD_ID_REGEX.matches(query)) {
            guild.getRoleById(query)
        } else {
            val roles = guild.getRolesByName(query, true)
            roles.firstOrNull { it.name.equals(query, ignoreCase = true) }
        }
    }

    fun getTextChannelById(id: String): TextChannel? = jda.getTextChannelById(id)

    fun getTextChannelById(setting: Setting): TextChannel? {
        val channelId = discordBot.settings.getString(setting)
        if (channelId.isEmpty() || channelId == "000000000000000000") return null
        return requireNotNull(getTextChannelById(channelId)) { "Could not find a channel with id $channelId" }
    }
}