package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.MalformedJsonException
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.exceptions.PermissionException
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.math.BigInteger
import java.security.SecureRandom

object Utils {
    val UUID_REGEX = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")

    fun ceilDiv(x: Long, y: Long): Long {
        return -Math.floorDiv(-x, y)
    }

    fun sendChannelMessage(channel: MessageChannel, message: String) {
        try {
            channel.sendMessage(message).queue()
        } catch (ex: PermissionException) {
            DiscordBot.instance.logger.warning("Could not send message to the " + channel.name + " because " + ex.message)
        }
    }

    @JvmStatic
    fun sendChannelMessage(channel: MessageChannel, message: MessageEmbed) {
        try {
            channel.sendMessage(message).queue()
        } catch (ex: PermissionException) {
            DiscordBot.instance.logger.warning("Could not send message to the " + channel.name + " because " + ex.message)
        }
    }

    fun sendMessageComplete(channel: MessageChannel, message: String): Message? {
        try {
            return channel.sendMessage(message).complete()
        } catch (ex: PermissionException) {
            DiscordBot.instance.logger.warning("Could not send message to the " + channel.name + " because " + ex.message)
        }
        return null
    }

    fun sendMessageComplete(channel: MessageChannel, message: MessageEmbed): Message? {
        try {
            return channel.sendMessage(message).complete()
        } catch (ex: PermissionException) {
            DiscordBot.instance.logger.warning("Could not send message to the " + channel.name + " because " + ex.message)
        }
        return null
    }

    fun editMessage(currentMessage: Message, newMessage: String) {
        currentMessage.editMessage(newMessage).queue()
    }

    fun editMessage(currentMessage: Message, newMessage: MessageEmbed) {
        currentMessage.editMessage(newMessage).queue()
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
        val nonInteractableRolesToAdd = rolesToAddFiltered.filter { role: Role -> !member.guild.selfMember.canInteract(role) }
        rolesToAddFiltered.removeAll(nonInteractableRolesToAdd.toSet())
        nonInteractableRolesToAdd.forEach { role: Role -> DiscordBot.instance.logger.warning("Failed to add role " + role.name + " to " + member.effectiveName + " because the bot's highest role is lower than the target role and thus can't interact with it") }
        rolesToRemove
                .filter { role: Role -> !role.isManaged }
                .filter { role: Role -> role.guild.publicRole.id != role.id }
                .filter { role: Role -> member.roles.contains(role) }
                .toCollection(rolesToRemoveFiltered)
        val nonInteractableRolesToRemove = rolesToRemoveFiltered.filter { role: Role -> !member.guild.selfMember.canInteract(role) }
        rolesToRemoveFiltered.removeAll(nonInteractableRolesToRemove.toSet())
        nonInteractableRolesToRemove.forEach { role: Role -> DiscordBot.instance.logger.warning("Failed to remove role " + role.name + " from " + member.effectiveName + " because the bot's highest role is lower than the target role and thus can't interact with it") }
        member.guild.modifyMemberRoles(member, rolesToAddFiltered, rolesToRemoveFiltered).queue()
    }

    fun generateToken(): String {
        return BigInteger(64, SecureRandom()).toString(32)
    }

    fun isJsonValid(json: String): Boolean {
        return try {
            isJsonValid(StringReader(json))
        } catch (ignored: IOException) {
            false
        }
    }

    @Throws(IOException::class)
    private fun isJsonValid(reader: Reader): Boolean {
        return isJsonValid(JsonReader(reader))
    }

    @Throws(IOException::class, AssertionError::class)
    private fun isJsonValid(jsonReader: JsonReader): Boolean {
        return try {
            var token: JsonToken?
            while (jsonReader.peek().also { token = it } != JsonToken.END_DOCUMENT && token != null) {
                when (token) {
                    JsonToken.BEGIN_ARRAY -> jsonReader.beginArray()
                    JsonToken.END_ARRAY -> jsonReader.endArray()
                    JsonToken.BEGIN_OBJECT -> jsonReader.beginObject()
                    JsonToken.END_OBJECT -> jsonReader.endObject()
                    JsonToken.NAME -> jsonReader.nextName()
                    JsonToken.STRING, JsonToken.NUMBER, JsonToken.BOOLEAN, JsonToken.NULL -> jsonReader.skipValue()
                    else -> throw AssertionError(token)
                }
            }
            true
        } catch (ignored: MalformedJsonException) {
            false
        }
    }
}