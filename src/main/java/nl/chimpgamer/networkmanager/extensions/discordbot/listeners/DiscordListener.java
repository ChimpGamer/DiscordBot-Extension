package nl.chimpgamer.networkmanager.extensions.discordbot.listeners;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting;
import nl.chimpgamer.networkmanager.extensions.discordbot.manager.DiscordUserManager;
import nl.chimpgamer.networkmanager.extensions.discordbot.tasks.GuildJoinCheckTask;
import nl.chimpgamer.networkmanager.api.cache.modules.CachedPlayers;
import nl.chimpgamer.networkmanager.api.cache.modules.CachedValues;
import nl.chimpgamer.networkmanager.api.communication.PubSubMessage;
import nl.chimpgamer.networkmanager.api.communication.PubSubMessageType;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.api.utils.GsonUtils;
import nl.chimpgamer.networkmanager.api.values.Command;

import java.util.UUID;

public class DiscordListener extends ListenerAdapter {
    private final DiscordBot discordBot;

    public DiscordListener(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        final DiscordUserManager discordUserManager = this.getDiscordBot().getDiscordUserManager();
        final CachedPlayers cachedPlayers = this.getDiscordBot().getNetworkManager().getCacheManager().getCachedPlayers();
        final CachedValues cachedValues = this.getDiscordBot().getNetworkManager().getCacheManager().getCachedValues();

        User user = event.getAuthor();
        TextChannel channel = event.getTextChannel();
        Message message = event.getMessage();

        String msg = message.getContentStripped();

        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.isFromType(ChannelType.TEXT) && channel.getGuild().equals(this.getDiscordBot().getGuild())) {
            if (channel.getId().equals(Setting.DISCORD_EVENTS_ADMINCHAT_CHANNEL.getAsString())) {
                UUID uuid = discordUserManager.getUuidByDiscordId(user.getId());
                if (uuid == null) {
                    return;
                }

                Player player = cachedPlayers.getPlayer(uuid);
                if (cachedValues.getBoolean(Command.ADMINCHAT_ENABLED)) {
                    String alert = this.getDiscordBot().getNetworkManager().getMessage(player.getLanguage(), "lang_adminchat_message")
                            .replace("%playername%", player.getRealName())
                            .replace("%username%", player.getUserName())
                            .replace("%nickname%", player.getNicknameOrUserName())
                            .replace("%server%", "Discord")
                            .replace("%message%", msg);
                    String perm1 = "networkmanager.adminchat";
                    String perm2 = "networkmanager.admin";
                    if (this.getDiscordBot().getNetworkManager().isRedisBungee()) {
                        PubSubMessage pubSubMessage = new PubSubMessage(PubSubMessageType.ADMINCHAT,
                                this.getDiscordBot().getNetworkManager().getRedisBungee().getServerId(), perm1 + " " + perm2 + " " + alert);
                        this.getDiscordBot().getNetworkManager().sendRedisBungee(GsonUtils.getGson().toJson(pubSubMessage));
                    } else {
                        this.getDiscordBot().getNetworkManager().sendMessageToStaff(alert, "all", perm1, perm2);
                    }
                }
            } else if (channel.getId().equals(Setting.DISCORD_EVENTS_STAFFCHAT_CHANNEL.getAsString())) {
                UUID uuid = discordUserManager.getUuidByDiscordId(user.getId());
                if (uuid == null) {
                    return;
                }

                Player player = cachedPlayers.getPlayer(uuid);
                if (cachedValues.getBoolean(Command.STAFFCHAT_ENABLED)) {
                    String alert = this.getDiscordBot().getNetworkManager().getMessage(player.getLanguage(), "lang_staffchat_message")
                            .replace("%playername%", player.getRealName())
                            .replace("%username%", player.getUserName())
                            .replace("%nickname%", player.getNicknameOrUserName())
                            .replace("%server%", "Discord")
                            .replace("%message%", msg);
                    String perm1 = "networkmanager.staffchat";
                    String perm2 = "networkmanager.admin";
                    if (this.getDiscordBot().getNetworkManager().isRedisBungee()) {
                        PubSubMessage pubSubMessage = new PubSubMessage(PubSubMessageType.STAFFCHAT,
                                this.getDiscordBot().getNetworkManager().getRedisBungee().getServerId(), perm1 + " " + perm2 + " " + alert);
                        this.getDiscordBot().getNetworkManager().sendRedisBungee(GsonUtils.getGson().toJson(pubSubMessage));
                    } else {
                        this.getDiscordBot().getNetworkManager().sendMessageToStaff(alert, "all", perm1, perm2);
                    }
                }
            }
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        final Member member = event.getMember();
        if (event.getGuild().equals(this.getDiscordBot().getGuild()) && !member.getUser().isBot()) {
            this.getDiscordBot().getScheduler().runAsync(new GuildJoinCheckTask(this.getDiscordBot(), member), false);
        }
    }

    private DiscordBot getDiscordBot() {
        return discordBot;
    }
}