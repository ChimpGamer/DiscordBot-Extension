package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.exceptions.PermissionException
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.events.PlayerUnregisteredEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import java.sql.SQLException

class DeleteUserTask(private val discordBot: DiscordBot, private val player: Player) : Runnable {

    override fun run() {
        val discordUserManager = discordBot.discordUserManager
        try {
            if (discordUserManager.existsInDatabase(player.uuid.toString())) {
                val discordId = discordUserManager.getDiscordIdByUuid(player.uuid)!!

                discordUserManager.deleteUserFromDatabase(player.uuid)
                discordUserManager.discordUsers.remove(player.uuid)

                val member = discordBot.guild.getMemberById(discordId)
                this.discordBot.eventHandler.callEvent(PlayerUnregisteredEvent(player, member))

                if (discordBot.settings.getBoolean(Setting.DISCORD_REGISTER_ADD_ROLE_ENABLED)) {
                    val verifiedRole = discordBot.discordManager.verifiedRole
                    if (verifiedRole != null && member != null) {
                        discordBot.guild.removeRoleFromMember(member, verifiedRole).queue()
                    }
                }

                if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
                    val removeRoles: MutableSet<Role> = HashSet()
                    for (roleName in discordBot.settings.getStringList(Setting.DISCORD_SYNC_RANKS_LIST)) {
                        val role = discordBot.discordManager.getRoleByName(roleName) ?: continue
                        removeRoles.add(role)
                    }

                    if (removeRoles.isEmpty()) return
                    try {
                        member?.let { discordBot.guild.modifyMemberRoles(it, null, removeRoles).queue() }
                    } catch (ex: PermissionException) {
                        if (ex.permission == Permission.UNKNOWN) {
                            discordBot.logger.warning("Could not set the role for " + member?.effectiveName + " because " + ex.message)
                        } else {
                            discordBot.logger.warning("Could not set the role for " + member?.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
                        }
                    }
                }

                val unregisterNotification = discordBot.messages.getString(DCMessage.REGISTRATION_UNREGISTER_NOTIFICATION)
                        .replace("%command_prefix%", discordBot.settings.getString(Setting.DISCORD_COMMAND_PREFIX))
                if (unregisterNotification.isNotEmpty()) {
                    val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(unregisterNotification)
                    member?.user?.openPrivateChannel()?.queue {
                        it.sendMessage(jsonEmbedBuilder.build()).queue()
                    }
                }

                if (discordBot.settings.getBoolean(Setting.DISCORD_UNREGISTER_KICK_ENABLED)) {
                    member?.kick(discordBot.settings.getString(Setting.DISCORD_UNREGISTER_KICK_REASON))
                }

                // TODO: Make these messages configurable
                if (player.isOnline) {
                    player.sendMessage("Successfully unregistered your discord account!")
                }
            } else {
                player.sendMessage("You cannot unlink your account because aren't registered!")
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }
}