package me.comu.exeter.commands.moderation;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OnCommand implements ICommand {


    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE) && event.getMember().getIdLong() != Core.OWNERID) {
            event.getChannel().sendMessage("You don't have permission to turn someone on").queue();
            return;
        }

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getChannel().sendMessage("I don't have permissions to turn someone on").queue();
            return;
        }

        if (args.isEmpty()) {
            event.getChannel().sendMessage("Please specify a user to turn on").queue();
            return;
        }
        if (OffCommand.offedUsers.isEmpty()) {
            event.getChannel().sendMessage("No user is currently turned off.").queue();
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
        if (!args.isEmpty() && mentionedMembers.isEmpty()) {
            List<Member> targets = event.getGuild().getMembersByName(args.get(0), true);
            if (targets.isEmpty()) {
                event.getChannel().sendMessage("Couldn't find the user " + Utility.removeMentions(args.get(0))).queue();
                return;
            } else if (targets.size() > 1) {
                event.getChannel().sendMessage("Multiple users found! Try mentioning the user instead.").queue();
                return;
            }
            if (OffCommand.offedUsers.contains(targets.get(0).getId())) {
                OffCommand.offedUsers.remove(targets.get(0).getId());
                event.getChannel().sendMessage("Ok, Turned on **" + targets.get(0).getAsMention() + "**.").queue();
            }
            else
                event.getChannel().sendMessage(targets.get(0).getAsMention() + " is not turned off.").queue();
        } else if (!args.isEmpty()) {
            if (OffCommand.offedUsers.contains(mentionedMembers.get(0).getId())) {
                OffCommand.offedUsers.remove(mentionedMembers.get(0).getId());
                event.getChannel().sendMessage("Ok, Turned on **" + mentionedMembers.get(0).getAsMention() + "**.").queue();
            }
            else
                event.getChannel().sendMessage(mentionedMembers.get(0).getAsMention() + " is not turned off.").queue();
        }
    }

    @Override
    public String getHelp() {
        return "Turns an off'd user back on\n`" + Core.PREFIX + getInvoke() + " [user]`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "on";
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    @Override
    public Category getCategory() {
        return Category.MODERATION;
    }
}
