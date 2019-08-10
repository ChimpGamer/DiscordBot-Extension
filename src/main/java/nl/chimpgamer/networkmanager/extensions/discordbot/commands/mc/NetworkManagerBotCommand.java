package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.MCMessage;
import nl.chimpgamer.networkmanager.api.commands.NMBungeeCommand;
import nl.chimpgamer.networkmanager.api.sender.Sender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NetworkManagerBotCommand extends NMBungeeCommand {
    private final DiscordBot discordBot;

    public NetworkManagerBotCommand(DiscordBot discordBot, String cmd) {
        super(discordBot.getNetworkManager(), cmd, Collections.singletonList("networkmanager.admin"), "nmbot");
        this.discordBot = discordBot;
    }

    @Override
    public void onExecute(Sender sender, String[] args) {
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reload")) {
                switch (args[1].toLowerCase()) {
                    case "config":
                    case "settings":
                        getDiscordBot().getSettings().reload();
                        sender.sendMessage(MCMessage.RELOAD_CONFIG.getMessage());
                        break;
                    case "messages":
                        getDiscordBot().getMessages().reload();
                        sender.sendMessage(MCMessage.RELOAD_MESSAGES.getMessage());
                        break;
                    case "jda":
                        boolean success = getDiscordBot().getDiscordManager().restartJDA();
                        if (success) {
                            sender.sendMessage(MCMessage.RELOAD_JDA_SUCCESS.getMessage());
                        } else {
                            sender.sendMessage(MCMessage.RELOAD_JDA_FAILED.getMessage());
                        }
                        break;
                }
            }
        }
    }

    @Override
    public List<String> onTabComplete(Sender sender, String[] args) {
        if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
            return Collections.singletonList("reload");
        }
        if (args.length == 2) {
            return Arrays.asList("config", "settings", "messages", "jda");
        }
        return Collections.emptyList();
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}