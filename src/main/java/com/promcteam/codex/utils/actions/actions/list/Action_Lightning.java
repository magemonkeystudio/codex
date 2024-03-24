package com.promcteam.codex.utils.actions.actions.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.utils.actions.actions.IActionExecutor;
import com.promcteam.codex.utils.actions.actions.IActionType;
import com.promcteam.codex.utils.actions.params.IParamResult;
import com.promcteam.codex.utils.actions.params.IParamType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Lightning extends IActionExecutor {

    public Action_Lightning(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.LIGHTNING);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Lightning_Desc.asList();
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
