package com.promcteam.codex.util.actions.actions.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.util.actions.actions.IActionExecutor;
import com.promcteam.codex.util.actions.actions.IActionType;
import com.promcteam.codex.util.actions.params.IParamResult;
import com.promcteam.codex.util.actions.params.IParamType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Goto extends IActionExecutor {

    public Action_Goto(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.GOTO);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_Goto_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.NAME);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {

    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }
}
