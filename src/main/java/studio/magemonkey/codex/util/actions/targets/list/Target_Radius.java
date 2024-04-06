package studio.magemonkey.codex.util.actions.targets.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import studio.magemonkey.codex.util.actions.targets.ITargetSelector;
import studio.magemonkey.codex.util.actions.targets.ITargetType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Target_Radius extends ITargetSelector {

    public Target_Radius(@NotNull CodexPlugin<?> plugin) {
        super(plugin, ITargetType.RADIUS);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_TargetSelector_Radius_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.ALLOW_SELF);
        this.registerParam(IParamType.ATTACKABLE);
        this.registerParam(IParamType.DISTANCE);
    }

    @Override
    protected void validateTarget(Entity exe, Set<Entity> targets, IParamResult result) {
        double dist = -1;
        if (result.hasParam(IParamType.DISTANCE)) {
            dist = result.getParamValue(IParamType.DISTANCE).getDouble(0);
        } else return;

        if (dist <= 0) return;

        Set<Entity> disTargets = new HashSet<>();
        disTargets.addAll(exe.getNearbyEntities(dist, dist, dist));

        targets.addAll(disTargets); // Add all targets from this selector
    }
}
