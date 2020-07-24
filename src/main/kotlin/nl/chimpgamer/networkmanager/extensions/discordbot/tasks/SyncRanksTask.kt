package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.exceptions.PermissionException
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils
import java.util.HashSet

class SyncRanksTask(private val discordBot: DiscordBot, private val player: Player): Runnable {

    override fun run() {
        val discordId = discordBot.discordUserManager.getDiscordIdByUuid(player.uuid) ?: return
        val member = discordBot.guild.getMemberById(discordId) ?: return
        discordBot.logger.info("Syncing roles for " + member.effectiveName)
        val addRoles: MutableSet<Role> = HashSet()
        val removeRoles: MutableSet<Role> = HashSet()
        for (roleName in discordBot.settings.getStringList(Setting.DISCORD_SYNC_RANKS_LIST)) {
            val role = discordBot.discordManager.getRoleByName(roleName) ?: continue
            val groups = nl.chimpgamer.networkmanager.bungeecord.utils.Utils.getGroupsName(player)
            if (groups.any { it.equals(role.name, ignoreCase = true) }) {
                addRoles.add(role)
            } else {
                removeRoles.add(role)
            }
        }
        // remove roles that the user already has from roles to add
        addRoles.removeAll(member.roles)
        // remove roles that the user doesn't already have from roles to remove
        removeRoles.removeIf { role: Role? -> !member.roles.contains(role) }
        /*println("AddRoles: $addRoles")
        println("RemoveRoles: $removeRoles")*/
        if (addRoles.isEmpty() && removeRoles.isEmpty()) {
            return
        }
        try {
            Utils.modifyRolesOfMember(member, addRoles, removeRoles)
        } catch (ex: PermissionException) {
            if (ex.permission == Permission.UNKNOWN) {
                discordBot.logger.warning("Could not set the role for " + member.effectiveName + " because " + ex.message)
            } else {
                discordBot.logger.warning("Could not set the role for " + member.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
            }
        }
    }
}