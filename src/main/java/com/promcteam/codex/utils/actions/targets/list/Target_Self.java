package com.promcteam.codex.utils.actions.targets.list;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.utils.actions.params.IParamResult;
import com.promcteam.codex.utils.actions.targets.ITargetSelector;
import com.promcteam.codex.utils.actions.targets.ITargetType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Target_Self extends ITargetSelector {

    public Target_Self(@NotNull CodexPlugin<?> plugin) {
        super(plugin, ITargetType.SELF);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_TargetSelector_Self_Desc.asList();
    }

    @Override
    public void registerParams() {

    }

    @Override
    protected void validateTarget(Entity exe, Set<Entity> targets, IParamResult result) {
        Set<Entity> disTargets = new HashSet<>();
        disTargets.add(exe);

        targets.addAll(disTargets); // Add all targets from this selector
    }
}
