package com.promcteam.codex.commands;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.commands.api.IGeneralCommand;
import com.promcteam.codex.commands.list.*;
import com.promcteam.codex.manager.api.Loadable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class CommandManager<P extends CodexPlugin<P>> implements Loadable {
    @NotNull
    private final P                       plugin;
    private       Set<IGeneralCommand<P>> commands;
    private       MainCommand<P>          mainCommand;

    public CommandManager(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup() {
        this.commands = new HashSet<>();

        // Create main plugin command and attach help sub-command as a default executor.
        this.mainCommand = new MainCommand<>(this.plugin);
        this.mainCommand.addDefaultCommand(new HelpCommand<>(this.plugin));

        // Register child plugin sub-commands to the main plugin command.
        this.plugin.registerCommands(this.mainCommand);

        // Check for plugin settings to register default commands.
        if (this.plugin.hasEditor()) {
            this.mainCommand.addSubCommand(new EditorCommand<>(this.plugin));
        }
        this.mainCommand.addSubCommand(new ReloadCommand<>(this.plugin));

        if (!this.plugin.isEngine()) {
            this.mainCommand.addSubCommand(new AboutCommand<>(this.plugin));
        }

        // Register main command as a bukkit command.
        this.registerCommand(this.mainCommand);
    }

    @Override
    public void shutdown() {
        for (IGeneralCommand<P> cmd : new HashSet<>(this.commands)) {
            this.unregisterCommand(cmd);
            cmd.clearSubCommands();
            cmd = null;
        }
        this.commands.clear();
    }

    @NotNull
    public Set<IGeneralCommand<P>> getCommands() {
        return this.commands;
    }

    @NotNull
    public MainCommand<P> getMainCommand() {
        return this.mainCommand;
    }

    public void registerCommand(@NotNull IGeneralCommand<P> cmd) {
        if (this.commands.add(cmd)) {
            CommandRegister.register(this.plugin, cmd);
        }
    }

    public void unregisterCommand(@NotNull IGeneralCommand<P> cmd) {
        if (this.commands.remove(cmd)) {
            CommandRegister.unregister(this.plugin, cmd.labels());
        }
    }
}
