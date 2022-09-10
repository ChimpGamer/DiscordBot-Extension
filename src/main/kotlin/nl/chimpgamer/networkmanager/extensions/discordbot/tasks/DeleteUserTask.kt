package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.exceptions.PermissionException
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.events.PlayerUnregisteredEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.JsonMessageEmbed
import java.sql.SQLException

class DeleteUserTask(private val discordBot: DiscordBot, private val player: Player) : Runnable {

    override fun run() {
        val discordUserManager = discordBot.discordUserManager
        try {
            val discordId = discordUserManager.getDiscordIdByUuid(player.uuid)
            // If discordId is not null the user exists in the database.
            if (discordId != null) {

                discordUserManager.deleteUserFromDatabase(player.uuid)
                discordUserManager.discordUsers.remove(player.uuid)

                val member = discordBot.guild.getMemberById(discordId)
                this.discordBot.eventBus.post(PlayerUnregisteredEvent(player, member))

                if (discordBot.settings.getBoolean(Setting.DISCORD_REGISTER_ADD_ROLE_ENABLED)) {
                    val verifiedRole = discordBot.discordManager.verifiedRole
                    if (verifiedRole != null && member != null) {
                        discordBot.guild.removeRoleFromMember(member, verifiedRole).queue()
                    }
                }

                if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
                    val removeRoles: MutableSet<Role> = HashSet()
                    for ((_, roleName) in discordBot.settings.getMap(Setting.DISCORD_SYNC_RANKS_MAP)) {
                        val role = discordBot.discordManager.getRole(roleName) ?: continue
                        removeRoles.add(role)
                    }

                    if (removeRoles.isEmpty()) return
                    try {
                        member?.let { discordBot.guild.modifyMemberRoles(it, null, removeRoles).queue() }
                    } catch (ex: PermissionException) {
                        if (ex.permission == Permission.UNKNOWN) {
                            discordBot.logger.warning("Could not set the role for ${member?.effectiveName} because ${ex.message}")
                        } else {
                            discordBot.logger.warning("Could not set the role for ${member?.effectiveName} because the bot does not have the required permission ${ex.permission.name}")
                        }
                    }
                }

                val unregisterNotification = discordBot.messages.getString(DCMessage.REGISTRATION_UNREGISTER_NOTIFICATION)
                if (unregisterNotification.isNotEmpty()) {
                    val jsonMessageEmbed = JsonMessageEmbed.fromJson(unregisterNotification)
                    member?.user?.openPrivateChannel()?.queue {
                        it.sendMessageEmbeds(jsonMessageEmbed.toMessageEmbed()).queue()
                    }
                }

                if (discordBot.settings.getBoolean(Setting.DISCORD_UNREGISTER_KICK_ENABLED)) {
                    member?.kick(discordBot.settings.getString(Setting.DISCORD_UNREGISTER_KICK_REASON))
                }

                val executeCommands = discordBot.settings.getStringList(Setting.DISCORD_UNREGISTER_EXECUTE_COMMANDS)
                if (executeCommands.isNotEmpty()) {
                    executeCommands.forEach { command ->
                        discordBot.networkManager.executeConsoleCommand(command.replace("%playername%", player.name))
                    }
                }

                if (player.isOnline) {
                    player.sendMessage(discordBot.messages.getString(MCMessage.UNREGISTER_SUCCESS))
                }
            } else {
                player.sendMessage(discordBot.messages.getString(MCMessage.UNREGISTER_NOT_REGISTERED))
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }
}
