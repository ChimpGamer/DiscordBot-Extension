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
            "member_name" -> {
                if (player == null) return null
                val userId = discordBot.discordUserManager.getDiscordIdByUuid(player.uuid) ?: return null
                val member = discordBot.guild.getMemberById(userId) ?: return null
                return member.effectiveName
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