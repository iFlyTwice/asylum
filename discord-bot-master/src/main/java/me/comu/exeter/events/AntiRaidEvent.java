package me.comu.exeter.events;

import me.comu.exeter.commands.admin.AntiRaidChannelSafetyCommand;
import me.comu.exeter.commands.admin.AntiRaidCommand;
import me.comu.exeter.commands.admin.WhitelistCommand;
import me.comu.exeter.core.Core;
import me.comu.exeter.util.CompositeKey;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AntiRaidEvent extends ListenerAdapter {

    @Override
    public void onVoiceChannelCreate(@Nonnull VoiceChannelCreateEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                if (auditLogEntries.get(0).getType().equals(ActionType.CHANNEL_CREATE)) {
                    String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                    Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                        int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                        if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 2)
                            return;
                    }
                    if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                        Member member = event.getGuild().getMemberById(id);
                        if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                            return;
                        List<Role> roles = Objects.requireNonNull(member).getRoles();
                        String[] stringArray = new String[member.getRoles().size()];
                        List<String> strings = Arrays.asList(stringArray);
                        for (int i = 0; i < roles.size(); i++) {
                            stringArray[i] = roles.get(i).getName();
                        }
                        stringArray = strings.toArray(new String[0]);
                        if (member.getRoles().size() == 0) {
                            event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();
                        } else {
                            for (Role role : member.getRoles()) {
                                if (event.getGuild().getSelfMember().canInteract(role)) {
                                    if (role.isManaged() || role.isPublicRole()) {
                                        role.getManager().revokePermissions(Permission.values()).queue();
                                    }
                                    if (!role.isManaged()) {
                                        event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                    }
                                }
                            }
                        }
                        String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                        String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                        String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                        LocalDateTime now = LocalDateTime.now();
                        String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                        Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `VOICE_CHANNEL_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!userComu.equalsIgnoreCase(userOwner))
                            Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `VOICE_CHANNEL_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                            for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                    User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                    if (!Objects.requireNonNull(whitelistUser).isBot())
                                        Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `VOICE_CHANNEL_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onVoiceChannelDelete(@Nonnull VoiceChannelDeleteEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (!AntiRaidChannelSafetyCommand.isActive()) {
                event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                    if (auditLogEntries.get(0).getType().equals(ActionType.CHANNEL_DELETE)) {
                        String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                        Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                        if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                            int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                            if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 2)
                                return;
                        }
                        if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                            Member member = event.getGuild().getMemberById(id);
                            if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                                return;
                            List<Role> roles = Objects.requireNonNull(member).getRoles();
                            String[] stringArray = new String[member.getRoles().size()];
                            List<String> strings = Arrays.asList(stringArray);
                            for (int i = 0; i < roles.size(); i++) {
                                stringArray[i] = roles.get(i).getName();
                            }
                            stringArray = strings.toArray(new String[0]);
                            if (member.getRoles().size() == 0) {
                                event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();

                            } else {
                                for (Role role : member.getRoles()) {
                                    if (event.getGuild().getSelfMember().canInteract(role)) {
                                        if (role.isManaged() || role.isPublicRole()) {
                                            role.getManager().revokePermissions(Permission.values()).queue();
                                        }
                                        if (!role.isManaged()) {
                                            event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                        }
                                    }
                                }
                            }
                            String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                            String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                            String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                            LocalDateTime now = LocalDateTime.now();
                            String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                            Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `VOICE_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!userComu.equalsIgnoreCase(userOwner))
                                Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `VOICE_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                                for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                        User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                        if (!Objects.requireNonNull(whitelistUser).isBot())
                                            Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `VOICE_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                    }
                                }
                            }
                        }
                    }
                });
            } else {
                event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                    if (auditLogEntries.get(0).getType().equals(ActionType.CHANNEL_DELETE)) {
                        String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                        Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                        if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                            int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                            if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 2)
                                return;
                        }
                        if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                            Member member = event.getGuild().getMemberById(id);
                            if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                                return;
                            List<Role> roles = Objects.requireNonNull(member).getRoles();
                            String[] stringArray = new String[member.getRoles().size()];
                            List<String> strings = Arrays.asList(stringArray);
                            for (int i = 0; i < roles.size(); i++) {
                                stringArray[i] = roles.get(i).getName();
                            }
                            stringArray = strings.toArray(new String[0]);
                            if (member.getRoles().size() == 0) {
                                event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();
                            } else {
                                for (Role role : member.getRoles()) {
                                    if (event.getGuild().getSelfMember().canInteract(role)) {
                                        if (role.isManaged() || role.isPublicRole()) {
                                            role.getManager().revokePermissions(Permission.values()).queue();
                                        }
                                        if (!role.isManaged()) {
                                            event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                        }
                                    }
                                }
                            }
                            // VOICE CHANNELS
                            if (AntiRaidChannelSafetyCommand.restorableChannels.stream().anyMatch((restorableCategory -> restorableCategory.getId().equals(event.getChannel().getId())))) {
                                AntiRaidChannelSafetyCommand.restorableChannels.forEach(restorableChannel -> {
                                    if (restorableChannel.getGuildId().equals(event.getGuild().getId()) && restorableChannel.getId().equals(event.getChannel().getId()) && restorableChannel.getChannelType().equals(ChannelType.VOICE))
                                        event.getGuild().createTextChannel(restorableChannel.getName()).setPosition(restorableChannel.getPosition()).queue();
                                        AntiRaidChannelSafetyCommand.restore();
                                });
                            }

                            String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                            String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                            String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                            LocalDateTime now = LocalDateTime.now();
                            String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                            Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `VOICE_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!userComu.equalsIgnoreCase(userOwner))
                                Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `VOICE_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                                for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                        User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                        if (!Objects.requireNonNull(whitelistUser).isBot())
                                            Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `VOICE_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                if (auditLogEntries.get(0).getType().equals(ActionType.CHANNEL_CREATE)) {
                    String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                    Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                        int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                        if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 2)
                            return;
                    }
                    if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                        Member member = event.getGuild().getMemberById(id);
                        if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                            return;
                        List<Role> roles = Objects.requireNonNull(member).getRoles();
                        String[] stringArray = new String[member.getRoles().size()];
                        List<String> strings = Arrays.asList(stringArray);
                        for (int i = 0; i < roles.size(); i++) {
                            stringArray[i] = roles.get(i).getName();
                        }
                        stringArray = strings.toArray(new String[0]);
                        if (member.getRoles().size() == 0) {
                            event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();

                        } else {
                            for (Role role : member.getRoles()) {
                                if (event.getGuild().getSelfMember().canInteract(role)) {
                                    if (role.isManaged() || role.isPublicRole()) {
                                        role.getManager().revokePermissions(Permission.values()).queue();
                                    }
                                    if (!role.isManaged()) {
                                        event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                    }
                                }
                            }
                        }
                        String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                        String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                        String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                        LocalDateTime now = LocalDateTime.now();
                        String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                        Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CHANNEL_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!userComu.equalsIgnoreCase(userOwner))
                            Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CHANNEL_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                            for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                    User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                    if (!Objects.requireNonNull(whitelistUser).isBot())
                                        Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CHANNEL_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                }
                            }
                        }

                    }
                }
            });
        }
    }

    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (!AntiRaidChannelSafetyCommand.isActive()) {
                event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                    if (auditLogEntries.get(0).getType().equals(ActionType.CHANNEL_DELETE)) {
                        String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                        Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                        if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                            int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                            if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 2)
                                return;
                        }
                        if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                            Member member = event.getGuild().getMemberById(id);
                            if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                                return;
                            List<Role> roles = Objects.requireNonNull(member).getRoles();
                            String[] stringArray = new String[member.getRoles().size()];
                            List<String> strings = Arrays.asList(stringArray);
                            for (int i = 0; i < roles.size(); i++) {
                                stringArray[i] = roles.get(i).getName();
                            }
                            stringArray = strings.toArray(new String[0]);
                            if (member.getRoles().size() == 0) {
                                event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();
                            } else {
                                for (Role role : member.getRoles()) {
                                    if (event.getGuild().getSelfMember().canInteract(role)) {
                                        if (role.isManaged() || role.isPublicRole()) {
                                            role.getManager().revokePermissions(Permission.values()).queue();
                                        }
                                        if (!role.isManaged()) {
                                            event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                        }
                                    }
                                }
                            }
                            String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                            String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                            String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                            LocalDateTime now = LocalDateTime.now();
                            String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                            Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `TEXT_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!userComu.equalsIgnoreCase(userOwner))
                                Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `TEXT_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                                for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                        User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                        if (!Objects.requireNonNull(whitelistUser).isBot())
                                            Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `TEXT_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                    }
                                }
                            }

                        }
                    }
                });
            } else {
                event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                    if (auditLogEntries.get(0).getType().equals(ActionType.CHANNEL_DELETE)) {
                        String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                        Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                        if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                            int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                            if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 2)
                                return;
                        }
                        if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                            Member member = event.getGuild().getMemberById(id);
                            if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                                return;
                            List<Role> roles = Objects.requireNonNull(member).getRoles();
                            String[] stringArray = new String[member.getRoles().size()];
                            List<String> strings = Arrays.asList(stringArray);
                            for (int i = 0; i < roles.size(); i++) {
                                stringArray[i] = roles.get(i).getName();
                            }
                            stringArray = strings.toArray(new String[0]);
                            if (member.getRoles().size() == 0) {
                                event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();
                            } else {
                                for (Role role : member.getRoles()) {
                                    if (event.getGuild().getSelfMember().canInteract(role)) {
                                        if (role.isManaged() || role.isPublicRole()) {
                                            role.getManager().revokePermissions(Permission.values()).queue();
                                        }
                                        if (!role.isManaged()) {
                                            event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                        }
                                    }
                                }
                            }
                            // TEXT CHANNELS
                            if (AntiRaidChannelSafetyCommand.restorableChannels.stream().anyMatch(restorableCategory -> restorableCategory.getId().equals(event.getChannel().getId()))) {
                                AntiRaidChannelSafetyCommand.restorableChannels.forEach(restorableChannel -> {
                                    if (restorableChannel.getGuildId().equals(event.getGuild().getId()) && restorableChannel.getId().equals(event.getChannel().getId()) && restorableChannel.getChannelType().equals(ChannelType.TEXT))
                                        event.getGuild().createTextChannel(restorableChannel.getName()).setPosition(restorableChannel.getPosition()).queue();
                                        AntiRaidChannelSafetyCommand.restore();
                                });
                            }

                            String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                            String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                            String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                            LocalDateTime now = LocalDateTime.now();
                            String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                            Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `TEXT_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `ARCS & Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!userComu.equalsIgnoreCase(userOwner))
                                Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `TEXT_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `ARCS & Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                                for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                        User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                        if (!Objects.requireNonNull(whitelistUser).isBot())
                                            Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `TEXT_CHANNEL_DELETE`\nBot: " + botCheck + "\nAction Taken: `ARCS & Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                    }
                                }
                            }

                        }
                    }
                });
            }
        }
    }

    @Override
    public void onCategoryCreate(@Nonnull CategoryCreateEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                if (auditLogEntries.get(0).getType().equals(ActionType.CHANNEL_CREATE)) {
                    String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                    Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                        int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                        if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 2)
                            return;
                    }
                    if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                        Member member = event.getGuild().getMemberById(id);
                        if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                            return;
                        List<Role> roles = Objects.requireNonNull(member).getRoles();
                        String[] stringArray = new String[member.getRoles().size()];
                        List<String> strings = Arrays.asList(stringArray);
                        for (int i = 0; i < roles.size(); i++) {
                            stringArray[i] = roles.get(i).getName();
                        }
                        stringArray = strings.toArray(new String[0]);
                        if (member.getRoles().size() == 0) {
                            event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();

                        } else {
                            for (Role role : member.getRoles()) {
                                if (event.getGuild().getSelfMember().canInteract(role)) {
                                    if (role.isManaged() || role.isPublicRole()) {
                                        role.getManager().revokePermissions(Permission.values()).queue();
                                    }
                                    if (!role.isManaged()) {
                                        event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                    }
                                }
                            }
                        }
                        String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                        String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                        String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                        LocalDateTime now = LocalDateTime.now();
                        String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                        Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CATEGORY_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!userComu.equalsIgnoreCase(userOwner))
                            Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CATEGORY_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                            for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                    User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                    if (!Objects.requireNonNull(whitelistUser).isBot())
                                        Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CATEGORY_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                }
                            }
                        }
                    }

                }
            });

        }
    }

    @Override
    public void onCategoryDelete(@Nonnull CategoryDeleteEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (!AntiRaidChannelSafetyCommand.isActive()) {
                event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                    if (auditLogEntries.get(0).getType().equals(ActionType.CHANNEL_DELETE)) {
                        String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                        Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                        if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                            int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                            if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 2)
                                return;
                        }
                        if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                            Member member = event.getGuild().getMemberById(id);
                            if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                                return;
                            List<Role> roles = Objects.requireNonNull(member).getRoles();
                            String[] stringArray = new String[member.getRoles().size()];
                            List<String> strings = Arrays.asList(stringArray);
                            for (int i = 0; i < roles.size(); i++) {
                                stringArray[i] = roles.get(i).getName();
                            }
                            stringArray = strings.toArray(new String[0]);
                            if (member.getRoles().size() == 0) {
                                event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();

                            } else {
                                for (Role role : member.getRoles()) {
                                    if (event.getGuild().getSelfMember().canInteract(role)) {
                                        if (role.isManaged() || role.isPublicRole()) {
                                            role.getManager().revokePermissions(Permission.values()).queue();
                                        }
                                        if (!role.isManaged()) {
                                            event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                        }
                                    }
                                }
                            }
                            String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                            String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                            String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                            LocalDateTime now = LocalDateTime.now();
                            String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                            Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CATEGORY_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!userComu.equalsIgnoreCase(userOwner))
                                Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CATEGORY_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                                for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                        User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                        if (!Objects.requireNonNull(whitelistUser).isBot())
                                            Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CATEGORY_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                    }
                                }
                            }

                        }
                    }
                });

            } else {
                event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                    if (auditLogEntries.get(0).getType().equals(ActionType.CHANNEL_DELETE)) {
                        String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                        Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                        if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                            int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                            if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 2)
                                return;
                        }
                        if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                            Member member = event.getGuild().getMemberById(id);
                            if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                                return;
                            List<Role> roles = Objects.requireNonNull(member).getRoles();
                            String[] stringArray = new String[member.getRoles().size()];
                            List<String> strings = Arrays.asList(stringArray);
                            for (int i = 0; i < roles.size(); i++) {
                                stringArray[i] = roles.get(i).getName();
                            }
                            stringArray = strings.toArray(new String[0]);
                            if (member.getRoles().size() == 0) {
                                event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();

                            } else {
                                for (Role role : member.getRoles()) {
                                    if (event.getGuild().getSelfMember().canInteract(role)) {
                                        if (role.isManaged() || role.isPublicRole()) {
                                            role.getManager().revokePermissions(Permission.values()).queue();
                                        }
                                        if (!role.isManaged()) {
                                            event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                        }
                                    }
                                }
                            }
                            // CATEGORIES
                            if (AntiRaidChannelSafetyCommand.restorableCategories.stream().anyMatch((restorableCategory -> restorableCategory.getId().equals(event.getId())))) {
                                AntiRaidChannelSafetyCommand.restorableCategories.stream().filter(restorableCategory -> restorableCategory.getGuildId().equals(event.getGuild().getId())).filter(restorableCategory -> restorableCategory.getId().equals(event.getId())).forEach((restorableCategory -> event.getGuild().createCategory(restorableCategory.getName()).setPosition(restorableCategory.getPosition()).queue((category -> {
                                    HashMap<Integer, String> hashMap = restorableCategory.getChildTextChannels();
                                    for (Map.Entry<Integer, String> entry : hashMap.entrySet()) {
                                        event.getGuild().createTextChannel( entry.getValue()).setParent(category).setPosition( entry.getKey()).queue();
                                    }
                                    HashMap<Integer, String> hashMap2 = restorableCategory.getChildVoiceChannels();
                                    for (Map.Entry<Integer, String> entry : hashMap2.entrySet()) {
                                        event.getGuild().createVoiceChannel( entry.getValue()).setParent(category).setPosition( entry.getKey()).queue();
                                    }
                                    AntiRaidChannelSafetyCommand.restore();
                                }))));
                            }
                            String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                            String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                            String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                            LocalDateTime now = LocalDateTime.now();
                            String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                            Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CATEGORY_DELETE`\nBot: " + botCheck + "\nAction Taken: `ARCS & Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!userComu.equalsIgnoreCase(userOwner))
                                Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CATEGORY_DELETE`\nBot: " + botCheck + "\nAction Taken: `ARCS & Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                                for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                        User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                        if (!Objects.requireNonNull(whitelistUser).isBot())
                                            Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `CATEGORY_DELETE`\nBot: " + botCheck + "\nAction Taken: `ARCS & Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                    }
                                }
                            }

                        }
                    }
                });

            }
        }
    }

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                if (auditLogEntries.get(0).getType().equals(ActionType.ROLE_CREATE) && auditLogEntries.get(1).getType().equals(ActionType.ROLE_CREATE)) {
                    String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                    Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                        int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                        if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 3)
                            return;
                    }
                    if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                        Member member = event.getGuild().getMemberById(id);
                        if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                            return;
                        List<Role> roles = Objects.requireNonNull(member).getRoles();
                        String[] stringArray = new String[member.getRoles().size()];
                        List<String> strings = Arrays.asList(stringArray);
                        for (int i = 0; i < roles.size(); i++) {
                            stringArray[i] = roles.get(i).getName();
                        }
                        stringArray = strings.toArray(new String[0]);

                        if (member.getRoles().size() == 0) {
                            event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();

                        } else {
                            for (Role role : member.getRoles()) {
                                if (event.getGuild().getSelfMember().canInteract(role)) {
                                    if (role.isManaged() || role.isPublicRole()) {
                                        role.getManager().revokePermissions(Permission.values()).queue();
                                    }
                                    if (!role.isManaged()) {
                                        event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                    }
                                }
                            }
                        }
                        String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                        String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                        String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                        LocalDateTime now = LocalDateTime.now();
                        String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                        Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `ROLE_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!userComu.equalsIgnoreCase(userOwner))
                            Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `ROLE_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                            for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                    User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                    if (!Objects.requireNonNull(whitelistUser).isBot())
                                        Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `ROLE_CREATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                }
                            }
                        }

                    }
                }
            });

        }
    }

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                if (auditLogEntries.get(0).getType().equals(ActionType.ROLE_DELETE) && auditLogEntries.get(1).getType().equals(ActionType.ROLE_DELETE)) {
                    String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                    Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                        int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                        if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 3)
                            return;
                    }
                    if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                        Member member = event.getGuild().getMemberById(id);
                        if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                            return;
                        List<Role> roles = Objects.requireNonNull(member).getRoles();
                        String[] stringArray = new String[member.getRoles().size()];
                        List<String> strings = Arrays.asList(stringArray);
                        for (int i = 0; i < roles.size(); i++) {
                            stringArray[i] = roles.get(i).getName();
                        }
                        stringArray = strings.toArray(new String[0]);
                        if (member.getRoles().size() == 0) {
                            event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();

                        } else {
                            for (Role role : member.getRoles()) {
                                if (event.getGuild().getSelfMember().canInteract(role)) {
                                    if (role.isManaged() || role.isPublicRole()) {
                                        role.getManager().revokePermissions(Permission.values()).queue();
                                    }
                                    if (!role.isManaged()) {
                                        event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                    }
                                }
                            }
                        }
                        String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                        String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                        String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                        LocalDateTime now = LocalDateTime.now();
                        String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                        Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `ROLE_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!userComu.equalsIgnoreCase(userOwner))
                            Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `ROLE_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                        if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                            for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                    {
                                        User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                        if (!Objects.requireNonNull(whitelistUser).isBot())
                                            Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `ROLE_DELETE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nRoles Removed: `" + rolesRemoved + "`");
                                    }
                                }
                            }

                        }
                    }
                }
            });

        }

    }

    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (event.getNewPermissions().contains(Permission.ADMINISTRATOR) || event.getNewPermissions().contains(Permission.MANAGE_SERVER) || event.getNewPermissions().contains(Permission.BAN_MEMBERS) || event.getNewPermissions().contains(Permission.KICK_MEMBERS) || event.getNewPermissions().contains(Permission.MANAGE_WEBHOOKS) || event.getNewPermissions().contains(Permission.MANAGE_CHANNEL) || event.getNewPermissions().contains(Permission.MANAGE_ROLES)) {
                event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                    if (auditLogEntries.get(0).getType().equals(ActionType.ROLE_UPDATE)) {
                        String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                        Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                        if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                            int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                            if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 3)
                                return;
                        }
                        if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                            Member member = event.getGuild().getMemberById(id);
                            if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                                return;
                            List<Role> roles = Objects.requireNonNull(member).getRoles();
                            String[] stringArray = new String[member.getRoles().size()];
                            List<String> strings = Arrays.asList(stringArray);
                            for (int i = 0; i < roles.size(); i++) {
                                stringArray[i] = roles.get(i).getName();
                            }
                            stringArray = strings.toArray(new String[0]);
                            if (member.getRoles().size() == 0) {
                                event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();

                            } else {
                                for (Role role : member.getRoles()) {
                                    if (event.getGuild().getSelfMember().canInteract(role)) {
                                        if (role.isManaged() || role.isPublicRole()) {
                                            role.getManager().revokePermissions(Permission.values()).queue();
                                        }
                                        if (!role.isManaged()) {
                                            event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                        }
                                    }
                                }
                            }
                            String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                            event.getRole().getManager().setPermissions(event.getOldPermissions()).queue();
                            String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                            String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                            LocalDateTime now = LocalDateTime.now();
                            String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                            Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `MALICIOUS_PERMISSIONS`\nBot: " + botCheck + "\nAction Taken: `Reverted Permissions For \"" + event.getRole().getName() + "\" & Removed Roles`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!userComu.equalsIgnoreCase(userOwner))
                                Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `MALICIOUS_PERMISSIONS`\nBot: " + botCheck + "\nAction Taken: `Reverted Permissions For \"" + event.getRole().getName() + "\" & Removed Roles`\nRoles Removed: `" + rolesRemoved + "`");
                            if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                                for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                        {
                                            User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                            if (!Objects.requireNonNull(whitelistUser).isBot())
                                                Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nWhen: `" + dtf.format(now) + "`" + "\nType: `MALICIOUS_PERMISSIONS`\nBot: " + botCheck + "\nAction Taken: `Reverted Permissions For \"" + event.getRole().getName() + "\" & Removed Roles`\nRoles Removed: `" + rolesRemoved + "`");
                                        }
                                    }
                                }

                            }
                        }
                    }
                });
            }

        }
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        boolean active = AntiRaidCommand.isActive();
        if (active && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            List<Role> roleList = event.getRoles().stream().filter(role -> role.hasPermission(Permission.ADMINISTRATOR) || role.hasPermission(Permission.MANAGE_SERVER) || role.hasPermission(Permission.BAN_MEMBERS) || role.hasPermission(Permission.KICK_MEMBERS) || role.hasPermission(Permission.MANAGE_WEBHOOKS)).collect(Collectors.toList());
            if (!roleList.isEmpty()) {
                event.getGuild().retrieveAuditLogs().queue((auditLogEntries) -> {
                    if (auditLogEntries.get(0).getType().equals(ActionType.MEMBER_ROLE_UPDATE)) {
                        String id = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getId();
                        Long idLong = Objects.requireNonNull(auditLogEntries.get(0).getUser()).getIdLong();
                        if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), id, event.getGuild().getId())) {
                            int permissionLevel = Integer.parseInt(WhitelistCommand.getWhitelistedIDs().get(CompositeKey.of(event.getGuild().getId(), id)));
                            if (permissionLevel == 0 || permissionLevel == 1 || permissionLevel == 3)
                                return;
                        }
                        if (!idLong.equals(Core.OWNERID) && !id.equals(event.getJDA().getSelfUser().getId()) && !id.equals(event.getGuild().getOwnerId())) {
                            Member member = event.getGuild().getMemberById(id);
                            if (member != null && !event.getGuild().getSelfMember().canInteract(member))
                                return;
                            List<Role> roles = Objects.requireNonNull(member).getRoles();
                            String[] stringArray = new String[member.getRoles().size()];
                            List<String> strings = Arrays.asList(stringArray);

                            for (int i = 0; i < roles.size(); i++) {
                                stringArray[i] = roles.get(i).getName();
                            }
                            stringArray = strings.toArray(new String[0]);
                            if (member.getRoles().size() == 0) {
                                event.getGuild().getPublicRole().getManager().setPermissions(0).givePermissions(Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_READ, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD, Permission.VOICE_STREAM, Permission.NICKNAME_CHANGE).queue();

                            } else {
                                for (Role role : member.getRoles()) {
                                    if (event.getGuild().getSelfMember().canInteract(role)) {
                                        if (role.isManaged() || role.isPublicRole()) {
                                            role.getManager().revokePermissions(Permission.values()).queue();
                                        }
                                        if (!role.isManaged()) {
                                            event.getGuild().removeRoleFromMember(member.getId(), role).queue();
                                        }
                                    }
                                }
                            }
                            String rolesRemoved = (stringArray.length == 0) ? "@\u200beveryone" : Arrays.deepToString(stringArray);
                            String[] stringArray2 = new String[roleList.size()];
                            List<String> strings2 = Arrays.asList(stringArray2);
                            for (int i = 0; i < roleList.size(); i++) {
                                event.getGuild().removeRoleFromMember(event.getMember(), roleList.get(i)).queue();
                                stringArray2[i] = roleList.get(i).getName();
                            }
                            stringArray2 = strings2.toArray(new String[strings.size()]);
                            String userComu = Objects.requireNonNull(event.getJDA().getUserById(Core.OWNERID)).getId();
                            String userOwner = Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId();
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
                            LocalDateTime now = LocalDateTime.now();
                            String botCheck = member.getUser().isBot() ? "`Yes`" : "`No`";
                            Utility.sendPrivateMessage(event.getJDA(), userComu, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nSuspect: `" + Utility.removeMarkdown(event.getMember().getUser().getAsTag()) + " (" + event.getMember().getId() + ")`" + "\nWhen: `" + dtf.format(now) + "`" + "\nType: `MALICIOUS_MEMBER_ROLE_UPDATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nWizzer's Roles Removed: `" + rolesRemoved + "`\nSuspect's Roles Removed: `" + Arrays.deepToString(stringArray2) + "`");
                            if (!userComu.equalsIgnoreCase(userOwner))
                                Utility.sendPrivateMessage(event.getJDA(), userOwner, "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nSuspect: `" + Utility.removeMarkdown(event.getMember().getUser().getAsTag())  + " (" + event.getMember().getId() + ")" + "\nWhen: `" + dtf.format(now) + "`" + "\nType: `MALICIOUS_MEMBER_ROLE_UPDATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nWizzer's Roles Removed: `" + rolesRemoved + "`\nSuspect's Roles Removed: `" + Arrays.deepToString(stringArray2) + "`");
                            if (!WhitelistCommand.getWhitelistedIDs().isEmpty()) {
                                for (CompositeKey x : WhitelistCommand.getWhitelistedIDs().keySet()) {
                                    if (Utility.isWhitelisted(WhitelistCommand.getWhitelistedIDs(), x.getUserID(), x.getGuildID()) && x.getGuildID().equals(event.getGuild().getId())) {
                                        {
                                            User whitelistUser = event.getJDA().getUserById(x.getUserID());
                                            if (!Objects.requireNonNull(whitelistUser).isBot())
                                                Utility.sendPrivateMessage(event.getJDA(), Objects.requireNonNull(event.getJDA().getUserById(x.getUserID())).getId(), "**Anti-Raid Report For " + Utility.removeMarkdown(event.getGuild().getName()) + "**\nWizzer: `" + Utility.removeMarkdown(member.getUser().getAsTag()) + " (" + member.getId() + ")`\nSuspect: `" + Utility.removeMarkdown(event.getMember().getUser().getAsTag()) + " (" + event.getMember().getId() + ")" + "\nWhen: `" + dtf.format(now) + "`" + "\nType: `MALICIOUS_MEMBER_ROLE_UPDATE`\nBot: " + botCheck + "\nAction Taken: `Roles Removed`\nWizzer's Roles Removed: `" + rolesRemoved + "`\nSuspect's Roles Removed: `" + Arrays.deepToString(stringArray2) + "`");
                                        }
                                    }
                                }

                            }
                        }
                    }
                });
            }

        }
    }

}
