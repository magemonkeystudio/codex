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

public class Action_Saturation extends IActionExecutor {

    public Action_Saturation(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.SATURATION);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_Saturation_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.AMOUNT);
        this.registerParam(IParamType.TARGET);
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
            double has          = livingEntity.getSaturation();
            if (percent) {
                amount2 = has * (amount / 100D);
            }

            livingEntity.setSaturation((float) (has + amount2));
        });
    }
}
