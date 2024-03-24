package com.promcteam.risecore.legacy.cmds;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum CommandType {
    PLAYER {
        @Override
        public void invoke(final CommandSender user, final String command, final R... r) {
            if (user instanceof Player) { //If the user is running it, run through normal methods to ensure events are called.
                ((Player) user).chat("/" + repl(command, user, r));
            } else { //Otherwise, run it as if console.
                Bukkit.dispatchCommand(user, repl(command, user, r));
            }
        }
    },
    OP {
        @Override
        public void invoke(final CommandSender user, final String command, final R... r) {
            final boolean isOp = user.isOp();
            try {
                if (!isOp) // don't op if he had op
                {
                    user.setOp(true);
                }
                if (user instanceof Player) { //If the user is running it, run through normal methods to ensure events are called.
                    ((Player) user).chat("/" + repl(command, user, r));
                } else { //Otherwise, run it as if console.
                    Bukkit.dispatchCommand(user, repl(command, user, r));
                }
                if (!isOp) // don't de-op if he had op
                {
                    user.setOp(false);
                }
            } finally // for sure... shit happens
            {
                if (!isOp) {
                    user.setOp(false);
                }
            }
        }
    },
    CONSOLE {
        @Override
        public void invoke(final CommandSender user, final String command, final R... r) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), repl(command, user, r));
        }
    };

    public abstract void invoke(CommandSender user, String command, R... r);

    private static String repl(final String command, final CommandSender user, final R... reps) {
        final String[] keys   = new String[reps.length + 1];
        final String[] values = new String[reps.length + 1];
        int            i      = 0;
        values[i] = user.getName();
        keys[i++] = "{player}";
        for (final R rep : reps) {
            values[i] = rep.getTo();
            keys[i++] = rep.getFrom();
        }
        return StringUtils.replaceEach(command, keys, values);
    }
}
