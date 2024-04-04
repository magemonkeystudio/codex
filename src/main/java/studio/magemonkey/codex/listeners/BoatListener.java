package studio.magemonkey.codex.listeners;

import studio.magemonkey.codex.CodexEngine;
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
        if (!CodexEngine.get().cfg().getJYML().getBoolean("removeBoatOnExit")) return;
        if (event.getVehicle().getType() != EntityType.BOAT) return;
        if (!(event.getExited() instanceof Player)) return;

        Player player = (Player) event.getExited();
        Boat   boat   = (Boat) event.getVehicle();

        if (!player.getInventory().addItem(new ItemStack(boat.getBoatType().getMaterial())).isEmpty())
            player.getWorld()
                    .dropItemNaturally(player.getLocation(), new ItemStack(boat.getBoatType().getMaterial()));
        boat.remove();
    }
}
