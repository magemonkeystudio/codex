package studio.magemonkey.codex.listeners;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.compat.VersionManager;

public class BoatListener implements Listener {
    @EventHandler
    public void exit(VehicleExitEvent event) {
        if (!CodexEngine.get().cfg().getJYML().getBoolean("removeBoatOnExit")) return;
        if (!(event.getVehicle() instanceof Boat)) return;
        if (!(event.getExited() instanceof Player)) return;

        Player player = (Player) event.getExited();
        Boat   boat   = (Boat) event.getVehicle();

        ItemStack item = new ItemStack(VersionManager.getNms().getMaterial(boat));
        if (!player.getInventory().addItem(item).isEmpty())
            player.getWorld()
                    .dropItemNaturally(player.getLocation(), item);
        boat.remove();
    }
}
