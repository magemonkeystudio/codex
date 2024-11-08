/**
 * Codex
 * studio.magemonkey.codex.mccore.commands.ICommand
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
package studio.magemonkey.codex.mccore.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Interface for handlers for commands
 */
public interface ICommand {

    /**
     * Executes a command
     *
     * @param handler command handler
     * @param plugin  plugin reference
     * @param sender  sender of the command
     * @param args    command arguments
     */
    public void execute(CommandHandler handler, Plugin plugin, CommandSender sender, String[] args);

    /**
     * @return permission needed for this command
     */
    public String getPermissionNode();

    /**
     * @return args string (e.g. &lt;playerName&gt; &lt;message&gt;)
     */
    public String getArgsString();

    /**
     * @return command description (be very brief)
     */
    public String getDescription();

    /**
     * @return type of sender required by the command
     */
    public SenderType getSenderType();
}
