package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.events.PlayerUnregisteredEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import java.sql.SQLException

class DeleteUserTask(private val discordBot: DiscordBot, private val player: Player) : Runnable {

    override fun run() {
        val discordUserManager = discordBot.discordUserManager;
        try {
            if (discordUserManager.existsInDatabase(player.uuid.toString())) {
                val discordId = discordUserManager.getDiscordIdByUuid(player.uuid)!!

                discordUserManager.deleteUserFromDatabase(player.uuid);
                discordUserManager.discordUsers.remove(player.uuid);

                val member = discordBot.guild.getMemberById(discordId)
                this.discordBot.eventHandler.callEvent(PlayerUnregisteredEvent(player, member))

                if (Setting.DISCORD_REGISTER_ADD_ROLE_ROLE_NAME.asBoolean) {
                    val verifiedRole = discordBot.discordManager.verifiedRole
                    if (verifiedRole != null && member != null) {
                        discordBot.guild.removeRoleFromMember(member, verifiedRole).queue()
                    }
                }

                if (discordBot.settings.getBoolean(Setting.DISCORD_UNREGISTER_KICK_ENABLED)) {
                    member?.kick(discordBot.settings.getString(Setting.DISCORD_UNREGISTER_KICK_REASON))
                }

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