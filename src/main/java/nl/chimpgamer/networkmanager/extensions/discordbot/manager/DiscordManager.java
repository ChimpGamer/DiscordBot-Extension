package nl.chimpgamer.networkmanager.extensions.discordbot.manager;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.core.entities.Game;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord.PlayerListCommand;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord.PlayersCommand;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord.RegisterCommand;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord.UnregisterCommand;

public class DiscordManager {
    private final DiscordBot discordBot;
    private CommandClientBuilder commandClientBuilder;

    public DiscordManager(DiscordBot discordBot) {
        this.discordBot = discordBot;
        this.commandClientBuilder = new CommandClientBuilder();
    }

    public void init() {
        this.getCommandClientBuilder()
                .setPrefix(this.getDiscordBot().getConfigManager().getCommandPrefix())
                .setOwnerId(this.getDiscordBot().getConfigManager().getOwnerId())

                .addCommands(
                        new PlayerListCommand(this.getDiscordBot()),
                        new PlayersCommand(this.getDiscordBot()),
                        new RegisterCommand(this.getDiscordBot()),
                        new UnregisterCommand(this.getDiscordBot())
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

    public CommandClientBuilder getCommandClientBuilder() {
        return commandClientBuilder;
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}