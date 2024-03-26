package com.promcteam.codex.util.actions.actions.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.util.actions.actions.IActionExecutor;
import com.promcteam.codex.util.actions.actions.IActionType;
import com.promcteam.codex.util.actions.params.IParamResult;
import com.promcteam.codex.util.actions.params.IParamType;
import com.promcteam.codex.util.random.Rnd;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Firework extends IActionExecutor {

    public Action_Firework(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.FIREWORK);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_Firework_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        if (!targets.isEmpty()) {
            for (Entity target : targets) {
                Rnd.spawnRandomFirework(target.getLocation());
            }
        } else {
            Rnd.spawnRandomFirework(exe.getLocation());
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }

}
