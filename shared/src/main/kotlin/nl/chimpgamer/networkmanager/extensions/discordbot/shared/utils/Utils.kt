package nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.MalformedJsonException
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.exceptions.PermissionException
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*
import java.util.function.Function

object Utils {
    val UUID_REGEX = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
    val DISCORD_ID_REGEX = Regex("\\d{17,20}")
    val USER_MENTION_REGEX = Regex("<@!?(\\d{17,20})>")
    val ROLE_MENTION_REGEX = Regex("<@&(\\d{17,20})>")

    private lateinit var discordBot: DiscordBot

    fun initialize(discordBot: DiscordBot) {
        this.discordBot = discordBot
    }

    fun sendChannelMessage(channel: MessageChannel, message: String) {
        try {
            channel.sendMessage(message).queue()
        } catch (ex: PermissionException) {
            discordBot.platform.warn("Could not send message to the " + channel.name + " because " + ex.message)
        }
    }

    fun sendChannelMessage(channel: MessageChannel, message: MessageEmbed) {
        try {
            channel.sendMessageEmbeds(message).queue()
        } catch (ex: PermissionException) {
            discordBot.platform.warn("Could not send message to the " + channel.name + " because " + ex.message)
        }
    }

    @Throws(InsufficientPermissionException::class)
    fun modifyRolesOfMember(member: Member, rolesToAdd: MutableSet<Role>, rolesToRemove: MutableSet<Role>) {
        val rolesToAddFiltered = rolesToAdd
            .asSequence()
            .filter { role: Role -> !role.isManaged }
            .filter { role: Role -> role.guild.publicRole.id != role.id }
            .filter { role: Role -> !member.roles.contains(role) }
            .toMutableSet()
        val rolesToRemoveFiltered = rolesToRemove
            .asSequence()
            .filter { role: Role -> !role.isManaged }
            .filter { role: Role -> role.guild.publicRole.id != role.id }
            .filter { role: Role -> member.roles.contains(role) }
            .toMutableSet()

        val nonInteractableRolesToAdd = rolesToAddFiltered.filter { role: Role -> !member.guild.selfMember.canInteract(role) }
        rolesToAddFiltered.removeAll(nonInteractableRolesToAdd.toSet())
        nonInteractableRolesToAdd.forEach { role: Role -> discordBot.platform.warn("Failed to add role " + role.name + " to " + member.effectiveName + " because the bot's highest role is lower than the target role and thus can't interact with it") }

        val nonInteractableRolesToRemove = rolesToRemoveFiltered.filter { role: Role -> !member.guild.selfMember.canInteract(role) }
        rolesToRemoveFiltered.removeAll(nonInteractableRolesToRemove.toSet())
        nonInteractableRolesToRemove.forEach { role: Role -> discordBot.platform.warn("Failed to remove role " + role.name + " from " + member.effectiveName + " because the bot's highest role is lower than the target role and thus can't interact with it") }

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
        } catch (ignored: AssertionError) {
            false
        }
    }

    @Throws(IOException::class, AssertionError::class)
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

internal fun EmbedBuilder.parsePlaceholdersToFields(handler: Function<String, String>) = apply {
    val fields: MutableList<MessageEmbed.Field> = LinkedList()
    for (field in this.fields) {
        var name = field.name
        var value = field.value

        if (name != null) {
            name = handler.apply(name)
        }

        if (value != null) {
            value = handler.apply(value)
        }

        fields.add(MessageEmbed.Field(name, value, field.isInline))
    }

    clearFields()
    fields.forEach(::addField)
}