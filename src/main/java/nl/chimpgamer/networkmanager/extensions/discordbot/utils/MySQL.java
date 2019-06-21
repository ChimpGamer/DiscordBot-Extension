package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.api.communication.IMySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQL {
    private final DiscordBot discordBot;
    private IMySQL nmMySQL;

    public MySQL(DiscordBot discordBot) {
        this.discordBot = discordBot;
        if (this.getDiscordBot().getNetworkManager().getMySQL().ping()) {
            this.getDiscordBot().getNetworkManager().debug("&c| &cSuccessfully connected with MySQL! (NetworkManagerBot)");
            this.nmMySQL = this.getDiscordBot().getNetworkManager().getMySQL();
            this.getDiscordBot().getScheduler().runAsync(() -> {
                try {
                    this.create();
                    this.getDiscordBot().getNetworkManager().debug("&c| &cDone with Creating MySQL things! (NetworkManagerBot)");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }, false);
        } else {
            this.getDiscordBot().error("&c| &cNo connection to Database (NetworkManagerBot)");
        }
    }

    private void create() throws SQLException {
        String nm_discordusers =
                "CREATE TABLE IF NOT EXISTS nm_discordusers(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, UUID VARCHAR(36), DiscordID VARCHAR(64), registered LONG) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_unicode_ci;";

        this.createTable(nm_discordusers);
    }

    private void createTable(String sql) throws SQLException {
        try (Connection connection = this.getNmMySQL().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private IMySQL getNmMySQL() {
        return nmMySQL;
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}