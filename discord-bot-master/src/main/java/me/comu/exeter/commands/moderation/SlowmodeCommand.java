package me.comu.exeter.commands.moderation;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SlowmodeCommand implements ICommand {

    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        Member member = event.getMember();
        Member selfMember = event.getGuild().getSelfMember();
        int slowtime;

        if (!Objects.requireNonNull(member).hasPermission(Permission.MANAGE_CHANNEL) && member.getIdLong() != Core.OWNERID) {
            channel.sendMessage("You don't have permission to set the slowmode of the channel").queue();
            return;
        }
        if (!selfMember.hasPermission(Permission.MANAGE_CHANNEL) && (!selfMember.hasPermission(Permission.MANAGE_CHANNEL))) {
            channel.sendMessage("I don't have permissions to set the slowmode of the channel").queue();
            return;
        }
        if (args.isEmpty())
        {
            event.getChannel().sendMessage("Please insert an amount to set the slowmode to!").queue();
            return;
        }
        try {
                slowtime = Integer.parseInt(args.get(0));
                if (slowtime < 0)
                {
                    channel.sendMessage("That number is either invalid or too large to change the volume to or is a floating point number (integers only)").queue();
                    return;
                }
        } catch (Exception ex) {
            channel.sendMessage("That number is either invalid or too large to change the volume to or is a floating point number (integers only)").queue();
            return;
        }
        channel.getManager().setSlowmode(slowtime).queue();
        if (slowtime == 0) {
            channel.sendMessage(String.format("Turned off slowmode for `%s`!", channel.getName())).queue();
        } else
        channel.sendMessage(String.format("`%s` has been put on slowmode for `%s` seconds!", channel.getName(), slowtime)).queue();
    }

    @Override
    public String getHelp() {
        return "Sets the slowmode time of a text-channel\n`" + Core.PREFIX + getInvoke() + " [amount]`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "slowmode";
    }

    @Override
    public String[] getAlias() {
        return new String[] {"slow","setslow","setslowmode"};
    }

     @Override
    public Category getCategory() {
        return Category.MODERATION;
    }
}
