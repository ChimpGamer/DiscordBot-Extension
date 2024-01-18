package nl.chimpgamer.networkmanager.extensions.discordbot.shared.tasks

import net.dv8tion.jda.api.entities.Member
import nl.chimpgamer.networkmanager.api.models.player.Player
import nl.chimpgamer.networkmanager.api.utils.Placeholders
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.DiscordBot
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.api.events.PlayerRegisteredEvent
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.api.models.Token
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.DCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.MCMessage
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.configurations.Setting
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.modals.JsonMessageEmbed
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.RedisBungeeUtils
import nl.chimpgamer.networkmanager.extensions.discordbot.shared.utils.Utils
import java.sql.SQLException

class VerifyUserTask(private val discordBot: DiscordBot, private val player: Player, private val token: Token) :
    Runnable {

    override fun run() {
        if (token.created + 300000 < System.currentTimeMillis()) { // Token Expired
            player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_TOKEN_EXPIRED))
        } else {
            try {
                checkNotNull(discordBot.guild) { "The discord bot has not been connected to a discord server. Connect it to a discord server." }
                val discordUserManager = this.discordBot.discordUserManager
                val member: Member? = this.discordBot.guild.getMemberById(this.token.discordID)
                if (member == null) {
                    player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_NOT_IN_SERVER))
                    return
                }
                if (discordUserManager.existsInDatabase(this.player.uuid.toString())) { // User is already registered...
                    player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_ACCOUNT_ALREADY_LINKED))
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
                    player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_COMPLETED))
                    this.discordBot.platform.eventBus.post(PlayerRegisteredEvent(this.player, member))
                    if (this.discordBot.networkManager.isRedisBungee) {
                        RedisBungeeUtils.sendRedisBungeeMessage(discordBot, "load " + player.uuid)
                    }

                    val syncUsername = discordBot.settings.getBoolean(Setting.DISCORD_SYNC_USERNAME_ENABLED)
                    val addVerifiedRole = discordBot.settings.getBoolean(Setting.DISCORD_REGISTER_ADD_ROLE_ENABLED) && discordBot.discordManager.verifiedRole != null

                    if (syncUsername && addVerifiedRole) {
                        val nickname = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
                        discordBot.discordManager.setNickNameAndAddRole(member, nickname, discordBot.discordManager.verifiedRole!!)
                    } else {
                        if (syncUsername) {
                            val nickname = Placeholders.setPlaceholders(player, discordBot.settings.getString(Setting.DISCORD_SYNC_USERNAME_FORMAT))
                            discordBot.discordManager.setNickName(member, nickname)
                        }
                        if (addVerifiedRole) {
                            discordBot.discordManager.verifiedRole?.let { discordBot.discordManager.addRoleToMember(member, it) }
                        }
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
                player.sendRichMessage(discordBot.messages.getString(MCMessage.REGISTER_ERROR))
                ex.printStackTrace()
            }
        }
    }
}