/**
 * Codex
 * studio.magemonkey.codex.mccore.chat.ResetCommand
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
package studio.magemonkey.codex.mccore.chat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.ISubCommand;

import java.util.List;

/**
 * Resets the player's display name
 */
class ResetCommand<P extends CodexPlugin<P>> extends ISubCommand<P> {
    ResetCommand(P plugin) {
        super(plugin, List.of("reset"), ChatNodes.RESET.getNode());
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
        ChatData data = Chat.getPlayerData(sender.getName());
        if (data != null) {
            data.setDisplayName(sender.getName());
            sender.sendMessage(ChatColor.DARK_GREEN + "Your name has been reset to its default");
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
        return "Resets your display name";
    }

    @Override
    public boolean playersOnly() {
        return true;
    }
}
