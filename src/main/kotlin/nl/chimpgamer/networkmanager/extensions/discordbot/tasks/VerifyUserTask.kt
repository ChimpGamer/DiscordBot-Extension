package nl.chimpgamer.networkmanager.extensions.discordbot.tasks

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.api.events.PlayerRegisteredEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.RedisBungeeUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils
import java.sql.SQLException

class VerifyUserTask(private val discordBot: DiscordBot, private val player: Player, private val token: Token) :
    Runnable {

    override fun run() {
        if (token.created + 300000 < System.currentTimeMillis()) { // Token Expired
            player.sendMessage(
                discordBot.messages.getString(MCMessage.REGISTER_TOKEN_EXPIRED)
                    .replace("%playername%", this.player.name)
            )
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
                    this.player.sendMessage(
                        discordBot.messages.getString(MCMessage.REGISTER_ACCOUNT_ALREADY_LINKED)
                            .replace("%playername%", this.player.name)
                    )
                } else {
                    // User is not registered yet...
                    discordUserManager.tokens.remove(this.token) // Remove token
                    discordUserManager.insertUser(this.player.uuid, this.token.discordID)
                    val registrationCompleted = discordBot.messages.getString(DCMessage.REGISTRATION_COMPLETED)
                    if (Utils.isJsonValid(registrationCompleted)) {
                        val jsonMessageEmbed = JsonMessageEmbed.fromJson(registrationCompleted)
                        token.interaction.editOriginalEmbeds(jsonMessageEmbed.toMessageEmbed()).queue()
                    } else {
                        token.interaction.editOriginal(registrationCompleted).queue()
                    }
                    this.player.sendMessage(
                        discordBot.messages.getString(MCMessage.REGISTER_COMPLETED)
                            .replace("%playername%", this.player.name)
                    )
                    this.discordBot.eventBus.post(PlayerRegisteredEvent(this.player, member))
                    if (this.discordBot.networkManager.isRedisBungee) {
                        RedisBungeeUtils.sendRedisBungeeMessage(discordBot, "load " + player.uuid)
                    }
                    if (discordBot.settings.getBoolean(Setting.DISCORD_REGISTER_ADD_ROLE_ENABLED)) {
                        val verifiedRole: Role? = this.discordBot.discordManager.verifiedRole
                        if (verifiedRole != null) {
                            this.discordBot.logger.info("Assigning the ${verifiedRole.name} role to ${member.effectiveName}")
                            discordBot.discordManager.addRoleToMember(member, verifiedRole)
                        }
                    }
                    if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME_ENABLED)) {
                        val format = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
                        discordBot.discordManager.setNickName(member, format)
                    }
                    if (discordBot.settings.getBoolean(Setting.DISCORD_SYNC_RANKS_ENABLED)) {
                        discordBot.scheduler.runDelayed(SyncRanksTask(discordBot, player), 1)
                    }

                    val executeCommands = discordBot.settings.getStringList(Setting.DISCORD_REGISTER_EXECUTE_COMMANDS)
                    if (executeCommands.isNotEmpty()) {
                        executeCommands.forEach { command ->
                            discordBot.networkManager.executeConsoleCommand(command
                                .replace("%playeruuid%", player.uuid.toString())
                                .replace("%playername%", player.name))
                        }
                    }
                }
            } catch (ex: SQLException) {
                this.player.sendMessage(
                    discordBot.messages.getString(MCMessage.REGISTER_ERROR)
                        .replace("%playername%", this.player.name)
                )
                ex.printStackTrace()
            }
        }
    }
}