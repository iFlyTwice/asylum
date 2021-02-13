package me.comu.exeter.commands.moderation;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MuteCommand implements ICommand {

    public static List<String> mutedUsers = new ArrayList<>();

    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        Member member = event.getMember();
        Member selfMember = event.getGuild().getSelfMember();
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
        if (!Objects.requireNonNull(member).hasPermission(Permission.MANAGE_ROLES) && member.getIdLong() != Core.OWNERID) {
            channel.sendMessage("You don't have permission to mute someone").queue();
            return;
        }

        if (!selfMember.hasPermission(Permission.MANAGE_ROLES)) {
            channel.sendMessage("I don't have permissions to mute that user").queue();
            return;
        }
        if (!SetMuteRoleCommand.isMuteRoleSet(event.getGuild())) {
            channel.sendMessage("Please set a mute-role first `" + Core.PREFIX + "help muterole`").queue();
            return;
        }

        if (args.isEmpty()) {
            channel.sendMessage("Please specify a user to mute").queue();
            return;
        }
        if (mentionedMembers.isEmpty()) {
            try {
                List<Member> targets = event.getGuild().getMembersByName(args.get(0), true);
                if (targets.isEmpty()) {
                    event.getChannel().sendMessage("Couldn't find the user " + Utility.removeMentions(args.get(0))).queue();
                    return;
                } else if (targets.size() > 1) {
                    event.getChannel().sendMessage("Multiple users found! Try mentioning the user instead.").queue();
                    return;
                }
                Member target = targets.get(0);
                String reason = String.join(" ", args.subList(1, args.size()));
                if (target.getRoles().contains(event.getGuild().getRoleById(SetMuteRoleCommand.getMutedRoleMap().get(event.getGuild().getId())))) {
                    channel.sendMessage("They're already muted bro, let it go").queue();
                    return;
                }
                if (reason.equals("")) {
                    event.getGuild().addRoleToMember(target, Objects.requireNonNull(event.getGuild().getRoleById(SetMuteRoleCommand.getMutedRoleMap().get(event.getGuild().getId())))).reason(String.format("Muted by %#s", event.getAuthor())).queue();
                    channel.sendMessage(String.format("Muted %s", target.getAsMention())).queue();
                    mutedUsers.add(target.getId());
                } else {
                    event.getGuild().addRoleToMember(target, Objects.requireNonNull(event.getGuild().getRoleById(SetMuteRoleCommand.getMutedRoleMap().get(event.getGuild().getId())))).reason(String.format("Muted by %#s for %s", event.getAuthor(), reason)).queue();
                    channel.sendMessage(String.format("Muted %s for `%s`", target.getAsMention(), reason)).queue();
                    mutedUsers.add(target.getId());
                }
            } catch (HierarchyException ex) {
                channel.sendMessage("I cannot mute anyone whilst the mute role is at a higher precedent than my own").queue();
            }
            return;
        }
        Member target = mentionedMembers.get(0);
        String reason = String.join(" ", args.subList(1, args.size()));

        if (target.getRoles().contains(event.getGuild().getRoleById(SetMuteRoleCommand.getMutedRoleMap().get(event.getGuild().getId())))) {
            channel.sendMessage("They're already muted bro, let it go").queue();
            return;
        }
        try {
            if (reason.equals("")) {
                event.getGuild().addRoleToMember(target, Objects.requireNonNull(event.getGuild().getRoleById(SetMuteRoleCommand.getMutedRoleMap().get(event.getGuild().getId())))).reason(String.format("Muted by %#s", event.getAuthor())).queue();
                channel.sendMessage(String.format("Muted %s", target.getAsMention())).queue();
                mutedUsers.add(target.getId());
            } else {
                event.getGuild().addRoleToMember(target, Objects.requireNonNull(event.getGuild().getRoleById(SetMuteRoleCommand.getMutedRoleMap().get(event.getGuild().getId())))).reason(String.format("Muted by %#s for %s", event.getAuthor(), reason)).queue();
                channel.sendMessage(String.format("Muted %s for `%s`", target.getAsMention(), reason)).queue();
                mutedUsers.add(target.getId());
            }
        } catch (HierarchyException ex) {
            channel.sendMessage("I cannot mute anyone whilst the mute role is at a higher precedent than my own").queue();
        } catch (IllegalArgumentException exx) {
            channel.sendMessage("Provided Role is not part of this Guild").queue();
        }


    }

    @Override
    public String getHelp() {
        return "Mutes the specified user from interacting with voice/text channels\n`" + Core.PREFIX + getInvoke() + " [reason]`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "mute";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"silence", "quiet", "stfu", "shhh"};
    }

    @Override
    public Category getCategory() {
        return Category.MODERATION;
    }

}
