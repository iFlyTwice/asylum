package me.comu.exeter.commands.bot;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;

public class CreateTagCommand implements ICommand {

    static final HashMap<String, String> tags = new HashMap<>();

    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("Please insert a valid tag and message").queue();
            return;
        }
        if (args.size() == 1) {
            event.getChannel().sendMessage("Please insert a message to go alongside the tag").queue();
            return;
        }
        if (!event.getMessage().getMentionedMembers().isEmpty()) {
            event.getChannel().sendMessage("You can't ping people in tags, sorry.").queue();
            return;
        }
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR) && event.getMessage().getContentRaw().contains(".gg/")) {
            event.getChannel().sendMessage("Only admins can create commands with discord invites").queue();
            return;
        }
        StringJoiner stringJoiner = new StringJoiner(" ");
        args.stream().skip(1).forEach(stringJoiner::add);
        String tag = args.get(0);
        if (tags.containsKey(tag)) {
            event.getChannel().sendMessage("`" + Utility.removeMentions(args.get(0)) + "` already exists as a tag!").queue();
            return;
        }
        String tagMessage = Utility.removeMentions(stringJoiner.toString());
        tags.put(tag, tagMessage);
        event.getChannel().sendMessage("Successfully added `" + tag + "` with the content of `" + tagMessage + "`").queue();

    }

    @Override
    public String getHelp() {
        return "Assigns a custom message to a tag\n`" + Core.PREFIX + getInvoke() + " [tag] <content>`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "createtag";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"tagcreate"};
    }

    @Override
    public Category getCategory() {
        return Category.BOT;
    }
}
