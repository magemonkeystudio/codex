package studio.magemonkey.codex.util;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.random.Rnd;

import java.util.Locale;

public class EffectUT {

    public static void playEffect(
            @NotNull Location loc,
            @NotNull String eff,
            double x,
            double y,
            double z,
            double speed,
            int amount) {

        World world = loc.getWorld();
        if (world == null) return;

        String[] nameSplit    = eff.split(":");
        String   particleName = nameSplit[0];
        String   particleData = nameSplit.length >= 2 ? nameSplit[1].toUpperCase() : null;

        Particle particle = CollectionsUT.getEnum(particleName, Particle.class);
        if (particle == null) return;

        String partName = particle.name().toLowerCase(Locale.US);
        if (partName.equals("dust") || partName.equals("redstone") || partName.equals("redstone_dust")) {
            Color color = Color.WHITE;
            if (particleData != null) {
                String[] pColor = particleData.split(",");
                int      r      = StringUT.getInteger(pColor[0], Rnd.get(255));
                int      g      = pColor.length >= 2 ? StringUT.getInteger(pColor[1], Rnd.get(255)) : 0;
                int      b      = pColor.length >= 3 ? StringUT.getInteger(pColor[2], Rnd.get(255)) : 0;
                color = Color.fromRGB(r, g, b);
            }

            Particle.DustOptions data = new Particle.DustOptions(color, 1.5f);
            try {
                world.spawnParticle(particle, loc, amount, x, y, z, speed, data, true);
            } catch (NoSuchMethodError e) {
                world.spawnParticle(particle, loc, amount, x, y, z, data);
            }
            return;
        }

        if (partName.equals("block") || partName.equals("block_crack")) {
            Material  m         = particleData != null ? Material.getMaterial(particleData) : Material.STONE;
            BlockData blockData = m != null ? m.createBlockData() : Material.STONE.createBlockData();
            try {
                world.spawnParticle(particle, loc, amount, x, y, z, speed, blockData, true);
            } catch (NoSuchMethodError e) {
                world.spawnParticle(particle, loc, amount, x, y, z, speed, blockData);
            }
            return;
        }

        if (partName.equals("item") || partName.equals("item_crack")) { // ITEM_CRACK/ITEM
            Material  m    = particleData != null ? Material.getMaterial(particleData) : Material.STONE;
            ItemStack item = m != null ? new ItemStack(m) : new ItemStack(Material.STONE);
            try {
                world.spawnParticle(particle, loc, amount, x, y, z, speed, item, true);
            } catch (NoSuchMethodError e) {
                world.spawnParticle(particle, loc, amount, x, y, z, speed, item);
            }
            return;
        }

        try {
            world.spawnParticle(particle, loc, amount, x, y, z, speed, null, true);
        } catch (NoSuchMethodError e) {
            world.spawnParticle(particle, loc, amount, x, y, z, speed);
        }
    }

    public static void drawLine(Location from,
                                Location to,
                                String pe,
                                float offX,
                                float offY,
                                float offZ,
                                float speed,
                                int amount) {
        Location origin = from.clone();
        Vector   target = new Location(to.getWorld(), to.getX(), to.getY(), to.getZ()).toVector();
        origin.setDirection(target.subtract(origin.toVector()));
        Vector increase = origin.getDirection();

        for (int counter = 0; counter < from.distance(to); counter++) {
            Location loc = origin.add(increase);
            EffectUT.playEffect(loc, pe, offX, offY, offZ, speed, 5);
        }
    }

}
