package me.comu.exeter.events;

import me.comu.exeter.commands.moderation.SetRainbowRoleCommand;
import me.comu.exeter.utility.Utility;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.role.update.RoleUpdateColorEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RainbowRoleEvent extends ListenerAdapter {


//    @Override
//    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {
//        long roleID = SetRainbowRoleCommand.getRoleID();
//        boolean isRainbowSet = SetRainbowRoleCommand.isIsRainbowRoleSet();
//        Guild guild = SetRainbowRoleCommand.guild;
//        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
//        ses.scheduleAtFixedRate(() -> {
//            if (isRainbowSet)
//            {
//                Role role = guild.getRoleById(roleID);
//                role.getManager().setColor(Core.getRandomColor()).queueAfter(2, TimeUnit.SECONDS);
//            }
//        }, 0, 2, TimeUnit.MINUTES);
//
//    }


    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {
        boolean isRainbowSet = SetRainbowRoleCommand.isIsRainbowRoleSet();
        long roleID = SetRainbowRoleCommand.getRoleID();
        Guild guild = SetRainbowRoleCommand.guild;
        while (isRainbowSet) {
            isRainbowSet = SetRainbowRoleCommand.isIsRainbowRoleSet();
            Role role = guild.getRoleById(roleID);
            Objects.requireNonNull(role).getManager().setColor(Utility.getRandomColor()).queueAfter(5, TimeUnit.SECONDS);
        }
    }
}
