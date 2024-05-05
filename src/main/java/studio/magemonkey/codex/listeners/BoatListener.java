package studio.magemonkey.codex.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.codex.CodexEngine;

public class BoatListener implements Listener {
    @EventHandler
    public void exit(VehicleExitEvent event) {
        if (!CodexEngine.get().cfg().getJYML().getBoolean("removeBoatOnExit")) return;
        if (event.getVehicle().getType() != EntityType.BOAT) return;
        if (!(event.getExited() instanceof Player)) return;

        Player player = (Player) event.getExited();
        Boat   boat   = (Boat) event.getVehicle();

        ItemStack item    = new ItemStack(getMaterial(boat));
        if (!player.getInventory().addItem(item).isEmpty())
            player.getWorld()
                    .dropItemNaturally(player.getLocation(), item);
        boat.remove();
    }

    private Material getMaterial(Boat boat) {
        try {
            return boat.getBoatType().getMaterial();
        } catch (NoSuchMethodError e) {
            String woodName = boat.getWoodType().name();
            // Should `getWoodType` be removed in later versions, this Reflection should work
            // String woodName = "OAK";
            // try {
            //     Method  getWoodTypeMethod = boat.getClass().getMethod("getWoodType");
            //     Enum<?> woodType          = (Enum<?>) getWoodTypeMethod.invoke(boat);
            //     woodName = woodType.name();
            // } catch (NoSuchMethodError | NoSuchMethodException | InvocationTargetException | IllegalAccessException be) {
            //     CodexEngine.get().getLogger().warning("Failed to get wood type of boat, defaulting to OAK");
            // }

            return switch (woodName) {
                case "REDWOOD" -> Material.SPRUCE_BOAT;
                case "BIRCH" -> Material.BIRCH_BOAT;
                case "JUNGLE" -> Material.JUNGLE_BOAT;
                case "ACACIA" -> Material.ACACIA_BOAT;
                case "DARK_OAK" -> Material.DARK_OAK_BOAT;
                default -> Material.OAK_BOAT;
            };
        }
    }
}
