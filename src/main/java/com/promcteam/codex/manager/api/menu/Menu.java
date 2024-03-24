package com.promcteam.codex.manager.api.menu;

import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.utils.ItemUT;
import com.promcteam.codex.utils.StringUT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
public abstract class Menu implements InventoryHolder {
    private static final Map<Player, Menu> ACTIVE_MENUS = new HashMap<>();

    @Nullable
    public static Menu getOpenMenu(Player player) {return ACTIVE_MENUS.get(player);}

    protected final Player                 player;
    protected final String                 title;
    protected final int                    rows;
    protected       Inventory              inventory;
    protected final TreeMap<Integer, Slot> slots       = new TreeMap<>();
    private final   Set<Listener>          listeners   = new HashSet<>();
    private final   Set<BukkitTask>        tasks       = new HashSet<>();
    private         int                    page        = 0;
    protected       Menu                   parentMenu;
    protected       boolean                opening     = false;
    protected       boolean                fakeClosing = false;

    public Menu(Player player, int rows, String title) {
        this.player = player;
        this.title = StringUT.color(title);
        this.rows = rows;
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    public Player getPlayer() {return player;}

    @Override
    @NotNull
    public Inventory getInventory() {return inventory;}

    public int getPages() {
        try {
            return (slots.lastKey() - 1) / this.inventory.getSize() + 1;
        } catch (NoSuchElementException e) {
            return 1;
        }
    }

    public int getPage() {return page;}

    public void setSlot(int i, @Nullable Slot slot) {
        if (slot == null) {
            slots.remove(i);
        } else {
            slots.put(i, slot);
            slot.setMenu(i, this);
        }
    }

    public abstract void setContents();

    @Nullable
    public Slot getSlot(int i) {
        return slots.get(i);
    }

    public void openSync() {
        new BukkitRunnable() {
            @Override
            public void run() {open();}
        }.runTask(CodexEngine.get());
    }

    public void open() {open(this.page);}

    public void open(int page) {
        Menu oldMenu = ACTIVE_MENUS.get(this.player);
        if (oldMenu != null && oldMenu != this && !oldMenu.isOpening()) {
            oldMenu.onClose();
        }
        ACTIVE_MENUS.put(this.player, this);

        slots.clear();
        setContents();
        int finalPage = page % getPages();
        inventory = Bukkit.createInventory(Menu.this, rows * 9, title
                .replace("%page%", String.valueOf(finalPage + 1))
                .replace("%pages%", String.valueOf(getPages())));
        for (int i = 0, last = Menu.this.inventory.getSize(); i < last; i++) {
            Slot slot = slots.get(finalPage * Menu.this.inventory.getSize() + i);
            if (slot != null) {inventory.setItem(i, slot.getItemStack());}
        }
        Menu.this.opening = true;
        player.openInventory(inventory);
        Menu.this.opening = false;
        Menu.this.page = finalPage;

    }

    public void openSubMenu(Menu menu) {
        menu.parentMenu = this;
        this.opening = true;
        menu.open();
        this.opening = false;
    }

    public boolean isOpening() {return opening;}

    public void close() {
        close(1);
    }

    public void close(int layers) {
        for (int i = 1; i <= layers; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.closeInventory();
                }
            }.runTaskLater(CodexEngine.get(), i);
        }
    }

    public void onClose() {
        if (this.fakeClosing) {
            this.fakeClosing = false;
            return;
        }
        for (Listener listener : this.listeners) {HandlerList.unregisterAll(listener);}
        this.listeners.clear();
        for (BukkitTask task : this.tasks) {if (!task.isCancelled()) {task.cancel();}}
        this.tasks.clear();
        ACTIVE_MENUS.remove(this.player);
        if (this.parentMenu != null && this.player.isOnline()) {
            this.parentMenu.open();
        }
    }

    public void fakeClose() {
        this.fakeClosing = true;
        this.player.closeInventory();
    }

    protected Slot getPrevButton() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        ItemUT.addSkullTexture(itemStack, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWE0YTliNzBhMjVhMjdkODE4OWU2MGQyN2VhOGNjOTYzMmMzNmI0NjkyODE1NWRlNzc1NWYzNjZlZjA0Yzg3NyJ9fX0=");
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RESET + "Previous Page");
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "Current Page: " + ChatColor.WHITE + getPages());
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return new Slot(itemStack) {
            public void onLeftClick() {
                this.menu.open(this.menu.getPage() + 1);
            }
        };
    }

    protected Slot getNextButton() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        ItemUT.addSkullTexture(itemStack, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTkxNTJjMmU5MWY0NzA0ODViZTIyMmRiNWQyYTg5NWNhZGM5MDMzMjZmNWM2NzFiZjhhNTU5MTQ5NjczYmU4MCJ9fX0=");
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RESET + "Next Page");
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "Current Page: " + ChatColor.WHITE + getPages());
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return new Slot(itemStack) {
            public void onLeftClick() {
                this.menu.open(this.menu.getPage() + 1);
            }
        };
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, CodexEngine.get());
        this.listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        if (this.listeners.remove(listener)) {HandlerList.unregisterAll(listener);}
    }

    public void registerTask(BukkitTask task) {this.tasks.add(task);}

    public void unregisterTask(BukkitTask task) {
        if (this.tasks.remove(task) && !task.isCancelled()) {task.cancel();}
    }

    public static class PreviousPageButton extends Slot {

        public PreviousPageButton(ItemStack itemStack) {super(itemStack);}

        @Override
        public void onLeftClick() {this.menu.open(this.menu.getPage() - 1);}
    }

    public static class NextPageButton extends Slot {

        public NextPageButton(ItemStack itemStack) {super(itemStack);}

        @Override
        public void onLeftClick() {this.menu.open(this.menu.getPage() + 1);}
    }
}
