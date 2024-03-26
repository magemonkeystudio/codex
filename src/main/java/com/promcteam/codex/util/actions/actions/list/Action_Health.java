package com.promcteam.codex.util.actions.actions.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.util.EntityUT;
import com.promcteam.codex.util.actions.actions.IActionExecutor;
import com.promcteam.codex.util.actions.actions.IActionType;
import com.promcteam.codex.util.actions.params.IParamResult;
import com.promcteam.codex.util.actions.params.IParamType;
import com.promcteam.codex.util.actions.params.IParamValue;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Health extends IActionExecutor {

    public Action_Health(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.HEALTH);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_Health_Desc.asList();
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

        double hp = value.getDouble(0);
        if (hp == 0) return;

        boolean percent = value.getBoolean();

        targets.forEach(target -> {
            if (!(target instanceof LivingEntity)) return;

            LivingEntity livingEntity = (LivingEntity) target;
            double       hp2          = hp;
            double       maxHp        = EntityUT.getAttribute(livingEntity, Attribute.GENERIC_MAX_HEALTH);
            if (percent) {
                hp2 = maxHp * (hp / 100D);
            }

            livingEntity.setHealth(Math.min(livingEntity.getHealth() + hp2, maxHp));
        });
    }
}
