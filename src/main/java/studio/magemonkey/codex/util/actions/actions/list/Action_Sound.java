package studio.magemonkey.codex.util.actions.actions.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.MsgUT;
import studio.magemonkey.codex.util.actions.actions.IActionExecutor;
import studio.magemonkey.codex.util.actions.actions.IActionType;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Sound extends IActionExecutor {

    public Action_Sound(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.SOUND);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_Sound_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.NAME);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        String name = result.getParamValue(IParamType.NAME).getString(null);
        if (name == null) return;

        for (Entity e : targets) {
            if (e.getType() == EntityType.PLAYER) {
                MsgUT.sound((Player) e, name);
            }
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
