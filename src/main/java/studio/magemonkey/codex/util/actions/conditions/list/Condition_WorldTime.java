package studio.magemonkey.codex.util.actions.conditions.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.actions.conditions.IConditionType;
import studio.magemonkey.codex.util.actions.conditions.IConditionValidator;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import studio.magemonkey.codex.util.actions.params.IParamValue.IOperator;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Condition_WorldTime extends IConditionValidator {

    public Condition_WorldTime(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IConditionType.WORLD_TIME);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Condition_WorldTime_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.NAME);
        this.registerParam(IParamType.AMOUNT);
    }

    @Override
    @Nullable
    protected Predicate<Entity> validate(
            @NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {

        String worldName = result.getParamValue(IParamType.NAME).getString(null);
        World  world     = worldName != null ? plugin.getServer().getWorld(worldName) : null;

        long      timeReq = result.getParamValue(IParamType.AMOUNT).getInt(-1);
        IOperator oper    = result.getParamValue(IParamType.AMOUNT).getOperator();

        return target -> {
            long timeWorld = world == null ? target.getWorld().getTime() : world.getTime();
            return oper.check(timeWorld, timeReq);
        };
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }
}