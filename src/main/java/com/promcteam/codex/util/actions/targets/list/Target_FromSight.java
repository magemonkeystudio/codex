package com.promcteam.codex.util.actions.targets.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.util.actions.params.IParamResult;
import com.promcteam.codex.util.actions.params.IParamType;
import com.promcteam.codex.util.actions.targets.ITargetSelector;
import com.promcteam.codex.util.actions.targets.ITargetType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Target_FromSight extends ITargetSelector {

    public Target_FromSight(@NotNull CodexPlugin<?> plugin) {
        super(plugin, ITargetType.FROM_SIGHT);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_TargetSelector_FromSight_Desc.asList();
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

        Location start = exe.getLocation();
        if (exe instanceof LivingEntity) {
            start = ((LivingEntity) exe).getEyeLocation();
        }

        Vector      increase   = start.getDirection();
        Set<Entity> disTargets = new HashSet<>();

        for (int counter = 0; counter < dist; counter++) {
            Location point = start.add(increase);

            Material wall = point.getBlock().getType();
            if (wall != Material.AIR && wall.isSolid()) {
                break;
            }

            disTargets.addAll(exe.getWorld().getNearbyEntities(point, 1.25, 1.25, 1.25));
        }

        targets.addAll(disTargets); // Add all targets from this selector
    }

}
