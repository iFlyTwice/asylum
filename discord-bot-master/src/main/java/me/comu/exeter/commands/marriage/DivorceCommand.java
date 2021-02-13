package me.comu.exeter.commands.marriage;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class DivorceCommand implements ICommand {
    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        if (!Utility.isMarried(event.getAuthor().getId())) {
            event.getChannel().sendMessage("You can't divorce anyone if you're not married!").queue();
            return;
        }
        List<Member> members = event.getMessage().getMentionedMembers();
        if (members.isEmpty()) {
            event.getChannel().sendMessage("Please specify a person to divorce").queue();
            return;
        }
        if (!Utility.isMarried(members.get(0).getId())) {
            event.getChannel().sendMessage("That user isn't even married to anyone...").queue();
            return;
        }
        String marriedUser = Utility.getMarriedUser(event.getAuthor().getId());
        try {
            if (Utility.marriedUsers.get(event.getAuthor().getId()).equals(marriedUser)) {
                Utility.marriedUsers.remove(event.getAuthor().getId());
            }
        } catch (NullPointerException ex) {
            if (marriedUser.equals(Utility.getKeyByValue(Utility.marriedUsers, event.getAuthor().getId()))) {
                Utility.marriedUsers.remove(marriedUser);
            }
        }
        event.getChannel().sendMessage("How sad, " + event.getAuthor().getAsMention() + " has divorced their beloved " + members.get(0).getAsMention() + ".").queue();
    }

    @Override
    public String getHelp() {
        return "Divorces your current married partner.\n`" + Core.PREFIX + getInvoke() + " [user]`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "divorce";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"breakup", "dump"};
    }

    @Override
    public Category getCategory() {
        return Category.MARRIAGE;
    }
}
