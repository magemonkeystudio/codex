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

public class Action_Burn extends IActionExecutor {

    public Action_Burn(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.BURN);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_Burn_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.DURATION);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        int dura = 0;
        if (result.hasParam(IParamType.DURATION)) {
            dura = result.getParamValue(IParamType.DURATION).getInt(0);
        }
        if (dura <= 0) return;

        for (Entity e : targets) {
            e.setFireTicks(e.getFireTicks() + dura);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
