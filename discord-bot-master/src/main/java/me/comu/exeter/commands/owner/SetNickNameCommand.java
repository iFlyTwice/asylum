package me.comu.exeter.commands.owner;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class SetNickNameCommand implements ICommand {
    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        if (!(event.getAuthor().getIdLong() == Core.OWNERID) && !event.getAuthor().getId().equalsIgnoreCase("725452437342912542")) {
            return;
        }
        if (args.isEmpty())
        {
            event.getChannel().sendMessage("Please specify a nickname").queue();
            return;
        }
            String message = event.getMessage().getContentRaw().substring(Core.PREFIX.length() + 10).trim();
            if (message.equals("resetnick"))
            {
                event.getChannel().sendMessage("Resetting nicknames").queue();
                for (Member m : event.getGuild().getMembers()) {
                    if (event.getGuild().getSelfMember().canInteract(m)) {
                        m.modifyNickname(m.getUser().getName()).queue();
                    }
                }
                return;
            }
        for (Member m : event.getGuild().getMembers()) {
            if (event.getGuild().getSelfMember().canInteract(m))
            {
                m.modifyNickname(message).queue();
            }
        }

    }

    @Override
    public String getHelp() {
        return "Sets everyone's nickname to the specified nickname.\n`" + Core.PREFIX + getInvoke() + " [nickname]`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "changenick";
    }

    @Override
    public String[] getAlias() {
        return new String[] {};
    }

    @Override
    public Category getCategory() {
        return Category.OWNER;
    }
}
