package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.exceptions.PermissionException
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.math.BigInteger
import java.security.SecureRandom
import java.util.function.Consumer
import java.util.stream.Collectors

object Utils {
    val UUID_REGEX: Regex = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")

    @JvmStatic
    fun sendChannelMessage(channel: MessageChannel, message: String?) {
        try {
            channel.sendMessage(message!!).queue()
        } catch (ex: PermissionException) {
            DiscordBot.instance!!.logger.warning("Could not send message to the " + channel.name + " because " + ex.message)
        }
    }

    @JvmStatic
    fun sendChannelMessage(channel: MessageChannel, message: MessageEmbed?) {
        try {
            channel.sendMessage(message!!).queue()
        } catch (ex: PermissionException) {
            DiscordBot.instance!!.logger.warning("Could not send message to the " + channel.name + " because " + ex.message)
        }
    }

    @JvmStatic
    fun sendMessageComplete(channel: MessageChannel, message: String?): Message? {
        try {
            return channel.sendMessage(message!!).complete()
        } catch (ex: PermissionException) {
            DiscordBot.instance!!.logger.warning("Could not send message to the " + channel.name + " because " + ex.message)
        }
        return null
    }

    @JvmStatic
    fun sendMessageComplete(channel: MessageChannel, message: MessageEmbed?): Message? {
        try {
            return channel.sendMessage(message!!).complete()
        } catch (ex: PermissionException) {
            DiscordBot.instance!!.logger.warning("Could not send message to the " + channel.name + " because " + ex.message)
        }
        return null
    }

    @JvmStatic
    fun editMessage(currentMessage: Message, newMessage: String?) {
        currentMessage.editMessage(newMessage!!).queue()
    }

    @JvmStatic
    fun editMessage(currentMessage: Message, newMessage: MessageEmbed?) {
        currentMessage.editMessage(newMessage!!).queue()
    }

    @Throws(InsufficientPermissionException::class)
    fun modifyRolesOfMember(member: Member, rolesToAdd: MutableSet<Role>, rolesToRemove: MutableSet<Role>) {
        val rolesToAddFiltered: MutableSet<Role>
        val rolesToRemoveFiltered: MutableSet<Role>
        rolesToAddFiltered = rolesToAdd.stream()
                .filter { role: Role -> !role.isManaged }
                .filter { role: Role -> role.guild.publicRole.id != role.id }
                .filter { role: Role -> !member.roles.contains(role) }
                .collect(Collectors.toSet())
        val nonInteractableRolesToAdd = rolesToAddFiltered.stream().filter { role: Role -> !member.guild.selfMember.canInteract(role) }.collect(Collectors.toSet())
        rolesToAddFiltered.removeAll(nonInteractableRolesToAdd)
        nonInteractableRolesToAdd.forEach(Consumer { role: Role -> DiscordBot.instance!!.logger.warning("Failed to add role " + role.name + " to " + member.effectiveName + " because the bot's highest role is lower than the target role and thus can't interact with it") })
        rolesToRemoveFiltered = rolesToRemove.stream()
                .filter { role: Role -> !role.isManaged }
                .filter { role: Role -> role.guild.publicRole.id != role.id }
                .filter { role: Role -> member.roles.contains(role) }
                .collect(Collectors.toSet())
        val nonInteractableRolesToRemove = rolesToRemoveFiltered.stream().filter { role: Role -> !member.guild.selfMember.canInteract(role) }.collect(Collectors.toSet())
        rolesToRemoveFiltered.removeAll(nonInteractableRolesToRemove)
        nonInteractableRolesToRemove.forEach(Consumer { role: Role -> DiscordBot.instance!!.logger.warning("Failed to remove role " + role.name + " from " + member.effectiveName + " because the bot's highest role is lower than the target role and thus can't interact with it") })
        member.guild.modifyMemberRoles(member, rolesToAddFiltered, rolesToRemoveFiltered).queue()
    }

    fun firstUpperCase(var0: String): String {
        return var0.substring(0, 1).toUpperCase() + var0.substring(1)
    }

    @JvmStatic
    fun generateToken(): String {
        return BigInteger(64, SecureRandom()).toString(32)
    }

    @JvmStatic
    fun setNickName(member: Member?, nickName: String) {
        if (member == null) {
            DiscordBot.instance!!.logger.info("Can't set the nickname of a null member")
            return
        }
        DiscordBot.instance!!.logger.info("Setting nickname for " + member.effectiveName)
        try {
            member.guild.modifyNickname(member, nickName).queue()
        } catch (ex: PermissionException) {
            if (ex.permission == Permission.UNKNOWN) {
                DiscordBot.instance!!.logger.warning("Could not set the nickname for " + member.effectiveName + " because " + ex.message)
            } else {
                DiscordBot.instance!!.logger.warning("Could not set the nickname for " + member.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
            }
        }
    }

    @JvmStatic
    fun addRoleToMember(member: Member, role: Role) {
        try {
            member.guild.addRoleToMember(member, role).queue()
        } catch (ex: PermissionException) {
            if (ex.permission == Permission.UNKNOWN) {
                DiscordBot.instance!!.logger.warning("Could not set the role for " + member.effectiveName + " because " + ex.message)
            } else {
                DiscordBot.instance!!.logger.warning("Could not set the role for " + member.effectiveName + " because the bot does not have the required permission " + ex.permission.getName())
            }
        }
    }

    @JvmStatic
    fun getRoleByName(roleName: String): Role? {
        val roles = DiscordBot.instance!!.guild.getRolesByName(roleName, true)
        if (roles.isEmpty()) {
            return null
        }
        for (role in roles) {
            if (role.name.equals(roleName, ignoreCase = true)) {
                return role
            }
        }
        return null
    }
}