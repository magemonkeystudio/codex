package mc.promcteam.engine.utils.actions.actions.list;

import mc.promcteam.engine.NexPlugin;
import mc.promcteam.engine.utils.actions.actions.IActionExecutor;
import mc.promcteam.engine.utils.actions.actions.IActionType;
import mc.promcteam.engine.utils.actions.params.IParamResult;
import mc.promcteam.engine.utils.actions.params.IParamType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class Action_Potion extends IActionExecutor {

    public Action_Potion(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.POTION);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Potion_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.NAME);
        this.registerParam(IParamType.DURATION);
        this.registerParam(IParamType.AMOUNT);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        String name = result.getParamValue(IParamType.NAME).getString(null);
        if (name == null) return;

        PotionEffectType pet = PotionEffectType.getByName(name.toUpperCase());
        if (pet == null) return;

        int dura = result.getParamValue(IParamType.DURATION).getInt(0);
        if (dura <= 0) return;

        int amplifier = Math.max(0, result.getParamValue(IParamType.AMOUNT).getInt(0) - 1);

        PotionEffect effect = new PotionEffect(pet, dura, amplifier);

        for (Entity e : targets) {
            if (!(e instanceof LivingEntity)) continue;
            LivingEntity li = (LivingEntity) e;
            li.addPotionEffect(effect);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
