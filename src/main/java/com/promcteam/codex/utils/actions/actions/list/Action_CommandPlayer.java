package com.promcteam.codex.utils.actions.actions.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.utils.actions.actions.IActionExecutor;
import com.promcteam.codex.utils.actions.actions.IActionType;
import com.promcteam.codex.utils.actions.params.IParamResult;
import com.promcteam.codex.utils.actions.params.IParamType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_CommandPlayer extends IActionExecutor {

    public Action_CommandPlayer(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.COMMAND_PLAYER);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_CommandPlayer_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.MESSAGE);
        this.registerParam(IParamType.TARGET);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        if (!result.hasParam(IParamType.MESSAGE)) return;
        if (exe.getType() != EntityType.PLAYER) return;

        String text = result.getParamValue(IParamType.MESSAGE).getString(null);
        if (text == null) return;

        text = text.replace("%executor%", exe.getName());

        Player executor = (Player) exe;
        if (!targets.isEmpty()) {
            for (Entity e : targets) {
                String text2 = text.replace("%target%", e.getName());
                executor.performCommand(text2);
            }
        } else {
            executor.performCommand(text);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }

}
