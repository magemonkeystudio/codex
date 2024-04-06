package studio.magemonkey.codex.util.actions.targets;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.actions.Parametized;
import studio.magemonkey.codex.util.actions.params.IAutoValidated;
import studio.magemonkey.codex.util.actions.params.IParam;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class ITargetSelector extends Parametized {

    public ITargetSelector(@NotNull CodexPlugin<?> plugin, @NotNull String key) {
        super(plugin, key);
        this.registerParam(IParamType.NAME);
    }

    public final void select(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull String fullStr) {
        IParamResult result = this.getParamResult(fullStr);

        this.validateTarget(exe, targets, result);
        this.autoValidate(exe, targets, result);
    }

    protected abstract void validateTarget(
            @NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result);

    private final void autoValidate(
            @NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {

        Entity executor = exe;
        if (exe instanceof Projectile) {
            Projectile       pj = (Projectile) exe;
            ProjectileSource ps = pj.getShooter();
            if (ps instanceof Entity) {
                executor = (Entity) ps;
            }
        }

        for (IParam param : this.getParams()) {
            if (!result.hasParam(param.getKey())) continue;
            if (!(param instanceof IAutoValidated)) continue;

            IAutoValidated auto = (IAutoValidated) param;
            auto.autoValidate(executor, targets, result.getParamValue(param.getKey()));
        }
    }
}
