package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import net.dv8tion.jda.api.OnlineStatus
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.placeholders.PlaceholderHook
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot

class DiscordPlaceholders(private val discordBot: DiscordBot) : PlaceholderHook() {
    override fun onPlaceholderRequest(player: Player?, parameters: String): String? {
        when (parameters.toLowerCase()) {
            "users" -> return discordBot.discordUserManager.discordUsers.size.toString()
            "guild_id" -> return discordBot.discordManager.guild.id
            "guild_name" -> return discordBot.discordManager.guild.name
            "guild_members_online" -> return discordBot.discordManager.guild.members.filter { it.onlineStatus === OnlineStatus.ONLINE }.size.toString()
            "guild_members_total" -> return discordBot.discordManager.guild.memberCount.toString()
            else -> {
                if (parameters.startsWith("is_member_of_guild")) {
                    val userId = parameters.replace("is_member_of_guild", "")
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