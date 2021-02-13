package me.comu.exeter.commands.moderation;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;

public class FilterCommand implements ICommand {

    public static final HashMap<String, String> filteredUsers = new HashMap<>();
    public static final HashMap<String, String> filteredRoles = new HashMap<>();
    private static boolean active = true;

    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage(getHelp()).queue();
            return;
        }
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("You don't have permission to toggle the filter").queue();
            return;
        }
        if (args.get(0).equalsIgnoreCase("listroles")) {
            if (filteredRoles.isEmpty()) {
                event.getChannel().sendMessage("No roles are whitelisted to the filter.").queue();
                return;
            }
            event.getChannel().sendMessage(filteredRoles.keySet().toString()).queue();
            return;
        }
        if (args.get(0).equalsIgnoreCase("listusers")) {
            if (filteredUsers.isEmpty()) {
                event.getChannel().sendMessage("Nobody is whitelisted to the filter.").queue();
                return;
            }
            event.getChannel().sendMessage(filteredUsers.keySet().toString()).queue();
            return;
        }
        if (args.get(0).equalsIgnoreCase("clearusers") || args.get(0).equalsIgnoreCase("clearuser")) {
            event.getChannel().sendMessage("Successfully cleared **"  + filteredUsers.size() + "** users.").queue();
            filteredUsers.clear();
            return;
        }
        if (args.get(0).equalsIgnoreCase("clearroles") || args.get(0).equalsIgnoreCase("clearrole") || args.get(0).equalsIgnoreCase("roleclear") || args.get(0).equalsIgnoreCase("rolesclear")) {
            event.getChannel().sendMessage("Successfully cleared **"  + filteredRoles.size() + "** roles.").queue();
            filteredRoles.clear();
            return;
        }
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
        if (args.get(0).equalsIgnoreCase("add") || args.get(0).equalsIgnoreCase("adduser") || args.get(0).equalsIgnoreCase("user") || args.get(0).equalsIgnoreCase("whitelist") || args.get(0).equalsIgnoreCase("wl")) {
            if (mentionedMembers.isEmpty()) {
                event.getChannel().sendMessage("Please specify who you want to whitelist from the filter").queue();
                return;
            }
            if (filteredUsers.containsKey(mentionedMembers.get(0).getId())) {
                event.getChannel().sendMessage(mentionedMembers.get(0).getAsMention() + " is already filter-whitelisted!").queue();
                return;
            }
            filteredUsers.put(mentionedMembers.get(0).getId(), event.getGuild().getId());
            event.getChannel().sendMessage(String.format("Added `%#s` to the filter whitelist", mentionedMembers.get(0).getUser())).queue();

        } else if (args.get(0).equalsIgnoreCase("remove") || args.get(0).equalsIgnoreCase("rem") || args.get(0).equalsIgnoreCase("unwhitelist") || args.get(0).equalsIgnoreCase("uwl") || args.get(0).equalsIgnoreCase("removeuser") || args.get(0).equalsIgnoreCase("deluser")) {
            if (event.getMessage().getMentionedMembers().isEmpty()) {
                event.getChannel().sendMessage("Please specify who you want to remove from the filter whitelist").queue();
                return;
            }
            if (filteredUsers.containsKey(mentionedMembers.get(0).getId())) {
                filteredUsers.remove(mentionedMembers.get(0).getId());
                event.getChannel().sendMessage(String.format("Removed `%#s` from the filter whitelist", mentionedMembers.get(0).getUser())).queue();

            }

        } else if (args.get(0).equalsIgnoreCase("role") || args.get(0).equalsIgnoreCase("addrole") || args.get(0).equalsIgnoreCase("whitelistrole") || args.get(0).equalsIgnoreCase("wlrole")) {
            Role role;
            try {
                if (args.size() == 1) {
                    event.getChannel().sendMessage("Please specify a role").queue();
                    return;
                }
                role = event.getGuild().getRoleById(Long.parseLong(args.get(1)));
                if (filteredRoles.containsKey(Objects.requireNonNull(role).getId())) {
                    event.getChannel().sendMessage("`" + role.getName() + "` is already filter-whitelisted.").queue();
                    return;
                }
                filteredRoles.put(Objects.requireNonNull(role).getId(), event.getGuild().getId());
                event.getChannel().sendMessage("All users with the `" + role.getName() + "` role will now be excluded from the filter.").queue();
            } catch (NullPointerException | NumberFormatException ex) {
                StringJoiner stringJoiner = new StringJoiner(" ");
                args.stream().skip(1).forEach(stringJoiner::add);
                List<Role> roles = event.getGuild().getRolesByName(stringJoiner.toString().toLowerCase().replaceFirst("addrole", ""), false);
                if (roles.isEmpty()) {
                    event.getChannel().sendMessage("Couldn't find role `" + Utility.removeMentions(args.get(1)) + "`. Maybe try using the role ID instead.").queue();
                    return;
                }
                if (roles.size() > 1) {
                    event.getChannel().sendMessage("Multiple roles found for `" + Utility.removeMentions(args.get(1)) + "`. Use the role ID instead.").queue();
                    return;
                }
                role = roles.get(0);
                if (FilterCommand.filteredRoles.containsKey(role.getId())) {
                    event.getChannel().sendMessage("`" + role.getName() + "` is already filter-whitelisted.").queue();
                    return;
                }
                filteredRoles.put(role.getId(), event.getGuild().getId());
                event.getChannel().sendMessage("All users with the `" + role.getName() + "` role will now be excluded from the filter.").queue();
            }
        } else if (args.get(0).equalsIgnoreCase("removerole") || args.get(0).equalsIgnoreCase("remrole") || args.get(0).equalsIgnoreCase("unwhitelistrole") || args.get(0).equalsIgnoreCase("uwlrole") || args.get(0).equalsIgnoreCase("delrole")) {
            Role role;
            try {
                if (args.size() == 1) {
                    event.getChannel().sendMessage("Please specify a role").queue();
                    return;
                }
                role = event.getGuild().getRoleById(Long.parseLong(args.get(1)));
                if (filteredRoles.containsKey(Objects.requireNonNull(role).getId())) {
                    filteredRoles.remove(role.getId());
                    event.getChannel().sendMessage("Removed the `" + role.getName() + "` role from the filter-whitelist.").queue();
                } else {
                    event.getChannel().sendMessage("`" + role.getName() + "` is not filter-whitelisted").queue();
                }
            } catch (NullPointerException | NumberFormatException ex) {
                StringJoiner stringJoiner = new StringJoiner(" ");
                args.stream().skip(1).forEach(stringJoiner::add);
                List<Role> roles = event.getGuild().getRolesByName(stringJoiner.toString().toLowerCase().replaceFirst("addrole", ""), false);
                if (roles.isEmpty()) {
                    event.getChannel().sendMessage("Couldn't find role `" + Utility.removeMentions(args.get(1)) + "`. Maybe try using the role ID instead.").queue();
                    return;
                }
                if (roles.size() > 1) {
                    event.getChannel().sendMessage("Multiple roles found for `" + args.get(1) + "`. Use the role ID instead.").queue();
                    return;
                }
                role = roles.get(0);
                if (filteredRoles.containsKey(role.getId())) {
                    filteredRoles.remove(role.getId());
                    event.getChannel().sendMessage("Removed the `" + role.getName() + "` role from the filter-whitelist.").queue();
                } else {
                    event.getChannel().sendMessage("`" + role.getName() + "` is not filter-whitelisted").queue();
                }
            }
        } else if (args.get(0).equalsIgnoreCase("true") || args.get(0).equalsIgnoreCase("on")) {
            if (!active) {
                active = true;
                event.getChannel().sendMessage("Filter is now active").queue();
            } else
                event.getChannel().sendMessage("Filter is already enabled").queue();
        } else if (args.get(0).equalsIgnoreCase("false") || args.get(0).equalsIgnoreCase("off")) {
            if (active) {
                active = false;
                event.getChannel().sendMessage("Filter is no longer active").queue();
            } else
                event.getChannel().sendMessage("Filter is already disabled").queue();
        }
    }

    public static boolean isActive() {
        return active;
    }

    @Override
    public String getHelp() {
        return "Toggles a text-channel filter\n`" + Core.PREFIX + getInvoke() + " [on/off] : [add/remove] <user> : [addrole/removerole] <role>`\nAliases: `" + Arrays.deepToString(getAlias()) + "`\n" + String.format("Currently `%s`.", active ? "enabled" : "disabled");
    }

    @Override
    public String getInvoke() {
        return "filter";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"togglefilter"};
    }

    @Override
    public Category getCategory() {
        return Category.MODERATION;
    }
}
