package studio.magemonkey.codex.util.actions.actions.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.actions.actions.IActionExecutor;
import studio.magemonkey.codex.util.actions.actions.IActionType;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;

import java.util.List;
import java.util.Set;

public class Action_Lightning extends IActionExecutor {

    public Action_Lightning(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.LIGHTNING);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_Lightning_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        for (Entity e : targets) {
            e.getWorld().strikeLightning(e.getLocation());
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
