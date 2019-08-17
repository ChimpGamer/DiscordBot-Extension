package nl.chimpgamer.networkmanager.extensions.discordbot.manager;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.commands.discord.*;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting;
import nl.chimpgamer.networkmanager.extensions.discordbot.listeners.DiscordListener;
import nl.chimpgamer.networkmanager.extensions.discordbot.utils.Utils;

import javax.security.auth.login.LoginException;
import java.util.List;

public class DiscordManager {
    private final DiscordBot discordBot;
    private final EventWaiter eventWaiter;
    private final CommandClientBuilder commandClientBuilder;

    private JDA JDA;
    private Guild guild;

    private Role verifiedRole;

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

        String roleName = Setting.DISCORD_VERIFY_ADD_ROLE_ROLE_NAME.getAsString();
        Role role = Utils.getRoleByName(roleName);
        if (role != null) {
            this.getDiscordBot().getLogger().info("Verified Role is: '" + role.getName() + "' (" + role.getId() + ")");
        } else {
            this.getDiscordBot().getLogger().info("No Verified Role found by the name: '" + roleName + "'");
        }
        this.setVerifiedRole(role);

        return success;
    }

    private void initJDA() throws LoginException, InterruptedException {
        this.JDA = new JDABuilder(AccountType.BOT)
                .setToken(Setting.DISCORD_TOKEN.getAsString())
                .addEventListeners(new DiscordListener(this.getDiscordBot()))
                .addEventListeners(this.getEventWaiter())
                .addEventListeners(this.getCommandClientBuilder().build())
                .setAutoReconnect(true)
                .setMaxReconnectDelay(180)
                .build()
                .awaitReady();
    }

    private void initCommandBuilder() {
        this.getCommandClientBuilder()
                .setPrefix(Setting.DISCORD_COMMAND_PREFIX.getAsString())
                .setOwnerId(Setting.DISCORD_OWNER_ID.getAsString())

                .addCommands(
                        new PlayerListCommand(this.getDiscordBot()),
                        new PlayersCommand(this.getDiscordBot()),
                        new RegisterCommand(this.getDiscordBot()),
                        new UnregisterCommand(this.getDiscordBot()),
                        new PlaytimeCommand(this.getDiscordBot()),
                        new UptimeCommand()
                );
        if (Setting.DISCORD_STATUS_ENABLED.getAsBoolean()) {
            Activity.ActivityType activityType;
            try {
                activityType = Activity.ActivityType.valueOf(Setting.DISCORD_STATUS_TYPE.getAsString().toUpperCase());
            } catch (IllegalArgumentException ex) {
                this.getDiscordBot().getLogger().warning("StatusType '" + Setting.DISCORD_STATUS_TYPE.getAsString() + "' is invalid. Using DEFAULT.");
                activityType = Activity.ActivityType.DEFAULT;
            }
            this.getCommandClientBuilder()
                    .setActivity(Activity.of(activityType, Setting.DISCORD_STATUS_MESSAGE.getAsString()
                            .replace("%player%", String.valueOf(this.getDiscordBot().getNetworkManager().getProxy().getPlayers().size()))));
        } else {
            this.getCommandClientBuilder().setActivity(null);
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

    public void setVerifiedRole(Role verifiedRole) {
        this.verifiedRole = verifiedRole;
    }

    public Role getVerifiedRole() {
        return verifiedRole;
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