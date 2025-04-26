/**
 * Codex
 * studio.magemonkey.codex.mccore.chat.ChatCommander
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

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.commands.api.IGeneralCommand;

import java.util.List;

/**
 * Controls commands for the chat API
 */
public class ChatCommander<P extends CodexPlugin<P>> extends IGeneralCommand<P> {
    /**
     * Constructor
     *
     * @param plugin plugin reference
     */
    public ChatCommander(P plugin) {
        super(plugin, List.of("chat"));

        registerSubCommands();
    }

    /**
     * Registers the sub-commands
     */
    protected void registerSubCommands() {
        this.addSubCommand(new ListCommand<>(plugin));
        this.addSubCommand(new NameCommand<>(plugin));
        this.addSubCommand(new PrefixCommand<>(plugin));
        this.addSubCommand(new ResetCommand<>(plugin));
    }

    @Override
    @NotNull
    public String usage() {
        return "/%cmd% list - Displays unlocked prefixes\n" +
                "/%cmd% name <name> - Sets your chat name\n" +
                "/%cmd% prefix <prefix> - Sets your chat prefix\n" +
                "/%cmd% reset - Resets your chat name and prefix";
    }

    @Override
    @NotNull
    public String description() {
        return "Chat commands";
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        printUsage(sender);
    }
}
