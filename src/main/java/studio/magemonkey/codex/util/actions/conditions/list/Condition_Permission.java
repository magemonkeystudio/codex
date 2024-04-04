package studio.magemonkey.codex.util.actions.conditions.list;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.actions.conditions.IConditionType;
import studio.magemonkey.codex.util.actions.conditions.IConditionValidator;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Condition_Permission extends IConditionValidator {

    public Condition_Permission(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IConditionType.PERMISSION);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Condition_Permission_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.NAME);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    @Nullable
    protected Predicate<Entity> validate(
            @NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {

        String node = result.getParamValue(IParamType.NAME).getString(null);
        if (node == null) return null;

        boolean negative = node.startsWith("-");
        return target -> target.hasPermission(node) == !negative;
    }
}