package com.promcteam.risecore.listeners;

import com.promcteam.risecore.Core;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

public class BoatListener implements Listener {

    @EventHandler
    public void exit(VehicleExitEvent event) {
        if (!Core.getInstance().getConfig().getBoolean("removeBoatOnExit"))
            return;

        if (event.getVehicle().getType() == EntityType.BOAT) {

            if (!(event.getExited() instanceof Player))
                return;

            Player player = (Player) event.getExited();
            Boat   boat   = (Boat) event.getVehicle();

            if (!player.getInventory().addItem(new ItemStack(treeToMat(boat.getWoodType()))).isEmpty())
                player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(treeToMat(boat.getWoodType())));
            boat.remove();
        }
    }

    // 1.12 version for getting boat materials.
/*    private Material treeToMat(TreeSpecies type) {
        switch (type) {
            case ACACIA:
                return Material.BOAT_ACACIA;
            case BIRCH:
                return Material.BOAT_BIRCH;
            case DARK_OAK:
                return Material.BOAT_DARK_OAK;
            case JUNGLE:
                return Material.BOAT_JUNGLE;
            case REDWOOD:
                return Material.BOAT_SPRUCE;
            default:
                return Material.BOAT;

        }
    }*/

    // Used in 1.13 and higher to get tree material from Boat type
    private Material treeToMat(TreeSpecies type) {
        switch (type) {
            case ACACIA:
                return Material.ACACIA_BOAT;
            case BIRCH:
                return Material.BIRCH_BOAT;
            case DARK_OAK:
                return Material.DARK_OAK_BOAT;
            case JUNGLE:
                return Material.JUNGLE_BOAT;
            case REDWOOD:
                return Material.SPRUCE_BOAT;
            default:
                return Material.OAK_BOAT;

        }
    }

}
