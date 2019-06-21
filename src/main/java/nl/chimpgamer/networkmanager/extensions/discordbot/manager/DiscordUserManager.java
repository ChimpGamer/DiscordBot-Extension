package nl.chimpgamer.networkmanager.extensions.discordbot.manager;

import lombok.Getter;
import net.dv8tion.jda.core.entities.Message;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.DiscordUser;
import nl.chimpgamer.networkmanager.extensions.discordbot.modals.NMToken;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.tasks.TokenExpiryTask;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.tasks.VerifyUserTask;
import nl.chimpgamer.networkmanager.extensions.discordbot.api.models.Token;
import nl.chimpgamer.networkmanager.api.models.player.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class DiscordUserManager {
    private final DiscordBot discordBot;
    private final Map<UUID, DiscordUser> discordUsers;
    private final List<Token> tokens;

    public DiscordUserManager(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.discordUsers = new ConcurrentHashMap<>();
        this.tokens = new ArrayList<>();
    }

    public void load() {
        this.getDiscordBot().getScheduler().runAsync(() -> {
            try (Connection connection = this.getDiscordBot().getNetworkManager().getMySQL().getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT UUID, DiscordID, registered FROM nm_discordusers");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    String discordId = rs.getString("DiscordID");
                    long registered = rs.getLong("registered");
                    this.discordUsers.put(uuid, new DiscordUser(uuid, discordId, registered));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }, false);
    }

    public void load(UUID uuid) {
        this.getDiscordBot().getScheduler().runAsync(() -> {
            try (Connection connection = this.getDiscordBot().getNetworkManager().getMySQL().getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT UUID, DiscordID, registered FROM nm_discordusers WHERE UUID=?;")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String discordId = rs.getString("DiscordID");
                        long registered = rs.getLong("registered");
                        this.discordUsers.put(uuid, new DiscordUser(uuid, discordId, registered));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }, false);
    }

    public void insertUser(UUID uuid, String discordID) throws SQLException {
        long now = System.currentTimeMillis();
        try (Connection connection = this.getDiscordBot().getNetworkManager().getMySQL().getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO nm_discordusers (UUID, DiscordID, registered) VALUES (?, ?, ?);")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, discordID);
            ps.setLong(3, now);
            ps.executeUpdate();
        }
        this.getDiscordUsers().put(uuid, new DiscordUser(uuid, discordID, now));
    }

    public void deleteUser(String discordID) {
        UUID uuid = this.getUuidByDiscordId(discordID);
        if (uuid != null) {
            this.getDiscordUsers().remove(uuid);
        }
        try (Connection connection = this.getDiscordBot().getNetworkManager().getMySQL().getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM nm_discordusers WHERE DiscordID=?;")) {
            ps.setString(1, discordID);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean checkUserByDiscordId(String discordId) throws SQLException {
        if (this.getUuidByDiscordId(discordId) == null) {
            try (Connection connection = this.getDiscordBot().getNetworkManager().getMySQL().getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM nm_discordusers WHERE DiscordID=?")) {
                ps.setString(1, discordId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } else {
            return true;
        }
    }

    public boolean checkUser(String uuid) throws SQLException {
        if (this.getDiscordIdByUuid(UUID.fromString(uuid)) == null) {
            try (Connection connection = this.getDiscordBot().getNetworkManager().getMySQL().getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM nm_discordusers WHERE UUID=?")) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } else {
            return true;
        }
    }

    public String getDiscordIdByUuid(UUID uuid) {
        if (getDiscordUsers().containsKey(uuid)) {
            return getDiscordUsers().get(uuid).getDiscordId();
        }
        return null;
    }

    public UUID getUuidByDiscordId(String id) {
        return this.getDiscordUsers().entrySet().stream().filter(entry -> entry.getValue().getDiscordId().equals(id)).map(Map.Entry::getKey).findAny().orElse(null);
    }

    public Token getToken(String token) {
        return this.getTokens().stream().filter(token1 -> token1.getToken().equalsIgnoreCase(token)).findAny().orElse(null);
    }

    public void verifyUser(Player player, String inputToken) {
        Token token = this.getToken(inputToken);
        VerifyUserTask vut = new VerifyUserTask(this.getDiscordBot(), player, token);
        this.getDiscordBot().getScheduler().runAsync(vut, false);
    }

    public void insertToken(String inputToken, String uuid, Message message) {
        Token token = new NMToken(inputToken, uuid, message);
        this.getTokens().add(token);
        this.getDiscordBot().getScheduler().runDelayed(new TokenExpiryTask(this.getDiscordBot(), token), 60L);
    }

    public boolean containsDiscordID(String discordID) {
        return this.getTokens().stream().anyMatch(token -> token.getDiscordID().equals(discordID));
    }
}