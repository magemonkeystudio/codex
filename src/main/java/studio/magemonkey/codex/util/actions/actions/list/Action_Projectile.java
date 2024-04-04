package studio.magemonkey.codex.util.actions.actions.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.actions.actions.IActionExecutor;
import studio.magemonkey.codex.util.actions.actions.IActionType;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Projectile extends IActionExecutor {

    public Action_Projectile(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.PROJECTILE);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_Projectile_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.NAME);
        this.registerParam(IParamType.SPEED);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        if (!(exe instanceof LivingEntity)) return;
        LivingEntity executor = (LivingEntity) exe;

        String clasName = result.getParamValue(IParamType.NAME).getString(null);
        if (clasName == null) return;

        EntityType pj;
        try {
            pj = EntityType.valueOf(clasName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return;
        }

        double speed = result.getParamValue(IParamType.SPEED).getDouble(3.5D);

        Location eye = executor.getEyeLocation().clone();
        if (!targets.isEmpty()) {
            for (Entity eTarget : targets) {
                Entity e = exe.getWorld().spawnEntity(eye.clone().add(eye.getDirection()), pj);
                if (!(e instanceof Projectile)) {
                    e.remove();
                    return;
                }

                Location to = eTarget.getLocation();
                if (eTarget instanceof LivingEntity) {
                    to = ((LivingEntity) eTarget).getEyeLocation();
                }

                Vector target = new Location(to.getWorld(), to.getX(), to.getY(), to.getZ()).toVector();
                eye.setDirection(target.subtract(eye.toVector()));
                Vector increase = eye.getDirection();

                Projectile arrow = (Projectile) e;
                arrow.setShooter(executor);
                arrow.setBounce(true);
                arrow.setVelocity(increase.multiply(speed));

                // TODO ProjectileStats.setPickable(arrow, false);
            }
        } else {
            Entity e = exe.getWorld().spawnEntity(eye.clone().add(eye.getDirection()), pj);
            if (!(e instanceof Projectile)) {
                e.remove();
                return;
            }

            Projectile arrow = (Projectile) e;
            arrow.setShooter(executor);
            arrow.setBounce(true);
            arrow.setVelocity(eye.getDirection().multiply(speed));

            // TODO ProjectileStats.setPickable(arrow, false);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }

}
