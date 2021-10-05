package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import net.dv8tion.jda.api.OnlineStatus
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.placeholders.PlaceholderHook
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

class DiscordPlaceholders(private val discordBot: DiscordBot) : PlaceholderHook() {
    override fun onPlaceholderRequest(player: Player?, parameters: String): String? {
        when (parameters.lowercase()) {
            "users" -> return discordBot.discordUserManager.discordUsers.size.toString()
            "guild_id" -> return discordBot.discordManager.guild.id
            "guild_name" -> return discordBot.discordManager.guild.name
            "guild_members_online" -> return discordBot.discordManager.guild.members.filter { it.onlineStatus === OnlineStatus.ONLINE }.size.toString()
            "guild_members_total" -> return discordBot.discordManager.guild.memberCount.toString()
            "guild_boost_count" -> return discordBot.discordManager.guild.boostCount.toString()
            "is_registered" -> {
                if (player == null) return null
                return discordBot.discordUserManager.discordUsers.containsKey(player.uuid).toString()
            }
            "user_id" -> {
                if (player == null) return null
                return discordBot.discordUserManager.getDiscordIdByUuid(player.uuid) ?: return ""
            }
            "user_name" -> {
                if (player == null) return null
                val userId = discordBot.discordUserManager.getDiscordIdByUuid(player.uuid) ?: return ""
                val member = discordBot.guild.getMemberById(userId) ?: return "Member not found"
                return member.user.name
            }
            "member_name" -> {
                if (player == null) return null
                val userId = discordBot.discordUserManager.getDiscordIdByUuid(player.uuid) ?: return ""
                val member = discordBot.guild.getMemberById(userId) ?: return "Member not found"
                return member.effectiveName
            }
            "user_current_voicechannel" -> {
                if (player == null) return null
                val userId = discordBot.discordUserManager.getDiscordIdByUuid(player.uuid) ?: return ""
                val member = discordBot.guild.getMemberById(userId) ?: return "Member not found"
                return member.voiceState?.channel?.name ?: "null"
            }
            else -> {
                if (parameters.startsWith("is_member_of_guild_")) {
                    val userId = parameters.replace("is_member_of_guild_", "")
                    val member = discordBot.discordManager.guild.getMemberById(userId)
                        ?: return "$userId is not a member of the discord server."
                    return "$userId (${member.asMention}) is a member of the discord server."
                }
            }
        }
        return null
    }

    override val identifier: String = "discordbot"
}