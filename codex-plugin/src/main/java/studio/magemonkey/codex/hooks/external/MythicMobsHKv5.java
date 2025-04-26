package studio.magemonkey.codex.hooks.external;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.utils.serialize.Position;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillMetadataImpl;
import io.lumine.mythic.core.skills.SkillTriggers;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.hooks.HookState;
import studio.magemonkey.codex.hooks.NHook;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MythicMobsHKv5 extends NHook<CodexEngine> implements IMythicHook {

    private MythicBukkit mm;

    public MythicMobsHKv5(CodexEngine plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        this.mm = MythicBukkit.inst();
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isMythicMob(@NotNull Entity entity) {
        return getMythicInstance(entity) != null;
    }

    @Override
    @NotNull
    public String getMythicNameByEntity(@NotNull Entity entity) {
        MythicMob mob = getMythicInstance(entity);
        return mob == null ? null : mob.getInternalName();
    }

    public MythicMob getMythicInstance(@NotNull Entity entity) {
        ActiveMob mob = getActiveMythicInstance(entity);

        return mob != null ? mob.getType() : null;
    }

    @Override
    public boolean isDropTable(@NotNull String table) {
        return mm.getDropManager().getDropTable(table) != null && mm.getDropManager().getDropTable(table).isPresent();
    }

    @Override
    public double getLevel(@NotNull Entity e) {
        ActiveMob mob = getActiveMythicInstance(e);

        return mob != null ? mob.getLevel() : 1;
    }

    @NotNull
    @Override
    public List<String> getMythicIds() {
        return new ArrayList<>(mm.getMobManager().getMobNames());
    }

    @Override
    public void setSkillDamage(@NotNull Entity entity, double amount) {
        if (!isMythicMob(entity)) return;
        ActiveMob am1 = getActiveMythicInstance(entity);
        am1.setLastDamageSkillAmount(amount);
    }

    @Override
    public void castSkill(@NotNull Entity e, @NotNull String skill) {
        ActiveMob mob = getActiveMythicInstance(e);
        if (mob == null) return;

        mm.getSkillManager().getSkill(skill).ifPresent(sk -> {
            sk.execute(new SkillMetadataImpl(SkillTriggers.API, mob, mob.getEntity()));
        });
    }

    @Override
    public void killMythic(@NotNull Entity e) {
        ActiveMob mob = getActiveMythicInstance(e);
        if (mob == null || mob.isDead()) return;

        mob.setDead();
        e.remove();
    }

    @Override
    public boolean isValid(@NotNull String name) {
        Optional<MythicMob> koke = mm.getMobManager().getMythicMob(name);
        return koke.isPresent();
    }

    @NotNull
    @Override
    public String getName(@NotNull String mobId) {
        Optional<MythicMob> koke = mm.getMobManager().getMythicMob(mobId);
        return koke.isPresent() ? koke.get().getDisplayName().get() : mobId;
    }

    @Nullable
    @Override
    public Entity spawnMythicMob(@NotNull String name, @NotNull Location loc, int level) {
        Optional<MythicMob> koke = mm.getMobManager().getMythicMob(name);
        if (koke.isPresent()) {
            MythicMob mob = koke.get();
            ActiveMob e   = mob.spawn(new AbstractLocation(Position.of(loc)), level);

            return e.getEntity().getBukkitEntity();
        }
        return null;
    }

    @Override
    public void taunt(LivingEntity target, LivingEntity source, double amount) {
        AbstractEntity abs = BukkitAdapter.adapt(source);
        ActiveMob      mob = getActiveMythicInstance(target);
        if (!mob.hasThreatTable()) {
            if (amount > 0) mob.setTarget(abs);
            else if (amount < 0) mob.getNewTarget();
            return;
        }

        ActiveMob.ThreatTable table = mob.getThreatTable();
        if (amount > 0) table.threatGain(abs, amount);
        else if (amount < 0) table.threatLoss(abs, -amount);
    }

    public ActiveMob getActiveMythicInstance(@NotNull Entity entity) {
        Optional<ActiveMob> mob = mm.getMobManager().getActiveMob(entity.getUniqueId());

        return mob.isPresent() ? mob.get() : null;
    }
}
