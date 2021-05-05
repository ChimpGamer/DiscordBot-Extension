package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.common.utils.Methods
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.events.PlayerRegisteredEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.JsonEmbedBuilder
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils
import java.sql.SQLException

class VerifyUserTask(private val discordBot: DiscordBot, private val player: Player, private val token: Token) : Runnable {

    override fun run() {
        if (this.token.created + 300000 < System.currentTimeMillis()) { // Token Expired
            this.player.sendMessage(discordBot.messages.getString(MCMessage.REGISTER_TOKEN_EXPIRED)
                    .replace("%playername%", this.player.name))
        } else {
            try {
                checkNotNull(discordBot.guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
                val discordUserManager = this.discordBot.discordUserManager
                val member: Member? = this.discordBot.guild.getMemberById(this.token.discordID)
                if (member == null) {
                    this.player.sendMessage(discordBot.messages.getString(MCMessage.REGISTER_NOT_IN_SERVER))
                    return
                }
                if (discordUserManager.existsInDatabase(this.player.uuid.toString())) { // User is already registered...
                    this.player.sendMessage(discordBot.messages.getString(MCMessage.REGISTER_ACCOUNT_ALREADY_LINKED)
                            .replace("%playername%", this.player.name))
                } else { // User is not registered yet...
                    discordUserManager.tokens.remove(this.token) // Remove token
                    discordUserManager.insertUser(this.player.uuid, this.token.discordID)
                    val registrationCompleted = discordBot.messages.getString(DCMessage.REGISTRATION_COMPLETED)
                    if (Methods.isJsonValid(registrationCompleted)) {
                        val jsonEmbedBuilder = JsonEmbedBuilder.fromJson(registrationCompleted)
                        Utils.editMessage(this.token.message, jsonEmbedBuilder.build())
                    } else {
                        Utils.editMessage(this.token.message, registrationCompleted)
                    }
                    this.player.sendMessage(discordBot.messages.getString(MCMessage.REGISTER_COMPLETED)
                            .replace("%playername%", this.player.name))
                    this.discordBot.networkManager.eventHandler.callEvent(PlayerRegisteredEvent(this.player, member))
                    if (this.discordBot.networkManager.isRedisBungee) {
                        this.discordBot.sendRedisBungee("load " + this.player.uuid)
                    }
                    if (discordBot.settings.getBoolean(Setting.DISCORD_REGISTER_ADD_ROLE_ENABLED)) {
                        val verifiedRole: Role? = this.discordBot.discordManager.verifiedRole
                        if (verifiedRole != null) {
                            this.discordBot.logger.info("Assigning the ${verifiedRole.name} role to ${member.effectiveName}")
                            discordBot.discordManager.addRoleToMember(member, verifiedRole)
                        }
                    }
                    if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME)) {
                        discordBot.discordManager.setNickName(member, this.player.name)
                    }
                    if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
                        discordBot.scheduler.runDelayed(SyncRanksTask(discordBot, player), 1)
                    }
                }
            } catch (ex: SQLException) {
                this.player.sendMessage(discordBot.messages.getString(MCMessage.REGISTER_ERROR)
                        .replace("%playername%", this.player.name))
                ex.printStackTrace()
            }
        }
    }
}