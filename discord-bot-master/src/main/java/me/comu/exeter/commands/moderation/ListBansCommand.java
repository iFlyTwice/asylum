package me.comu.exeter.commands.moderation;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import me.comu.exeter.pagination.method.Pages;
import me.comu.exeter.pagination.model.Page;
import me.comu.exeter.pagination.type.PageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListBansCommand implements ICommand {

    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        Member member = event.getMember();
        Member selfMember = event.getGuild().getSelfMember();

        if (!Objects.requireNonNull(member).hasPermission(Permission.BAN_MEMBERS) && Objects.requireNonNull(event.getMember()).getIdLong() != Core.OWNERID) {
            channel.sendMessage("You don't have permission to list bans").queue();
            return;
        }

        if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
            channel.sendMessage("I don't have permissions to list the bans").queue();
            return;
        }

        event.getGuild().retrieveBanList().queue((entries) -> {
            if (entries.isEmpty()) {
                channel.sendMessage("There are no users currently banned!").queue();
                return;
            }
            StringBuilder buffer = new StringBuilder();
            for (Guild.Ban entry : entries) {
                buffer.append(" + ").append(entry.getUser().getName()).append("#").append(entry.getUser().getDiscriminator()).append(" | ").append(entry.getReason()).append("\n");
            }
            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(event.getGuild().getName() + " Banlist (" + entries.size() + ")\n").setColor(Color.RED).setFooter("Requested by " + event.getAuthor().getAsTag(), event.getAuthor().getEffectiveAvatarUrl()).setTimestamp(Instant.now());
            ArrayList<Page> pages = new ArrayList<>();
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setContent(buffer.toString());
            Queue<Message> messages = messageBuilder.buildAll(MessageBuilder.SplitPolicy.ANYWHERE);
            for (Message message : messages) {
                embedBuilder.setDescription(message.getContentRaw());
                pages.add(new Page(PageType.EMBED, embedBuilder.build()));
            }
            event.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> Pages.paginate(success, pages, false, 60, TimeUnit.SECONDS));
        });
    }

    @Override
    public String getHelp() {
        return "Lists all banned users\n`" + Core.PREFIX + getInvoke() + "`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "listbans";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"banlist", "bans"};
    }

    @Override
    public Category getCategory() {
        return Category.MODERATION;
    }
}
