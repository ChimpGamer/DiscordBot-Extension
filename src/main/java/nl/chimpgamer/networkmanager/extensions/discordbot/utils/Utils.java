package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import nl.chimpgamer.networkmanager.api.models.player.Player;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;
import nl.chimpgamer.networkmanager.extensions.discordbot.configurations.Setting;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

    public static void copyInputStreamToFile(InputStream in, File file) {
        try {
            Files.copy(in, file.toPath());
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

    public static Message sendMessageComplete(MessageChannel channel, MessageEmbed message) {
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

    public static void editMessage(Message currentMessage, MessageEmbed newMessage) {
        currentMessage.editMessage(newMessage).queue();
    }

    public static void modifyRolesOfMember(Member member, Set<Role> rolesToAdd, Set<Role> rolesToRemove) throws InsufficientPermissionException {
        rolesToAdd = rolesToAdd.stream()
                .filter(role -> !role.isManaged())
                .filter(role -> !role.getGuild().getPublicRole().getId().equals(role.getId()))
                .filter(role -> !member.getRoles().contains(role))
                .collect(Collectors.toSet());
        Set<Role> nonInteractableRolesToAdd = rolesToAdd.stream().filter(role -> !member.getGuild().getSelfMember().canInteract(role)).collect(Collectors.toSet());
        rolesToAdd.removeAll(nonInteractableRolesToAdd);
        nonInteractableRolesToAdd.forEach(role -> DiscordBot.getInstance().getLogger().warning("Failed to add role " + role.getName() + " to " + member.getEffectiveName() + " because the bot's highest role is lower than the target role and thus can't interact with it"));

        rolesToRemove = rolesToRemove.stream()
                .filter(role -> !role.isManaged())
                .filter(role -> !role.getGuild().getPublicRole().getId().equals(role.getId()))
                .filter(role -> member.getRoles().contains(role))
                .collect(Collectors.toSet());
        Set<Role> nonInteractableRolesToRemove = rolesToRemove.stream().filter(role -> !member.getGuild().getSelfMember().canInteract(role)).collect(Collectors.toSet());
        rolesToRemove.removeAll(nonInteractableRolesToRemove);
        nonInteractableRolesToRemove.forEach(role -> DiscordBot.getInstance().getLogger().warning("Failed to remove role " + role.getName() + " from " + member.getEffectiveName() + " because the bot's highest role is lower than the target role and thus can't interact with it"));

        member.getGuild().modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();
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
            member.getGuild().modifyNickname(member, nickName).queue();
        } catch (PermissionException ex) {
            if (ex.getPermission() == Permission.UNKNOWN) {
                DiscordBot.getInstance().getLogger().warning("Could not set the nickname for " + member.getEffectiveName() + " because " + ex.getMessage());
            } else {
                DiscordBot.getInstance().getLogger().warning("Could not set the nickname for " + member.getEffectiveName() + " because the bot does not have the required permission " + ex.getPermission().getName());
            }
        }
    }

    public static void addRoleToMember(@NotNull Member member, @NotNull Role role) {
        try {
            member.getGuild().addRoleToMember(member, role).queue();
        } catch (PermissionException ex) {
            if (ex.getPermission() == Permission.UNKNOWN) {
                DiscordBot.getInstance().getLogger().warning("Could not set the role for " + member.getEffectiveName() + " because " + ex.getMessage());
            } else {
                DiscordBot.getInstance().getLogger().warning("Could not set the role for " + member.getEffectiveName() + " because the bot does not have the required permission " + ex.getPermission().getName());
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

        DiscordBot.getInstance().getLogger().info("Syncing roles for " + member.getEffectiveName());

        Set<Role> addRoles = new HashSet<>();
        Set<Role> removeRoles = new HashSet<>();
        for (String roleName : Setting.DISCORD_SYNC_RANKS_LIST.getAsList()) {
            Role role = getRoleByName(roleName);

            if (role == null) {
                continue;
            }

            List<String> groups = nl.chimpgamer.networkmanager.bungeecord.utils.Utils.getGroupsName(player);
            for (String group : groups) {
                if (group.equalsIgnoreCase(role.getName())) {
                    addRoles.add(role);
                } else {
                    if (groups.stream().noneMatch(groupName -> groupName.equalsIgnoreCase(role.getName()))) {
                        removeRoles.add(role);
                    }
                }
            }
        }

        // remove roles that the user already has from roles to add
        addRoles.removeAll(member.getRoles());
        // remove roles that the user doesn't already have from roles to remove
        removeRoles.removeIf(role -> !member.getRoles().contains(role));

        System.out.println("AddRoles: " + addRoles);
        System.out.println("RemoveRoles: " + removeRoles);

        if (addRoles.isEmpty() && removeRoles.isEmpty()) {
            return;
        }

        try {
            modifyRolesOfMember(member, addRoles, removeRoles);
            //DiscordBot.getInstance().getGuild().modifyMemberRoles(member, addRoles.isEmpty() ? null : addRoles, removeRoles.isEmpty() ? null : removeRoles).queue();
        } catch (PermissionException ex) {
            if (ex.getPermission() == Permission.UNKNOWN) {
                DiscordBot.getInstance().getLogger().warning("Could not set the role for " + member.getEffectiveName() + " because " + ex.getMessage());
            } else {
                DiscordBot.getInstance().getLogger().warning("Could not set the role for " + member.getEffectiveName() + " because the bot does not have the required permission " + ex.getPermission().getName());
            }
        }
    }

    public static Role getRoleByName(String roleName) {
        List<Role> roles = DiscordBot.getInstance().getGuild().getRolesByName(roleName, true);
        if (roles.isEmpty()) {
            return null;
        }
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        return null;
    }
}