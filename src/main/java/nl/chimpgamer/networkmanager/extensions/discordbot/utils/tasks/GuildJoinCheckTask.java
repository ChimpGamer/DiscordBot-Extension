package nl.chimpgamer.networkmanager.extensions.discordbot.utils.tasks;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.entities.Member;

import net.dv8tion.jda.core.entities.Role;
import nl.chimpgamer.networkmanager.api.cache.modules.CachedPlayers;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;
import nl.chimpgamer.networkmanager.api.models.player.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class GuildJoinCheckTask implements Runnable {
    private final DiscordBot discordBot;
    private final Member member;

    @Override
    public void run() {
        final CachedPlayers cachedPlayers = this.getDiscordBot().getNetworkManager().getCacheManager().getCachedPlayers();

        try (Connection connection = this.getDiscordBot().getNetworkManager().getMySQL().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT DiscordID, UUID FROM nm_discordusers WHERE DiscordID=?;")) {
            ps.setString(1, this.getMember().getUser().getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Optional<Player> opPlayer = cachedPlayers.getPlayerSafe(UUID.fromString(rs.getString("UUID")));
                    if (!opPlayer.isPresent()) {
                        return;
                    }
                    Player player = opPlayer.get();

                    if (this.getDiscordBot().getConfigManager().isVerifyAddRole()) {
                        String verifyRoleName = this.getDiscordBot().getConfigManager().getVerifyRole();
                        if (!verifyRoleName.isEmpty()) {
                            Role verifiedRole = Utils.getRoleByName(verifyRoleName);
                            if (verifiedRole != null) {
                                this.getDiscordBot().getGuild().getController().addSingleRoleToMember(member, verifiedRole).queue();
                            } else {
                                this.getDiscordBot().warn("The verified role '" + verifyRoleName + "' does not seem to exist!");
                            }
                        } else {
                            this.getDiscordBot().warn("The verified role '" + verifyRoleName + "' does not seem to exist!");
                        }
                    }


                    if (this.getDiscordBot().getConfigManager().isSyncUserName()) {
                        Utils.setNickName(this.getMember(), player.getName());
                    }

                    if (this.getDiscordBot().getConfigManager().isSyncRanks()) {
                        Utils.syncRanks(player);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Member getMember() {
        return member;
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}