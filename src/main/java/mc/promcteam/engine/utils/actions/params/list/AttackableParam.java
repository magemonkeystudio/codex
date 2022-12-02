package mc.promcteam.engine.utils.actions.params.list;

import mc.promcteam.engine.hooks.Hooks;
import mc.promcteam.engine.utils.actions.params.IAutoValidated;
import mc.promcteam.engine.utils.actions.params.IParamType;
import mc.promcteam.engine.utils.actions.params.IParamValue;
import mc.promcteam.engine.utils.actions.params.defaults.IParamBoolean;
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
