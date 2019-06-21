package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.MCMessage;
import nl.chimpgamer.networkmanager.api.commands.NMBungeeCommand;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.api.sender.Sender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VerifyCommand extends NMBungeeCommand {
    private final DiscordBot discordBot;

    public VerifyCommand(DiscordBot discordBot, String cmd) {
        super(discordBot.getNetworkManager(), cmd, Arrays.asList("networkmanager.bot.verify", "networkmanager.admin"));
        this.discordBot = discordBot;
        this.setPlayerOnly(true);
    }

    @Override
    public void onExecute(Sender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(MCMessage.VERIFY_HELP.getMessage()
                    .replace("%playername%", player.getName()));
        } else {
            String token = args[0];
            if (token.length() != 13) {
                // Invalid token
                player.sendMessage(MCMessage.VERIFY_INVALID_TOKEN.getMessage()
                        .replace("%playername%", player.getName()));
            } else {
                // Verify user with token
                this.getDiscordBot().getDiscordUserManager().verifyUser(player, token);
                this.getNetworkManager().debug("Verifying " + player.getRealName() + " with token: " + token);
            }
        }
    }

    @Override
    public List<String> onTabComplete(Sender sender, String[] args) {
        return Collections.emptyList();
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}