package studio.magemonkey.codex.hooks.external;

import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.hooks.HookState;
import studio.magemonkey.codex.hooks.NHook;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MythicMobsHK extends NHook<CodexEngine> implements IMythicHook {

    private MythicMobs mm;

    public MythicMobsHK(CodexEngine plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        this.mm = MythicMobs.inst();
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isMythicMob(@NotNull Entity e) {
        return mm.getAPIHelper().isMythicMob(e);
    }

    @Override
    public String getMythicNameByEntity(@NotNull Entity e) {
        return mm.getAPIHelper().getMythicMobInstance(e).getType().getInternalName();
    }

    @Override
    public MythicMob getMythicInstance(@NotNull Entity e) {
        return mm.getAPIHelper().getMythicMobInstance(e).getType();
    }

    @Override
    public boolean isDropTable(@NotNull String table) {
        return mm.getDropManager().getDropTable(table) != null && MythicMobs.inst()
                .getDropManager()
                .getDropTable(table)
                .isPresent();
    }

    @Override
    public double getLevel(@NotNull Entity e) {
        return mm.getAPIHelper().getMythicMobInstance(e).getLevel();
    }

    @NotNull
    @Override
    public List<String> getMythicIds() {
        return new ArrayList<>(mm.getMobManager().getMobNames());
    }

    @Override
    public void setSkillDamage(@NotNull Entity e, double d) {
        if (!isMythicMob(e)) return;
        ActiveMob am1 = mm.getMobManager().getMythicMobInstance(e);
        am1.setLastDamageSkillAmount(d);
    }

    @Override
    public void castSkill(@NotNull Entity e, @NotNull String skill) {
        mm.getAPIHelper().castSkill(e, skill);
    }

    @Override
    public void killMythic(@NotNull Entity e) {
        if (!this.mm.getAPIHelper().getMythicMobInstance(e).isDead()) {
            this.mm.getAPIHelper().getMythicMobInstance(e).setDead();
            e.remove();
        }
    }

    @Override
    public boolean isValid(@NotNull String name) {
        MythicMob koke = this.mm.getAPIHelper().getMythicMob(name);
        return koke != null;
    }

    @NotNull
    @Override
    public String getName(@NotNull String mobId) {
        MythicMob koke = mm.getAPIHelper().getMythicMob(mobId);
        return koke != null ? koke.getDisplayName().get() : mobId;
    }

    @Nullable
    @Override
    public Entity spawnMythicMob(@NotNull String name, @NotNull Location loc, int level) {
        try {
            MythicMob koke = mm.getAPIHelper().getMythicMob(name);
            Entity    e    = mm.getAPIHelper().spawnMythicMob(koke, loc, level);
            //mm.getAPIHelper().getMythicMobInstance(e).setLevel(level);
            return e;
        } catch (InvalidMobTypeException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void taunt(LivingEntity target, LivingEntity source, double amount) {
        if (amount > 0) {
            MythicMobs.inst().getAPIHelper().addThreat(target, source, amount);
        } else if (amount < 0) {
            MythicMobs.inst().getAPIHelper().reduceThreat(target, source, -amount);
        }
    }
}
