package com.promcteam.codex.utils.actions.conditions.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.utils.actions.conditions.IConditionType;
import com.promcteam.codex.utils.actions.conditions.IConditionValidator;
import com.promcteam.codex.utils.actions.params.IParamResult;
import com.promcteam.codex.utils.actions.params.IParamType;
import com.promcteam.codex.utils.actions.params.IParamValue.IOperator;
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
        return plugin.lang().Core_Editor_Actions_Condition_WorldTime_Desc.asList();
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