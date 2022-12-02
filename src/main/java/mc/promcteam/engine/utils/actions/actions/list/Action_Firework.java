package mc.promcteam.engine.utils.actions.actions.list;

import mc.promcteam.engine.NexPlugin;
import mc.promcteam.engine.utils.actions.actions.IActionExecutor;
import mc.promcteam.engine.utils.actions.actions.IActionType;
import mc.promcteam.engine.utils.actions.params.IParamResult;
import mc.promcteam.engine.utils.actions.params.IParamType;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Firework extends IActionExecutor {

    public Action_Firework(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.FIREWORK);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Firework_Desc.asList();
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
