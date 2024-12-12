package studio.magemonkey.codex.util.actions.params.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.hooks.Hooks;
import studio.magemonkey.codex.util.actions.params.IAutoValidated;
import studio.magemonkey.codex.util.actions.params.IParamType;
import studio.magemonkey.codex.util.actions.params.IParamValue;
import studio.magemonkey.codex.util.actions.params.defaults.IParamBoolean;

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
