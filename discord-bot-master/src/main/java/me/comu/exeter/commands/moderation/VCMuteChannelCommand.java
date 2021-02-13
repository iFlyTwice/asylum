package me.comu.exeter.commands.moderation;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VCMuteChannelCommand implements ICommand {
    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR) && event.getMember().getIdLong() != Core.OWNERID) {
            event.getChannel().sendMessage("You don't have permission to mute a user from VC").queue();
            return;
        }

        if (!event.getGuild().getSelfMember().hasPermission(Permission.VOICE_MUTE_OTHERS)) {
            event.getChannel().sendMessage("I don't have permissions to mute that user").queue();
            return;
        }

        if (args.isEmpty()) {
            event.getChannel().sendMessage("Please specify a VC to mass-mute").queue();
            return;
        }
        if (args.get(0).equalsIgnoreCase(getInvoke()) || args.get(0).equalsIgnoreCase("massvcmute") || args.get(0).equalsIgnoreCase("vcmassmute") || args.get(0).equalsIgnoreCase("mvc")) {
            try {
                List<VoiceChannel> voiceChannels = event.getGuild().getVoiceChannelsByName(args.get(0), true);
                List<Member> members = voiceChannels.get(0).getMembers();
                members.forEach(m -> m.mute(true).queue());
                event.getChannel().sendMessage("Mass server-muted everyone in `" + voiceChannels.get(0).getName() + "`.").queue();
            } catch (Exception ex) {
                VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(args.get(0));
                List<Member> members = Objects.requireNonNull(voiceChannel).getMembers();
                members.forEach(m -> m.mute(true).queue());
                event.getChannel().sendMessage("Mass server-muted everyone in `" + voiceChannel.getName() + "`.").queue();
            }

    } else if (args.get(0).equalsIgnoreCase("unmutevc") || args.get(0).equalsIgnoreCase("umvc"))
        {
            try {
                List<VoiceChannel> voiceChannels = event.getGuild().getVoiceChannelsByName(args.get(0), true);
                List<Member> members = voiceChannels.get(0).getMembers();
                members.forEach(m -> m.mute(false).queue());
                event.getChannel().sendMessage("Mass unserver-muted everyone in `" + voiceChannels.get(0).getName() + "`.").queue();
            } catch (Exception ex) {
                VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(args.get(0));
                List<Member> members = Objects.requireNonNull(voiceChannel).getMembers();
                members.forEach(m -> m.mute(false).queue());
                event.getChannel().sendMessage("Mass unserver-muted everyone in `" + voiceChannel.getName() + "`.").queue();
            }
        }

}

    @Override
    public String getHelp() {
        return "Server mutes everyone in the specified VC\n`" + Core.PREFIX + getInvoke() + " [voice-channel] : unmutevc [voice-channe]`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "mutevc";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"massvcmute", "vcmassmute", "mvc"};
    }

    @Override
    public Category getCategory() {
        return Category.MODERATION;
    }
}
