package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;

import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.api.models.player.Player;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class Utils {

    public static void copyInputStreamToFile(InputStream in, File file) {
        try (OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[1024];

            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void sendChannelMessage(MessageChannel channel, String message) {
        try {
            channel.sendMessage(message).queue();
        } catch (PermissionException ex) {
            DiscordBot.getInstance().getLogger().warning("Could not send message to the " + channel.getName() + " because " + ex.getMessage());
        }
    }

    public static void sendChannelMessage(MessageChannel channel, MessageEmbed message) {
        try {
            channel.sendMessage(message).queue();
        } catch (PermissionException ex) {
            DiscordBot.getInstance().getLogger().warning("Could not send message to the " + channel.getName() + " because " + ex.getMessage());
        }
    }

    public static Message sendMessageComplete(MessageChannel channel, String message) {
        try {
            return channel.sendMessage(message).complete();
        } catch (PermissionException ex) {
            DiscordBot.getInstance().getLogger().warning("Could not send message to the " + channel.getName() + " because " + ex.getMessage());
        }
        return null;
    }

    public static void editMessage(Message currentMessage, String newMessage) {
        currentMessage.editMessage(newMessage).queue();
    }

    public static String firstUpperCase(String var0) {
        return var0.substring(0, 1).toUpperCase() + var0.substring(1);
    }

    public static String generateToken() {
        return new BigInteger(64, new SecureRandom()).toString(32);
    }

    public static void setNickName(Member member, String nickName) {
        if (member == null) {
            DiscordBot.getInstance().getLogger().info("Can't set the nickname of a null member");
            return;
        }
        DiscordBot.getInstance().getLogger().info("Setting nickname for " + member.getEffectiveName());

        try {
            member.getGuild().getController().setNickname(member, nickName).queue();
        } catch (PermissionException ex) {
            if (ex.getPermission() == Permission.UNKNOWN) {
                DiscordBot.getInstance().getLogger().warning("Could not set the nickname for " + member.getEffectiveName() + " because " + ex.getMessage());
            } else {
                DiscordBot.getInstance().getLogger().warning("Could not set the nickname for " + member.getEffectiveName() + " because the bot does not have the required permission " + ex.getPermission().getName());
            }
        }
    }

    public static void syncRanks(Player player) {
        if (player == null) {
            return;
        }

        String discordId = DiscordBot.getInstance().getDiscordUserManager().getDiscordIdByUuid(player.getUuid());
        if (discordId == null) {
            return;
        }

        Member member = DiscordBot.getInstance().getGuild().getMemberById(discordId);
        if (member == null) {
            return;
        }

        Set<Role> addRoles = new HashSet<>();
        Set<Role> removeRoles = new HashSet<>();
        for (String roleName : DiscordBot.getInstance().getConfigManager().getSyncRanks()) {
            Role role = getRoleByName(roleName);

            if (role == null) {
                continue;
            }

            List<String> groups = nl.chimpgamer.networkmanager.bungeecord.utils.Utils.getGroupsName(player);
            for (String group : groups) {
                if (group.equalsIgnoreCase(role.getName())) {
                    addRoles.add(role);
                } else {
                    if (member.getRoles().contains(role) && groups.stream().noneMatch(groupName -> groupName.equalsIgnoreCase(role.getName()))) {
                        removeRoles.add(role);
                    }
                }
            }

            // remove roles that the user already has from roles to add
            addRoles.removeAll(member.getRoles());
            // remove roles that the user doesn't already have from roles to remove
            removeRoles.removeIf(role1 ->  !member.getRoles().contains(role));

            DiscordBot.getInstance().getGuild().getController().removeRolesFromMember(member, removeRoles).queue();
            DiscordBot.getInstance().getGuild().getController().addRolesToMember(member, addRoles).queue();
        }
    }

    public static Role getRoleByName(String roleName) {
        List<Role> roles = DiscordBot.getInstance().getGuild().getRolesByName(roleName, false);
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        for (Role role : roles) {
            if (role.getName().equals(roleName)) {
                return role;
            }
        }
        return null;
    }
}