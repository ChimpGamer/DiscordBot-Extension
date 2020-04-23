package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.exceptions.PermissionException
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.math.BigInteger
import java.security.SecureRandom
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
        val rolesToAddFiltered: MutableSet<Role> = HashSet()
        val rolesToRemoveFiltered: MutableSet<Role> = HashSet()
        rolesToAdd
                .filter { role: Role -> !role.isManaged }
                .filter { role: Role -> role.guild.publicRole.id != role.id }
                .filter { role: Role -> !member.roles.contains(role) }
                .toCollection(rolesToAddFiltered)
        val nonInteractableRolesToAdd = rolesToAddFiltered.stream().filter { role: Role -> !member.guild.selfMember.canInteract(role) }.collect(Collectors.toSet())
        rolesToAddFiltered.removeAll(nonInteractableRolesToAdd)
        nonInteractableRolesToAdd.forEach { role: Role -> DiscordBot.instance!!.logger.warning("Failed to add role " + role.name + " to " + member.effectiveName + " because the bot's highest role is lower than the target role and thus can't interact with it") }
        rolesToRemove
                .filter { role: Role -> !role.isManaged }
                .filter { role: Role -> role.guild.publicRole.id != role.id }
                .filter { role: Role -> member.roles.contains(role) }
                .toCollection(rolesToRemoveFiltered)
        val nonInteractableRolesToRemove = rolesToRemoveFiltered.stream().filter { role: Role -> !member.guild.selfMember.canInteract(role) }.collect(Collectors.toSet())
        rolesToRemoveFiltered.removeAll(nonInteractableRolesToRemove)
        nonInteractableRolesToRemove.forEach { role: Role -> DiscordBot.instance!!.logger.warning("Failed to remove role " + role.name + " from " + member.effectiveName + " because the bot's highest role is lower than the target role and thus can't interact with it") }
        member.guild.modifyMemberRoles(member, rolesToAddFiltered, rolesToRemoveFiltered).queue()
    }

    fun firstUpperCase(var0: String): String {
        return var0.substring(0, 1).toUpperCase() + var0.substring(1)
    }

    @JvmStatic
    fun generateToken(): String {
        return BigInteger(64, SecureRandom()).toString(32)
    }
}