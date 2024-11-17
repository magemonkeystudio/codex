package studio.magemonkey.codex.util.actions.actions.list;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.EffectUT;
import studio.magemonkey.codex.util.actions.actions.IActionExecutor;
import studio.magemonkey.codex.util.actions.actions.IActionType;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;

import java.util.List;
import java.util.Set;

public class Action_ParticleSimple extends IActionExecutor {

    public Action_ParticleSimple(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IActionType.PARTICLE_SIMPLE);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Action_ParticleSimple_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.NAME);
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.AMOUNT);
        this.registerParam(IParamType.SPEED);
        this.registerParam(IParamType.OFFSET);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        String name = result.getParamValue(IParamType.NAME).getString(null);
        if (name == null) return;

        double[] offset = result.getParamValue(IParamType.OFFSET).getDoubleArray();

        int amount = result.getParamValue(IParamType.AMOUNT).getInt(30);

        float speed = (float) result.getParamValue(IParamType.SPEED).getDouble(0.1);

        for (Entity e : targets) {
            Location loc;
            if (e instanceof LivingEntity) {
                loc = ((LivingEntity) e).getEyeLocation();
            } else loc = e.getLocation();

            EffectUT.playEffect(
                    loc, name,
                    (float) offset[0], (float) offset[1], (float) offset[2],
                    speed, amount);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
