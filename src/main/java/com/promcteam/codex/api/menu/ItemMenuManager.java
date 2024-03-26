package com.promcteam.codex.api.menu;

import com.promcteam.codex.CodexEngine;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMenuManager implements Listener {

    private static final List<ItemMenu>  menus = new ArrayList<>();
    private static       ItemMenuManager inst;


    public static void init() {
        if (inst != null) return;

        CodexEngine plugin = CodexEngine.get();
        inst = new ItemMenuManager();
        plugin.getPluginManager().registerEvents(inst, plugin);
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
