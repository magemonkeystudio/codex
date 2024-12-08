package studio.magemonkey.codex.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.Codex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LocUT {

    @Nullable
    public static String serialize(@NotNull Location loc) {
        World world = loc.getWorld();
        if (world == null) return null;

        return loc.getX() + "," + loc.getY() + "," +
                loc.getZ() + "," + loc.getPitch() + "," +
                loc.getYaw() + "," + world.getName();
    }

    @NotNull
    public static List<String> serialize(@NotNull List<Location> list) {
        return list.stream().map(LocUT::serialize).collect(Collectors.toList());
    }

    @Nullable
    public static Location deserialize(@NotNull String raw) {
        String[] split = raw.split(",");
        if (split.length != 6) return null;

        World world = Bukkit.getWorld(split[5]);
        if (world == null) {
            Codex.error("Invalid/Unloaded world for: '" + raw + "' location!");
            return null;
        }

        double x     = StringUT.getDouble(split[0], 0, true);
        double y     = StringUT.getDouble(split[1], 0, true);
        double z     = StringUT.getDouble(split[2], 0, true);
        float  pitch = (float) StringUT.getDouble(split[3], 0, true);
        float  yaw   = (float) StringUT.getDouble(split[4], 0, true);

        return new Location(world, x, y, z, yaw, pitch);
    }

    @NotNull
    public static List<Location> deserialize(@NotNull List<String> list) {
        List<Location> locations = new ArrayList<>();
        list.forEach(raw -> {
            Location loc = deserialize(raw);
            if (loc != null) {
                locations.add(loc);
            }
        });
        return locations;
    }

    @NotNull
    public static Location getFirstGroundBlock(@NotNull Location loc) {
        return getFirstGroundBlock(loc, false);
    }

    @NotNull
    public static Location getFirstGroundBlock(@NotNull Location loc, boolean airOnly) {
        float yaw   = loc.getYaw();
        float pitch = loc.getPitch();

        int minY = loc.getWorld() != null ? loc.getWorld().getMinHeight() : 0;

        Block under = loc.getBlock();
        while ((under.isEmpty()
                || ((airOnly && under.getType().isAir()) || (!airOnly && !under.getType().isSolid())))
                && under.getY() > minY) {
            under = under.getRelative(BlockFace.DOWN);
        }

        loc = under.getRelative(BlockFace.UP).getLocation();
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        return loc;
    }

    @NotNull
    public static Location getCenter(@NotNull Location loc, boolean vert) {
        float yaw   = loc.getYaw();
        float pitch = loc.getPitch();

        loc = new Location(loc.getWorld(),
                getRelativeCoord(loc.getBlockX()),
                vert ? getRelativeCoord(loc.getBlockY()) : loc.getBlockY(),
                getRelativeCoord(loc.getBlockZ()));

        loc.setYaw(yaw);
        loc.setPitch(pitch);
        return loc;
    }

    @NotNull
    public static Location getCenter(@NotNull Location loc) {
        return getCenter(loc, true);
    }

    private static double getRelativeCoord(double cord) {
        return cord + 0.5;
    }

    @NotNull
    public static Location getPointOnCircle(@NotNull Location loc, boolean b, double n, double n2, double n3) {
        return (b ? loc.clone() : loc).add(Math.cos(n) * n2, n3, Math.sin(n) * n2);
    }

    @NotNull
    public static Location getPointOnCircle(@NotNull Location loc, double n, double n2, double n3) {
        return getPointOnCircle(loc, true, n, n2, n3);
    }

    @Nullable
    public static BlockFace getDirection(@NotNull Entity e) {
        float n = e.getLocation().getYaw();
        n = n / 90.0F;
        n = (float) Math.round(n);
        if ((n == -4.0F) || (n == 0.0F) || (n == 4.0F)) {
            return BlockFace.SOUTH;
        }
        if ((n == -1.0F) || (n == 3.0F)) {
            return BlockFace.EAST;
        }
        if ((n == -2.0F) || (n == 2.0F)) {
            return BlockFace.NORTH;
        }
        if ((n == -3.0F) || (n == 1.0F)) {
            return BlockFace.WEST;
        }
        return null;
    }

    @NotNull
    public static Vector getDirectionTo(@NotNull Location from, @NotNull Location to) {
        Location origin = from.clone();
        Vector   target = to.clone().toVector();
        origin.setDirection(target.subtract(origin.toVector()));

        return origin.getDirection();
    }

    @NotNull
    public static List<String> getWorldNames() {
        return Bukkit.getWorlds().stream().map(World::getName)
                .collect(Collectors.toList());
    }
}
