package studio.magemonkey.codex.util.actions.actions.list;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.api.NMSProvider;
import studio.magemonkey.codex.util.EntityUT;
import studio.magemonkey.codex.util.actions.actions.IActionExecutor;
import studio.magemonkey.codex.util.actions.actions.IActionType;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import studio.magemonkey.codex.util.actions.params.IParamValue;

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
            double       maxHp        = EntityUT.getAttribute(livingEntity, NMSProvider.getNms().getAttribute("MAX_HEALTH"));

            if (percent) {
                hp2 = maxHp * (hp / 100D);
            }

            livingEntity.setHealth(Math.min(livingEntity.getHealth() + hp2, maxHp));
        });
    }
}
