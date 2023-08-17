package mc.promcteam.engine.manager.api.menu;

import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.manager.IManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;

public class MenuManager extends IManager<NexEngine> {

    public MenuManager(NexEngine plugin) {
        super(plugin);
    }

    @Override
    public void setup() {
        this.registerListeners();
    }

    @Override
    public void shutdown() {
        this.unregisterListeners();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) {
            return;
        }

        InventoryView view           = event.getView();
        Inventory     otherInventory = view.getTopInventory() == inventory
                ? view.getBottomInventory()
                : view.getTopInventory();
        if (otherInventory.getHolder() instanceof Menu && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            return;
        }

        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof Menu) {
            event.setCancelled(true);
            Slot slot = ((Menu) holder).getSlot(event.getSlot());
            if (slot != null) {
                switch (event.getClick()) {
                    case LEFT -> slot.onLeftClick();
                    case SHIFT_LEFT -> slot.onShiftLeftClick();
                    case RIGHT -> slot.onRightClick();
                    case SHIFT_RIGHT -> slot.onShiftRightClick();
                    case MIDDLE -> slot.onMiddleClick();
                    case NUMBER_KEY -> slot.onNumberClick(event.getHotbarButton());
                    case DOUBLE_CLICK -> slot.onDoubleClick();
                    case DROP -> slot.onDrop();
                    case CONTROL_DROP -> slot.onControlDrop();
                    case SWAP_OFFHAND -> slot.onSwapOffhand();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryView view = event.getView();
        for (Integer rawSlot : event.getRawSlots()) {
            Inventory inventory = view.getInventory(rawSlot);
            if (inventory != null && inventory.getHolder() instanceof Menu) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Menu) {
            Menu menu = (Menu) holder;
            if (!menu.isOpening()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        menu.onClose();
                    }
                }.runTask(NexEngine.get());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Menu openMenu = Menu.getOpenMenu(event.getPlayer());
        if (openMenu != null) {openMenu.onClose();}
    }
}
