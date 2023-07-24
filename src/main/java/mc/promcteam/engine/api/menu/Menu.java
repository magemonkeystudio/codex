package mc.promcteam.engine.api.menu;

import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.utils.ItemUT;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Menu implements InventoryHolder { // TODO close if another menu of the same class opens
    protected final Player                 player;
    protected final String                 title;
    protected final int                    rows;
    protected       Inventory              inventory;
    protected final TreeMap<Integer, Slot> slots       = new TreeMap<>();
    private final   Set<Listener>          listeners   = new HashSet<>();
    private         int                    page        = 0;
    private         Runnable               onClose;
    private         boolean                opening     = false;
    private         boolean                fakeClosing = false;

    public Menu(Player player, int rows, String title) {
        this.player = player;
        this.title = title;
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
        }.runTask(NexEngine.get());
    }

    public void open() {open(this.page);}

    public void open(int page) {
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

    private void setOnClose(Menu menu, int page) {
        this.onClose = () -> menu.open(page);
    }

    public void openSubMenu(Menu menu) {
        menu.setOnClose(this, page);
        this.opening = true;
        menu.open();
        this.opening = false;
    }

    public boolean isOpening() {return opening;}

    public void onClose() {
        if (this.fakeClosing) {
            this.fakeClosing = false;
            return;
        }
        for (Listener listener : this.listeners) {HandlerList.unregisterAll(listener);}
        if (this.onClose != null) {this.onClose.run();}
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
        Bukkit.getPluginManager().registerEvents(listener, NexEngine.get());
        this.listeners.add(listener);
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
