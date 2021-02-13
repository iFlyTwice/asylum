package me.comu.exeter.commands.misc;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UserInfoCommand implements ICommand {

    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a");
        List<Member> memberList = event.getMessage().getMentionedMembers();

        if (event.getMessage().getMentionedMembers().isEmpty() && args.isEmpty()) {
            try {
                MessageEmbed embed = new EmbedBuilder().setColor(Objects.requireNonNull(event.getMember()).getColor())
                        .setThumbnail(event.getAuthor().getEffectiveAvatarUrl())
                        .addField("Username", event.getMember().getUser().getAsTag(), true)
                        .addField("Online Status", event.getMember().getOnlineStatus().name().replaceAll("_", " "), true)
                        .addField("Bot Account", event.getMember().getUser().isBot() ? "Yes" : "No", true)
                        .addField("Account Created", event.getMember().getUser().getTimeCreated().format(timeFormatter), true)
                        .addField("Joined Server", event.getMember().getTimeJoined().format(timeFormatter), true)
                        .addField("Activity", displayGameInfo(event.getMember()), true)
                        .addField(String.format("Roles: (%s)", event.getMember().getRoles().size()), getRolesAsString(event.getMember().getRoles()), true)
                        .addField("Administrator", event.getMember().hasPermission(Permission.ADMINISTRATOR) ? "Yes" : "No", true)
                        .addField("Account ID", event.getMember().getId(), true)
                        .build();
                event.getChannel().sendMessage(embed).queue();
            } catch (IllegalArgumentException ex) {
                MessageEmbed embedEx = new EmbedBuilder().setColor(Objects.requireNonNull(event.getMember()).getColor())
                        .setThumbnail(event.getAuthor().getEffectiveAvatarUrl())
                        .addField("Username", event.getMember().getUser().getAsTag(), true)
                        .addField("Online Status", event.getMember().getOnlineStatus().name().replaceAll("_", " "), true)
                        .addField("Bot Account", event.getMember().getUser().isBot() ? "Yes" : "No", true)
                        .addField("Account Created", event.getMember().getUser().getTimeCreated().format(timeFormatter), true)
                        .addField("Joined Server", event.getMember().getTimeJoined().format(timeFormatter), true)
                        .addField("Activity:", displayGameInfo(event.getMember()), true)
                        .addField(String.format("Roles: (%s)", event.getMember().getRoles().size()), "Too many to display", true)
                        .addField("Administrator", event.getMember().hasPermission(Permission.ADMINISTRATOR) ? "Yes" : "No", true)
                        .addField("Account ID", event.getMember().getId(), true)
                        .build();
                event.getChannel().sendMessage(embedEx).queue();
            }
        }
        if (!args.isEmpty() && !memberList.isEmpty()) {
            try {
                MessageEmbed embed = new EmbedBuilder().setColor(memberList.get(0).getColor())
                        .setThumbnail(memberList.get(0).getUser().getEffectiveAvatarUrl())
                        .addField("Username", memberList.get(0).getUser().getAsTag(), true)
                        .addField("Online Status", memberList.get(0).getOnlineStatus().name().replaceAll("_", " "), true)
                        .addField("Bot Account", memberList.get(0).getUser().isBot() ? "Yes" : "No", true)
                        .addField("Account Created", memberList.get(0).getUser().getTimeCreated().format(timeFormatter), true)
                        .addField("Joined Server", memberList.get(0).getTimeJoined().format(timeFormatter), true)
                        .addField("Activity:", displayGameInfo(memberList.get(0)), true)
                        .addField(String.format("Roles: (%s)", memberList.get(0).getRoles().size()), getRolesAsString(memberList.get(0).getRoles()), true)
                        .addField("Administrator",memberList.get(0).hasPermission(Permission.ADMINISTRATOR) ? "Yes" : "No", true)
                        .addField("Account ID", memberList.get(0).getId(), true)
                        .build();
                event.getChannel().sendMessage(embed).queue();
            } catch (IllegalArgumentException ex) {
                MessageEmbed embedEx = new EmbedBuilder().setColor(memberList.get(0).getColor())
                        .setThumbnail(memberList.get(0).getUser().getEffectiveAvatarUrl())
                        .setThumbnail(memberList.get(0).getUser().getEffectiveAvatarUrl())
                        .addField("Username", memberList.get(0).getUser().getAsTag(), true)
                        .addField("Online Status", memberList.get(0).getOnlineStatus().name().replaceAll("_", " "), true)
                        .addField("Bot Account", memberList.get(0).getUser().isBot() ? "Yes" : "No", true)
                        .addField("Account Created", memberList.get(0).getUser().getTimeCreated().format(timeFormatter), true)
                        .addField("Joined Server", memberList.get(0).getTimeJoined().format(timeFormatter), true)
                        .addField("Activity:", displayGameInfo(memberList.get(0)), true)
                        .addField(String.format("Roles: (%s)", memberList.get(0).getRoles().size()), "Too many to display", true)
                        .addField("Administrator",memberList.get(0).hasPermission(Permission.ADMINISTRATOR) ? "Yes" : "No", true)
                        .addField("Account ID", memberList.get(0).getId(), true)
                        .build();
                event.getChannel().sendMessage(embedEx).queue();
            }
        }
        if (!args.isEmpty() && memberList.isEmpty()) {
            List<Member> targets = event.getGuild().getMembersByName(args.get(0), true);
            if (targets.isEmpty()) {
                event.getChannel().sendMessage("Couldn't find the user " + Utility.removeMentions(args.get(0))).queue();
                return;
            } else if (targets.size() > 1) {
                event.getChannel().sendMessage("Multiple users found! Try mentioning the user instead.").queue();
                return;
            }
            try {
                MessageEmbed embed = new EmbedBuilder().setColor(targets.get(0).getColor())
                        .setThumbnail(targets.get(0).getUser().getEffectiveAvatarUrl())
                        .addField("Username", targets.get(0).getUser().getAsTag(), true)
                        .addField("Bot Account", targets.get(0).getUser().isBot() ? "Yes" : "No", true)
                        .addField("Online Status", targets.get(0).getOnlineStatus().name().replaceAll("_", " "), true)
                        .addField("Account Created", targets.get(0).getUser().getTimeCreated().format(timeFormatter), true)
                        .addField("Joined Server", targets.get(0).getTimeJoined().format(timeFormatter), true)
                        .addField("Activity:", displayGameInfo(targets.get(0)), true)
                        .addField(String.format("Roles: (%s)", targets.get(0).getRoles().size()), getRolesAsString(targets.get(0).getRoles()), true)
                        .addField("Administrator",targets.get(0).hasPermission(Permission.ADMINISTRATOR) ? "Yes" : "No", true)
                        .addField("Account ID", targets.get(0).getId(), true)
                        .build();
                event.getChannel().sendMessage(embed).queue();
            } catch (IllegalArgumentException ex) {
                MessageEmbed embedEx = new EmbedBuilder().setColor(targets.get(0).getColor())
                        .setThumbnail(targets.get(0).getUser().getEffectiveAvatarUrl())
                        .addField("Username", targets.get(0).getUser().getAsTag(), true)
                        .addField("Online Status", targets.get(0).getOnlineStatus().name().replaceAll("_", " "), true)
                        .addField("Bot Account", targets.get(0).getUser().isBot() ? "Yes" : "No", true)
                        .addField("Account Created", targets.get(0).getUser().getTimeCreated().format(timeFormatter), true)
                        .addField("Joined Server", targets.get(0).getTimeJoined().format(timeFormatter), true)
                        .addField("Activity:", displayGameInfo(targets.get(0)), true)
                        .addField(String.format("Roles: (%s)", targets.get(0).getRoles().size()), "Too many to display", true)
                        .addField("Administrator",targets.get(0).hasPermission(Permission.ADMINISTRATOR) ? "Yes" : "No", true)
                        .addField("Account ID", targets.get(0).getId(), true)
                        .build();
                event.getChannel().sendMessage(embedEx).queue();
            }

        }

    }

    private String displayGameInfo(Member name) {
        try {
            String game = name.getActivities().get(0).getName();
            return "Playing: " + game;
        } catch (NullPointerException | IndexOutOfBoundsException exx) {
            return "None";
        }
    }

    private String getRolesAsString(List<Role> rolesList) {
        StringBuilder roles;
        if (!rolesList.isEmpty()) {
            Role tempRole = rolesList.get(0);
            roles = new StringBuilder(tempRole.getName());
            for (int i = 1; i < rolesList.size(); i++) {
                tempRole =  rolesList.get(i);
                roles.append(", ").append(tempRole.getName());
            }
        } else {
            roles = new StringBuilder("No Roles");
        }
        return roles.toString();
    }

    @Override
    public String getHelp() {
        return "Gets information about a user\n" + '`' + Core.PREFIX + getInvoke() + " [user]`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "info";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"userinfo", "whois", "ui"};
    }

    @Override
    public Category getCategory() {
        return Category.MISC;
    }
}
