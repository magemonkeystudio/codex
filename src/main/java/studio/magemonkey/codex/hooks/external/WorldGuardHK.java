package studio.magemonkey.codex.hooks.external;

import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.hooks.HookState;
import studio.magemonkey.codex.hooks.NHook;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class WorldGuardHK extends NHook<CodexEngine> {

    private WorldGuard worldGuard;

    public WorldGuardHK(@NotNull CodexEngine plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public HookState setup() {
        this.worldGuard = WorldGuard.getInstance();
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {

    }

    public boolean canFights(@NotNull Entity damager, @NotNull Entity victim) {
        return WorldGuardPlugin.inst().createProtectionQuery().testEntityDamage(damager, victim);
    }

    public boolean isInRegion(@NotNull Entity entity, @NotNull String region) {
        return this.getRegion(entity).equalsIgnoreCase(region);
    }

    @NotNull
    public String getRegion(@NotNull Entity entity) {
        return this.getRegion(entity.getLocation());
    }

    @NotNull
    public String getRegion(@NotNull Location loc) {
        ProtectedRegion region = this.getProtectedRegion(loc);
        return region == null ? "" : region.getId();
    }

    @Nullable
    public ProtectedRegion getProtectedRegion(@NotNull Entity entity) {
        return this.getProtectedRegion(entity.getLocation());
    }

    @Nullable
    public ProtectedRegion getProtectedRegion(@NotNull Location loc) {
        com.sk89q.worldedit.world.World world         = BukkitAdapter.adapt(loc.getWorld());
        BlockVector3                    vector3       = BukkitAdapter.adapt(loc).toVector().toBlockPoint();
        RegionManager                   regionManager = worldGuard.getPlatform().getRegionContainer().get(world);

        ApplicableRegionSet set = regionManager.getApplicableRegions(vector3);

        ProtectedRegion region   = null;
        int             priority = -1;
        for (ProtectedRegion pr : set) {
            if (pr.getPriority() > priority) {
                priority = pr.getPriority();
                region = pr;
            }
        }
        return region;
    }

    @NotNull
    public Collection<ProtectedRegion> getProtectedRegions(@NotNull World w) {
        com.sk89q.worldedit.world.World world         = BukkitAdapter.adapt(w);
        RegionManager                   regionManager = worldGuard.getPlatform().getRegionContainer().get(world);

        return regionManager.getRegions().values();
    }
}
