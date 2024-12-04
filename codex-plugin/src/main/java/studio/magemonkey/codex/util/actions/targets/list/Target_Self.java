package studio.magemonkey.codex.util.actions.targets.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.targets.ITargetSelector;
import studio.magemonkey.codex.util.actions.targets.ITargetType;

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
        return plugin.lang().Codex_Editor_Actions_TargetSelector_Self_Desc.asList();
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
