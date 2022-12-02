package mc.promcteam.engine.utils.actions.conditions.list;

import mc.promcteam.engine.NexPlugin;
import mc.promcteam.engine.hooks.external.VaultHK;
import mc.promcteam.engine.utils.actions.conditions.IConditionType;
import mc.promcteam.engine.utils.actions.conditions.IConditionValidator;
import mc.promcteam.engine.utils.actions.params.IParamResult;
import mc.promcteam.engine.utils.actions.params.IParamType;
import mc.promcteam.engine.utils.actions.params.IParamValue;
import mc.promcteam.engine.utils.actions.params.IParamValue.IOperator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Condition_VaultBalance extends IConditionValidator {

    private VaultHK vault;

    public Condition_VaultBalance(@NotNull NexPlugin<?> plugin) {
        super(plugin, IConditionType.VAULT_BALANCE);
        this.vault = plugin.getVault();
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Condition_VaultBalance_Desc.asList();
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
