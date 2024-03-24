package com.promcteam.codex.utils.actions.params.list;

import com.promcteam.codex.utils.actions.params.IAutoValidated;
import com.promcteam.codex.utils.actions.params.IParamType;
import com.promcteam.codex.utils.actions.params.IParamValue;
import com.promcteam.codex.utils.actions.params.defaults.IParamBoolean;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AllowSelfParam extends IParamBoolean implements IAutoValidated {

    public AllowSelfParam() {
        super(IParamType.ALLOW_SELF, "allow-self");
    }

    @Override
    public void autoValidate(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamValue val) {
        boolean b = val.getBoolean();
        if (!b) {
            targets.remove(exe);
        } else {
            targets.add(exe);
        }
    }
}
