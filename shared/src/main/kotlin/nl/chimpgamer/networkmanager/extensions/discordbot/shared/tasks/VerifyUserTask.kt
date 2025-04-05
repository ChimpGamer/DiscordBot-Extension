package nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.utils.data.DataObject
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.api.utils.TimeUtils
import nl.chimpgamer.networkmanager.api.values.Message
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.api.events.PlayerRegisteredEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.api.models.Token
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.RedisBungeeUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.parsePlaceholdersToFields
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.optionals.getOrNull

class VerifyUserTask(private val discordBot: DiscordBot, private val player: Player, private val token: Token) :
    Runnable {

    override fun run() {
        if (token.created + 300000 < System.currentTimeMillis()) { // Token Expired
            player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_TOKEN_EXPIRED))
        } else {
            try {
                checkNotNull(discordBot.guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
                val discordUserManager = this.discordBot.discordUserManager
                val member: Member? = this.discordBot.guild.getMemberById(this.token.discordID)
                if (member == null) {
                    player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_NOT_IN_SERVER))
                    return
                }
                if (discordUserManager.existsInDatabase(this.player.uuid.toString())) { // User is already registered...
                    player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_ACCOUNT_ALREADY_LINKED))
                } else {
                    // User is not registered yet...
                    discordUserManager.tokens.remove(this.token) // Remove token
                    discordUserManager.insertUser(this.player.uuid, this.token.discordID)
                    val registrationCompleted = discordBot.messages.getString(DCMessage.REGISTRATION_COMPLETED)
                    if (Utils.isJsonValid(registrationCompleted)) {
                        val data = DataObject.fromJson(registrationCompleted)
                        val embedBuilder = EmbedBuilder.fromData(data)
                        token.interaction.editOriginalEmbeds(embedBuilder.build()).queue()
                    } else {
                        token.interaction.editOriginal(registrationCompleted).queue()
                    }
                    player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_COMPLETED))
                    this.discordBot.platform.eventBus.post(PlayerRegisteredEvent(this.player, member))
                    if (this.discordBot.networkManager.isRedisBungee) {
                        RedisBungeeUtils.sendRedisBungeeMessage(discordBot, "load " + player.uuid)
                    }

                    val syncUsername = discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME_ENABLED)
                    val addVerifiedRole = discordBot.settings.getBoolean(Setting.DISCORD_REGISTER_ADD_ROLE_ENABLED) && discordBot.discordManager.verifiedRole != null

                    val removeRole = discordBot.settings.getBoolean(Setting.DISCORD_REGISTER_REMOVE_ROLE_ENABLED)
                    if (removeRole) {
                        val roleToRemove = discordBot.discordManager.getRole(discordBot.settings.getString(Setting.DISCORD_REGISTER_REMOVE_ROLE_ROLE_NAME))
                        if (roleToRemove != null) {
                            Utils.modifyRolesOfMember(member, mutableSetOf(), mutableSetOf(roleToRemove))
                        }
                    }

                    if (syncUsername && addVerifiedRole) {
                        val nickname = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
                        discordBot.discordManager.setNickNameAndAddRole(member, nickname, discordBot.discordManager.verifiedRole!!)
                    } else {
                        if (syncUsername) {
                            val nickname = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
                            discordBot.discordManager.setNickName(member, nickname)
                        }
                        if (addVerifiedRole) {
                            discordBot.discordManager.verifiedRole?.let { discordBot.discordManager.addRoleToMember(member, it) }
                        }
                    }

                    if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
                        discordBot.scheduler.runDelayed(SyncRanksTask(discordBot, player), 1)
                    }

                    val executeCommands = discordBot.settings.getStringList(Setting.DISCORD_REGISTER_EXECUTE_COMMANDS)
                    if (executeCommands.isNotEmpty()) {
                        executeCommands.forEach { command ->
                            discordBot.networkManager.executeConsoleCommand(command
                                .replace("%playeruuid%", player.uuid.toString())
                                .replace("%playername%", player.name))
                        }
                    }

                    val registrationAlertsChannel = discordBot.discordManager.getTextChannelById(Setting.DISCORD_EVENTS_REGISTRATION_ALERTS_CHANNEL)
                    if (registrationAlertsChannel != null) {
                        val language = discordBot.getDefaultLanguage()

                        // Hopefully all placeholder values are available when this runs.
                        discordBot.scheduler.runDelayed({
                            val data = DataObject.fromJson(discordBot.messages.getString(DCMessage.REGISTRATION_COMPLETED_ALERT))
                            val date = SimpleDateFormat(discordBot.networkManager.getMessage(language, Message.LOOKUP_DATETIME_FORMAT))
                            val firstLoginFormatted = date.format(Date(player.firstlogin))
                            val embedBuilder = EmbedBuilder.fromData(data).apply {
                                val title = data.getString("title", null)?.replace("%player_name%", player.name)
                                    ?.replace("%discord_member_name%", member.user.name)
                                data.optObject("thumbnail").map { it.getString("url") }.getOrNull()
                                val thumbnail = data.optObject("thumbnail").map { it.getString("url") }.getOrNull()?.replace("%player_uuid%", player.uuid.toString())
                                    ?.replace("%discord_member_effective_avatar_url%", member.effectiveAvatarUrl)
                                setTitle(title)
                                setThumbnail(thumbnail)
                                parsePlaceholdersToFields { text ->
                                    Placeholders.setPlaceholders(
                                        player, text
                                            .replace("%discord_member_name%", member.user.name)
                                            .replace("%discord_member_mention%", member.asMention)
                                            .replace("%discord_member_roles%", member.roles.joinToString { it.name })
                                            .replace("%discord_member_highest_role%", member.roles.maxByOrNull { it.position }?.name ?: "No Roles")
                                            .replace("%player_name%", player.name)
                                            .replace("%player_uuid%", player.uuid.toString())
                                            .replace("%player_first_login%", firstLoginFormatted)
                                            .replace("%player_playtime%", TimeUtils.getTimeString(language, player.livePlaytime / 1000))
                                            .replace("%player_server%", player.server ?: "null")
                                    )
                                }
                            }

                            Utils.sendChannelMessage(registrationAlertsChannel, embedBuilder.build())
                        }, 5)
                    }
                }
            } catch (ex: SQLException) {
                player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_ERROR))
                ex.printStackTrace()
            }
        }
    }
}