package nl.chimpgamer.networkmanager.extensions.discordbot.commands.mc;

import nl.chimpgamer.networkmanager.api.commands.NMBungeeCommand;
import nl.chimpgamer.networkmanager.api.sender.Sender;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.MCMessage;

import java.util.List;

public class DiscordCommand extends NMBungeeCommand {

    public DiscordCommand(DiscordBot discordBot, String name) {
        super(discordBot.getNetworkManager(), name, null);
        this.setPlayerOnly(true);
    }

    @Override
    public void onExecute(Sender sender, String[] strings) {
        sender.sendMessage(MCMessage.DISCORD_RESPONSE.getMessage());
    }

    @Override
    public List<String> onTabComplete(Sender sender, String[] strings) {
        return null;
    }
}
