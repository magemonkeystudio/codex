package studio.magemonkey.codex.util.actions.params.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.actions.params.IAutoValidated;
import studio.magemonkey.codex.util.actions.params.IParamType;
import studio.magemonkey.codex.util.actions.params.IParamValue;
import studio.magemonkey.codex.util.actions.params.defaults.IParamBoolean;

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
