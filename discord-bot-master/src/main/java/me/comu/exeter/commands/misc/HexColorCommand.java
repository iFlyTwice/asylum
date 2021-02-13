package me.comu.exeter.commands.misc;

import me.comu.exeter.core.Core;
import me.comu.exeter.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class HexColorCommand implements ICommand {
    @Override
    public void handle(List<String> args, GuildMessageReceivedEvent event) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("Please specify a valid hex-color.").queue();
            return;
        }
        try {
            String input = args.get(0).replace("#", "").replace("0x", "");
            Color color = Color.decode("#" + input);
            event.getChannel().sendMessage(new EmbedBuilder().setColor(color).addField("Hex", "#" + input, false).addField("RGB", +color.getRed() + ", " + color.getGreen() + ", " + color.getBlue(), false).build()).queue();
        } catch (NumberFormatException ex)
        {
            event.getChannel().sendMessage("Invalid hex-color").queue();
        }
    }

    @Override
    public String getHelp() {
        return "Returns information about the hex color specified\n`" + Core.PREFIX + getInvoke() + "[hex-color]`\nAliases: `" + Arrays.deepToString(getAlias()) + "`";
    }

    @Override
    public String getInvoke() {
        return "hexcolor";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"hex", "checkhex","color", "checkhexcolor", "gethex", "gethexcolor", "colorhex"};
    }

    @Override
    public Category getCategory() {
        return Category.MISC;
    }
}
