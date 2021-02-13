package me.comu.exeter.commands.marriage;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class MarryCommand implements ICommand {

    private boolean pending = false;
    private final EventWaiter eventWaiter;

    public MarryCommand(EventWaiter waiter) {
        this.eventWaiter = waiter;
    }

    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("Please specify who you want to marry.").queue();
            return;
        }
        if (pending) {
            event.getChannel().sendMessage("Someone already has a pending marriage request to respond to").queue();
            return;
        }
        List<Member> members = event.getMessage().getMentionedMembers();
        if (members.isEmpty()) {
            event.getChannel().sendMessage("Please specify a valid user to marry.").queue();
            return;
        }
        if (members.get(0).getUser().getId().equals(event.getAuthor().getId())) {
            event.getChannel().sendMessage("You cannot marry yourself, no matter how lonely you may be.").queue();
            return;
        }

        if (members.get(0).getUser().isBot()) {
            event.getChannel().sendMessage("You cannot marry a bot, no matter how lonely you may be.").queue();
            return;
        }

        if (Utility.marriedUsers.containsKey(event.getAuthor().getId())) {
            event.getChannel().sendMessage("Bro wtf, you're already married! " + Objects.requireNonNull(event.getJDA().getUserById(Utility.marriedUsers.get(Objects.requireNonNull(event.getMember()).getId()))).getAsMention() + " you seeing this?!").queue();
            return;
        }
        if (Utility.marriedUsers.containsValue(Objects.requireNonNull(event.getMember()).getId())) {
            event.getChannel().sendMessage("Bro wtf, you're already married! " + Objects.requireNonNull(Objects.requireNonNull(event.getJDA().getUserById(Objects.requireNonNull(Utility.getKeyByValue(Utility.marriedUsers, event.getMember().getId()))))).getAsMention() + " you seeing this?!").queue();
            return;
        }
        if (Utility.marriedUsers.containsKey(members.get(0).getId()) || Utility.marriedUsers.containsValue(members.get(0).getId())) {
            event.getChannel().sendMessage("Bro, they're already married, let it go.").queue();
        }
        pending = true;
        event.getChannel().sendMessage(event.getMember().getUser().getName() + " has requested to marry you, do you accept? (Yes/No) " + members.get(0).getUser().getAsMention()).queue();
        eventWaiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> members.get(0).getId().equals(e.getAuthor().getId()) && event.getChannel().equals(e.getChannel()) && (e.getMessage().getContentRaw().equalsIgnoreCase("yes") || e.getMessage().getContentRaw().equalsIgnoreCase("no")),
                e -> {
                    if (e.getMessage().getContentRaw().equalsIgnoreCase("yes")) {
                        event.getChannel().sendMessage(e.getAuthor().getAsMention() + " has accepted " + event.getAuthor().getAsMention() + "'s marriage proposal. **Congratulations**! \uD83E").queue();
                        Utility.marriedUsers.put(event.getAuthor().getId(), e.getAuthor().getId());
                        pending = false;
                    } else if (e.getMessage().getContentRaw().equalsIgnoreCase("no")) {
                        event.getChannel().sendMessage(e.getAuthor().getAsMention() + " just rejected " + event.getAuthor().getAsMention() + "'s marriage proposal LOL. Maybe next time bro.").queue();
                        pending = false;
                    }
                }, 10, TimeUnit.SECONDS, () -> {
                    event.getChannel().sendMessage(members.get(0).getAsMention() + " never replied to " + event.getAuthor().getAsMention() + "'s marriage proposal :( F in the chat boys").queue();
                    pending = false;
                });
    }


    @Override
    public String getHelp() {
        return "Sends a marriage proposal to the specified user\n`" + Core.PREFIX + getInvoke() + " [user]`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "marry";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"propose"};
    }

    @Override
    public Category getCategory() {
        return Category.MARRIAGE;
    }
}
