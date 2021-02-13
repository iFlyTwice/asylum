package me.comu.exeter.pagination.listener;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MessageHandler extends ListenerAdapter {
    private final Map<String, Consumer<MessageReactionAddEvent>> events = new HashMap<>();

    public void addEvent(String id, Consumer<MessageReactionAddEvent> act) {
        events.put(id, act);
    }

    public void removeEvent(Message msg) {
        events.remove((msg.getChannelType().isGuild() ? msg.getGuild().getId() : msg.getPrivateChannel().getUser().getId()) + msg.getId());
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent evt) {
        Consumer<MessageReactionAddEvent> act = events.get(evt.getGuild().getId() + evt.getMessageId());
        if (act != null) act.accept(evt);
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent evt) {
        events.remove((evt.getChannelType().isGuild() ? evt.getGuild().getId() : evt.getPrivateChannel().getUser().getId()) + evt.getMessageId());
    }
}