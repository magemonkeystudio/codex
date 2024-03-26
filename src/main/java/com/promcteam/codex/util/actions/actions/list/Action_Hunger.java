package com.promcteam.codex.util.actions.actions.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.util.actions.actions.IActionExecutor;
import com.promcteam.codex.util.actions.actions.IActionType;
import com.promcteam.codex.util.actions.params.IParamResult;
import com.promcteam.codex.util.actions.params.IParamType;
import com.promcteam.codex.util.actions.params.IParamValue;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Hunger extends IActionExecutor {

    public Action_Hunger(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.HUNGER);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_Hunger_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.AMOUNT);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        IParamValue value = result.getParamValue(IParamType.AMOUNT);

        double amount = value.getDouble(0);
        if (amount == 0) return;

        boolean percent = value.getBoolean();

        targets.forEach(target -> {
            if (!(target instanceof Player)) return;

            Player livingEntity = (Player) target;
            double amount2      = amount;
            double max          = 20;
            if (percent) {
                amount2 = max * (amount / 100D);
            }

            livingEntity.setFoodLevel((int) Math.min(livingEntity.getFoodLevel() + amount2, max));
        });
    }
}
