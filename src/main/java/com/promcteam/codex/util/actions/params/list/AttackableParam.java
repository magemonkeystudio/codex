package com.promcteam.codex.util.actions.params.list;

import com.promcteam.codex.hooks.Hooks;
import com.promcteam.codex.util.actions.params.IAutoValidated;
import com.promcteam.codex.util.actions.params.IParamType;
import com.promcteam.codex.util.actions.params.IParamValue;
import com.promcteam.codex.util.actions.params.defaults.IParamBoolean;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AttackableParam extends IParamBoolean implements IAutoValidated {

    public AttackableParam() {
        super(IParamType.ATTACKABLE, "attackable");
    }

    @Override
    public void autoValidate(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamValue val) {
        boolean b = val.getBoolean();
        targets.removeIf(target -> {
            boolean attackable = Hooks.canFights(exe, target);
            return attackable != b;
        });
    }
}
