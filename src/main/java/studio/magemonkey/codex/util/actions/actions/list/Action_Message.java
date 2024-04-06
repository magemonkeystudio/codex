package studio.magemonkey.codex.util.actions.actions.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.MsgUT;
import studio.magemonkey.codex.util.actions.actions.IActionExecutor;
import studio.magemonkey.codex.util.actions.actions.IActionType;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
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
        return plugin.lang().Codex_Editor_Actions_Action_Message_Desc.asList();
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
