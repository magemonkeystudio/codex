package studio.magemonkey.codex.api;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum CommandType {
    PLAYER {
        @Override
        public void invoke(final CommandSender user, final String command, final Replacer... r) {
            if (user instanceof Player) { //If the user is running it, run through normal methods to ensure events are called.
                ((Player) user).chat("/" + replace(command, user, r));
            } else { //Otherwise, run it as if console.
                Bukkit.dispatchCommand(user, replace(command, user, r));
            }
        }
    },
    OP {
        @Override
        public void invoke(final CommandSender user, final String command, final Replacer... r) {
            final boolean isOp = user.isOp();
            try {
                if (!isOp) // don't op if he had op
                {
                    user.setOp(true);
                }
                if (user instanceof Player) { //If the user is running it, run through normal methods to ensure events are called.
                    ((Player) user).chat("/" + replace(command, user, r));
                } else { //Otherwise, run it as if console.
                    Bukkit.dispatchCommand(user, replace(command, user, r));
                }
                // don't de-op if he had op
                if (!isOp) user.setOp(false);
            } finally {
                // for sure... shit happens
                if (!isOp) user.setOp(false);
            }
        }
    },
    CONSOLE {
        @Override
        public void invoke(final CommandSender user, final String command, final Replacer... r) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replace(command, user, r));
        }
    };

    public abstract void invoke(CommandSender user, String command, Replacer... r);

    private static String replace(final String command, final CommandSender user, final Replacer... replacements) {
        final String[] keys   = new String[replacements.length + 1];
        final String[] values = new String[replacements.length + 1];
        int            i      = 0;
        values[i] = user.getName();
        keys[i++] = "{player}";
        for (final Replacer replacer : replacements) {
            values[i] = replacer.getTo();
            keys[i++] = replacer.getFrom();
        }
        return StringUtils.replaceEach(command, keys, values);
    }
}
