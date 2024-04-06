package studio.magemonkey.codex.util.actions.actions.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.actions.actions.IActionExecutor;
import studio.magemonkey.codex.util.actions.actions.IActionType;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_CommandConsole extends IActionExecutor {

    public Action_CommandConsole(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.COMMAND_CONSOLE);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_CommandConsole_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.MESSAGE);
        this.registerParam(IParamType.TARGET);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        if (!result.hasParam(IParamType.MESSAGE)) return;

        String text = result.getParamValue(IParamType.MESSAGE).getString(null);
        if (text == null) return;

        text = text.replace("%executor%", exe.getName());

        if (!targets.isEmpty()) {
            for (Entity e : targets) {
                String text2 = text.replace("%target%", e.getName());
                plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), text2);
            }
        } else {
            plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), text);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }
}
