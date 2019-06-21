package nl.chimpgamer.networkmanager.extensions.discordbot.utils.tasks;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.api.events.PlayerVerifyEvent;
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token;
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordUserManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.MCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import java.sql.SQLException;

@Getter
@RequiredArgsConstructor
public class VerifyUserTask implements Runnable {
    private final DiscordBot discordBot;
    private final Player player;
    private final Token token;

    @Override
    public void run() {
        if (this.getToken() == null) {
            this.getPlayer().sendMessage(MCMessage.VERIFY_INVALID_TOKEN.getMessage()
                    .replace("%playername%", this.getPlayer().getName()));
            return;
        }

        if (this.getToken().getCreated() + 300000 < System.currentTimeMillis()) {
            // Token Expired
            this.getPlayer().sendMessage(MCMessage.VERIFY_TOKEN_EXPIRED.getMessage()
                    .replace("%playername%", this.getPlayer().getName()));
        } else {
            try {
                Preconditions.checkNotNull(getDiscordBot().getGuild(), "The discord bot has not been connected to a discord server. Connect it to a discord server.");

                DiscordUserManager discordUserManager = this.getDiscordBot().getDiscordUserManager();

                Member member = getDiscordBot().getGuild().getMemberById(this.getToken().getDiscordID());
                if (member == null) {
                    this.getPlayer().sendMessage(MCMessage.VERIFY_HELP.getMessage());
                    return;
                }
                if (discordUserManager.checkUser(this.getPlayer().getUuid().toString())) {
                    // User is already registered...
                    this.getPlayer().sendMessage(MCMessage.VERIFY_ACCOUNT_ALREADY_LINKED.getMessage()
                            .replace("%playername%", this.getPlayer().getName()));
                } else {
                    // User is not registered yet...
                    discordUserManager.getTokens().remove(this.getToken()); // Remove token
                    discordUserManager.insertUser(this.getPlayer().getUuid(), this.getToken().getDiscordID());
                    Utils.editMessage(this.getToken().getMessage(), DCMessage.REGISTRATION_COMPLETED.getMessage());
                    this.getPlayer().sendMessage(MCMessage.VERIFY_COMPLETED.getMessage()
                            .replace("%playername%", this.getPlayer().getName()));
                    this.getDiscordBot().getNetworkManager().getEventHandler().callEvent(new PlayerVerifyEvent(this.getPlayer(), member));
                    if (this.getDiscordBot().getNetworkManager().isRedisBungee()) {
                        this.getDiscordBot().sendRedisBungee("load " + this.getPlayer().getUuid());
                    }

                    if (this.getDiscordBot().getConfigManager().isVerifyAddRole()) {
                        String verifyRoleName = this.getDiscordBot().getConfigManager().getVerifyRole();
                        if (!verifyRoleName.isEmpty()) {
                            Role verifiedRole = Utils.getRoleByName(verifyRoleName);
                            if (verifiedRole != null) {
                                this.getDiscordBot().getGuild().getController().addSingleRoleToMember(member, verifiedRole).queue();
                            }
                        }
                    }

                    if (this.getDiscordBot().getConfigManager().isSyncUserName()) {
                        Utils.setNickName(member, this.getPlayer().getName());
                    }

                    if (this.getDiscordBot().getConfigManager().isSyncRanks()) {
                        Utils.syncRanks(this.getPlayer());
                    }
                }
            } catch (SQLException ex) {
                this.getPlayer().sendMessage(MCMessage.VERIFY_ERROR.getMessage()
                        .replace("%playername%", this.getPlayer().getName()));
                ex.printStackTrace();
            }
        }
    }
}