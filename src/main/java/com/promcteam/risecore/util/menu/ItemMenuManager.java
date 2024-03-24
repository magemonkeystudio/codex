package com.promcteam.risecore.util.menu;

import com.promcteam.risecore.Core;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class ItemMenuManager implements Listener {

    private static final List<ItemMenu>  menus = new ArrayList<>();
    private static       ItemMenuManager inst;

    private ItemMenuManager() {
    }

    public static void init() {
        if (inst != null) return;

        inst = new ItemMenuManager();
        Bukkit.getPluginManager().registerEvents(inst, Core.getInstance());
    }

    public static ItemMenu create(int size, String title, boolean destroyOnExit) {
        ItemMenu menu = new ItemMenu(null, size, title)
                .destroyOnExit(destroyOnExit);
        menus.add(menu);
        return menu;
    }

    public static ItemMenu create(int size, String title) {
        return create(size, title, false);
    }

    protected static void destroy(ItemMenu menu) {
        menus.remove(menu);
    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();

        for (ItemMenu menu : menus) {
            if (inv.getHolder().equals(menu)) {
                menu.click(event);
            }
        }
    }

    @EventHandler
    public void drag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();

        for (ItemMenu menu : menus) {
            if (inv.getHolder().equals(menu)) {
                menu.drag(event);
            }
        }
    }

    @EventHandler
    public void exit(InventoryCloseEvent event) {
        Inventory      inv          = event.getInventory();
        List<ItemMenu> queueDestroy = new ArrayList<>();

        for (ItemMenu menu : menus) {
            if (inv.getHolder().equals(menu) && menu.isDestroyOnExit()) {
                queueDestroy.add(menu);
            }
        }
        queueDestroy.forEach(menu -> menu.destroy());
    }

}
