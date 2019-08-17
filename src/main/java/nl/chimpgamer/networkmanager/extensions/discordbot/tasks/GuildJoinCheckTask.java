package nl.chimpgamer.networkmanager.extensions.discordbot.tasks;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import nl.chimpgamer.networkmanager.api.cache.modules.CachedPlayers;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.DCMessage;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

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
                    if (Setting.DISCORD_VERIFY_ADD_ROLE_ENABLED.getAsBoolean()) {
                        Role verifiedRole = this.getDiscordBot().getDiscordManager().getVerifiedRole();
                        if (verifiedRole != null) {
                            this.getDiscordBot().getLogger().info("Assigning the " + verifiedRole.getName() + " role to " + member.getEffectiveName());
                            Utils.addRoleToMember(member, verifiedRole);
                        }
                    }


                    if (Setting.DISCORD_SYNC_USERNAME.getAsBoolean()) {
                        Utils.setNickName(this.getMember(), player.getName());
                    }

                    if (Setting.DISCORD_SYNC_RANKS_ENABLED.getAsBoolean()) {
                        Utils.syncRanks(player);
                    }
                } else {
                    String welcomeMessage = DCMessage.WELCOME_NEW_MEMBER.getMessage();
                    if (welcomeMessage.isEmpty()) {
                        return;
                    }
                    this.getMember().getUser().openPrivateChannel().queue(channel -> channel.sendMessage(welcomeMessage.replace("%mention%", this.getMember().getUser().getAsTag())).queue());
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