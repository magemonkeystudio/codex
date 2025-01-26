/**
 * Codex
 * studio.magemonkey.codex.mccore.scoreboard.CycleCommand
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.ISubCommand;

import java.util.List;

/**
 * Makes a player's scoreboard start cycling
 */
public class CycleCommand<P extends CodexPlugin<P>> extends ISubCommand<P> {
    CycleCommand(P plugin) {
        super(plugin, List.of("cycle"), ScoreboardNodes.CYCLE.getNode());
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
        if (sender instanceof Player) {
            PlayerBoards board = BoardManager.getPlayerBoards(sender.getName());
            if (board.isCycling())
                sender.sendMessage(ChatColor.DARK_RED + "Your scoreboard is already cycling");
            else {
                board.startCycling();
                sender.sendMessage(ChatColor.DARK_GREEN + "Your scoreboard is now cycling");
            }
        } else printUsage(sender);
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    /**
     * @return description
     */
    @Override
    @NotNull
    public String description() {
        return "Makes the scoreboard cycle";
    }

    @Override
    public boolean playersOnly() {
        return true;
    }
}
