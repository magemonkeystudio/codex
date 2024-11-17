package studio.magemonkey.codex.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.Debugger;
import studio.magemonkey.codex.util.SerializationBuilder;
import studio.magemonkey.risecore.legacy.util.DeserializationWorker;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class DelayedCommand implements ConfigurationSerializable {
    private CommandType as;
    private String      cmd;
    private int         delay;

    public DelayedCommand() {
        this.as = CommandType.CONSOLE;
        this.cmd = "";
        this.delay = 0;
        Debugger.log("Created DelayedCommand with empty constructor");
    }

    public DelayedCommand(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.delay = w.getInt("delay", 0);
        this.as = w.getEnum("as", CommandType.CONSOLE);
        this.cmd = w.getString("cmd");
        Validate.notEmpty(this.cmd, "Command can't be empty! " + this);
    }

    public static void invoke(final Plugin plugin,
                              final CommandSender target,
                              final Iterable<DelayedCommand> commands,
                              final Replacer... reps) {
        invoke(plugin, target, commands, null, reps);
    }

    public static void invoke(final Plugin plugin,
                              final CommandSender target,
                              final Iterable<DelayedCommand> commands,
                              final Runnable onEnd,
                              final Replacer... reps) {
        final Iterator<DelayedCommand> it = commands.iterator();
        if (!it.hasNext()) {
            return;
        }
        it.next().invoke(plugin, target, it, onEnd, reps);
    }

    public static List<DelayedCommand> deserializeMapList(List<Map<String, Object>> list) {
        return list.stream().map(DelayedCommand::new).collect(Collectors.toList());
    }

    public void invoke(final Plugin plugin,
                       final CommandSender target,
                       final Iterator<DelayedCommand> next,
                       final Runnable onEnd,
                       final Replacer... reps) {
        final Runnable action = () ->
        {
            this.as.invoke(target, this.cmd, reps);
            if ((next != null) && next.hasNext()) {
                next.next().invoke(plugin, target, next, onEnd, reps);
            } else {
                // that was last element.
                if (onEnd != null) onEnd.run();
            }
        };
        if (this.delay == 0) {
            action.run();
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, action, this.delay);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return SerializationBuilder.start(3)
                .append("delay", this.delay)
                .append("as", this.as)
                .append("cmd", this.cmd)
                .build();
    }

    @Override
    public String toString() {
        return new org.apache.commons.lang3.builder.ToStringBuilder(this,
                org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("as", this.as)
                .append("cmd", this.cmd)
                .append("delay", this.delay)
                .toString();
    }
}
