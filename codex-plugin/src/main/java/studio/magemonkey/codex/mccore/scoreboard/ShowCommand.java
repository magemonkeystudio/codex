/**
 * Codex
 * studio.magemonkey.codex.mccore.scoreboard.ShowCommand
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2024 MageMonkeyStudio
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package studio.magemonkey.codex.mccore.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.ISubCommand;

import java.util.List;

/**
 * Shows a desired scoreboard for the player
 */
public class ShowCommand<P extends CodexPlugin<P>> extends ISubCommand<P> {
    ShowCommand(P plugin) {
        super(plugin, List.of("show"), ScoreboardNodes.SHOW.getNode());
    }

    /**
     * Executes the command
     *
     * @param sender  sender of the command
     * @param label   command label
     * @param args    command arguments
     */
    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            StringBuilder name = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++) {
                name.append(" ").append(args[i]);
            }
            PlayerBoards board = BoardManager.getPlayerBoards(sender.getName());
            if (board.showBoard(name.toString()))
                sender.sendMessage(ChatColor.DARK_GREEN + "Your scoreboard has been changed");
            else
                sender.sendMessage(ChatColor.DARK_RED + "You do not have a scoreboard with that name");
        } else printUsage(sender);
    }

    @Override
    @NotNull
    public String usage() {
        return "<boardName>";
    }

    /**
     * @return description
     */
    @Override
    @NotNull
    public String description() {
        return "Shows the scoreboard";
    }

    @Override
    public boolean playersOnly() {
        return true;
    }
}
