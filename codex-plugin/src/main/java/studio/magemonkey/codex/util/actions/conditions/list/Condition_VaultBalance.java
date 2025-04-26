package studio.magemonkey.codex.util.actions.conditions.list;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.hooks.external.VaultHK;
import studio.magemonkey.codex.util.actions.conditions.IConditionType;
import studio.magemonkey.codex.util.actions.conditions.IConditionValidator;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import studio.magemonkey.codex.util.actions.params.IParamValue;
import studio.magemonkey.codex.util.actions.params.IParamValue.IOperator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Condition_VaultBalance extends IConditionValidator {

    private VaultHK vault;

    public Condition_VaultBalance(@NotNull CodexPlugin<?> plugin) {
        super(plugin, IConditionType.VAULT_BALANCE);
        this.vault = plugin.getVault();
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Codex_Editor_Actions_Condition_VaultBalance_Desc.asList();
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
    @Nullable
    protected Predicate<Entity> validate(
            @NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {

        if (vault == null) return null;

        IParamValue val    = result.getParamValue(IParamType.AMOUNT);
        double      amount = val.getDouble(-1);
        if (amount == -1) return null;

        IOperator oper = val.getOperator();

        return target -> {
            if (target.getType() == EntityType.PLAYER) {
                Player p       = (Player) target;
                double balance = vault.getBalance(p);
                return oper.check(balance, amount);
            }
            return false;
        };
    }
}
