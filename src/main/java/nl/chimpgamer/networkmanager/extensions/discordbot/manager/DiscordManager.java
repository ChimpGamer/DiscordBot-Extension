package nl.chimpgamer.networkmanager.extensions.discordbot.manager;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord.*;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.DiscordListener;

import javax.security.auth.login.LoginException;
import java.util.List;

public class DiscordManager {
    private final DiscordBot discordBot;
    private final EventWaiter eventWaiter;
    private final CommandClientBuilder commandClientBuilder;

    private JDA JDA;
    private Guild guild;

    public DiscordManager(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.eventWaiter = new EventWaiter();
        this.commandClientBuilder = new CommandClientBuilder();
    }

    public boolean init() {
        boolean success = true;
        try {
            this.initCommandBuilder();
            this.initJDA();
        } catch (LoginException | InterruptedException ex) {
            ex.printStackTrace();
            success = false;
        }

        List<Guild> guilds = this.getJDA().getGuilds();

        this.guild = guilds.size() != 0 ? guilds.get(0) : null;

        if (guild == null) {
            this.getDiscordBot().getLogger().warning("The Bot is not a member of a guild");
            success = false;
        }

        if (guilds.size() > 1) {
            this.getDiscordBot().getLogger().warning("The Bot is a member of too many guilds.");
            success = false;
        }

        return success;
    }

    private void initJDA() throws LoginException, InterruptedException {
        this.JDA = new JDABuilder(AccountType.BOT)
                .setToken(this.getDiscordBot().getConfigManager().getDiscordToken())
                .addEventListener(new DiscordListener(this.getDiscordBot()))
                .addEventListener(this.getEventWaiter())
                .addEventListener(this.getCommandClientBuilder().build())
                .setAutoReconnect(true)
                .setMaxReconnectDelay(180)
                .build()
                .awaitReady();
    }

    private void initCommandBuilder() {
        this.getCommandClientBuilder()
                .setPrefix(this.getDiscordBot().getConfigManager().getCommandPrefix())
                .setOwnerId(this.getDiscordBot().getConfigManager().getOwnerId())

                .addCommands(
                        new PlayerListCommand(this.getDiscordBot()),
                        new PlayersCommand(this.getDiscordBot()),
                        new RegisterCommand(this.getDiscordBot()),
                        new UnregisterCommand(this.getDiscordBot()),
                        new PlaytimeCommand(this.getDiscordBot()),
                        new UptimeCommand(this.getDiscordBot())
                );
        if (this.getDiscordBot().getConfigManager().isStatusEnabled()) {
            Game.GameType gameType;
            try {
                gameType = Game.GameType.valueOf(this.getDiscordBot().getConfigManager().getStatusType().toUpperCase());
            } catch (IllegalArgumentException ex) {
                this.getDiscordBot().getLogger().warning("StatusType '" + this.getDiscordBot().getConfigManager().getStatusType() + "' is invalid. Using DEFAULT.");
                gameType = Game.GameType.DEFAULT;
            }
            this.getCommandClientBuilder()
                    .setGame(Game.of(gameType, this.getDiscordBot().getConfigManager().getStatusMessage()
                            .replace("%player%", String.valueOf(this.getDiscordBot().getNetworkManager().getProxy().getPlayers().size()))));
        } else {
            this.getCommandClientBuilder().setGame(null);
        }
    }

    public void shutdownJDA() {
        if (this.getJDA() != null) {
            this.getDiscordBot().getLogger().info("Shutting down JDA...");
            this.getJDA().shutdown();
        }
    }

    public boolean restartJDA() {
        this.shutdownJDA();
        try {
            this.initJDA();
            return true;
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private CommandClientBuilder getCommandClientBuilder() {
        return commandClientBuilder;
    }

    private EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    public Guild getGuild() {
        return guild;
    }

    public JDA getJDA() {
        return JDA;
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}