package nl.chimpgamer.networkmanager.extensions.discordbot.listeners.bungee;

import com.google.common.base.Preconditions;
import net.dv8tion.jda.core.entities.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import net.md_5.bungee.event.EventPriority;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;
import nl.chimpgamer.networkmanager.api.models.player.Player;

import java.util.Optional;

public class JoinLeaveListener implements Listener {
    private final DiscordBot discordBot;

    public JoinLeaveListener(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PostLoginEvent event) {
        final ProxiedPlayer proxiedPlayer = event.getPlayer();
        if (proxiedPlayer == null) {
            return;
        }
        Optional<Player> opPlayer = getDiscordBot().getNetworkManager().getPlayerSafe(proxiedPlayer.getUniqueId());
        if (!opPlayer.isPresent()) {
            getDiscordBot().getLogger().info("Could not load player...");
            return;
        }
        Player player = opPlayer.get();

        String discordId = this.getDiscordBot().getDiscordUserManager().getDiscordIdByUuid(player.getUuid());
        if (discordId == null) {
            return;
        }

        Preconditions.checkNotNull(this.getDiscordBot().getGuild(), "The discord bot has not been connected to a discord server. Connect it to a discord server.");

        Member member = this.getDiscordBot().getGuild().getMemberById(discordId);
        if (member == null) {
            return;
        }

        if (this.getDiscordBot().getConfigManager().isSyncUserName()) {
            Utils.setNickName(member, player.getName());
        }

        if (this.getDiscordBot().getConfigManager().isSyncRanks()) {
            Utils.syncRanks(player);
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}