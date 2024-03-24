package com.promcteam.codex.utils.actions.actions.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.utils.MsgUT;
import com.promcteam.codex.utils.actions.actions.IActionExecutor;
import com.promcteam.codex.utils.actions.actions.IActionType;
import com.promcteam.codex.utils.actions.params.IParamResult;
import com.promcteam.codex.utils.actions.params.IParamType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Message extends IActionExecutor {

    public Action_Message(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.MESSAGE);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Message_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.MESSAGE);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        if (!result.hasParam(IParamType.MESSAGE)) return;

        String text = result.getParamValue(IParamType.MESSAGE).getString(null);
        if (text == null) return;

        text = text.replace("%executor%", exe.getName());

        for (Entity e : targets) {
            MsgUT.sendWithJSON(e, text.replace("%target%", e.getName()));
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
