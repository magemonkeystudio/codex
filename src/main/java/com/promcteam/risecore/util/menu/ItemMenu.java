package com.promcteam.risecore.util.menu;

import com.promcteam.risecore.Debugger;
import com.promcteam.risecore.util.menu.replacements.MenuReplacement;
import com.promcteam.risecore.util.patterns.InventoryPattern;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.promcteam.risecore.util.Utils.isAir;

public class ItemMenu implements InventoryHolder {

    @Getter
    private final int    size;
    @Getter
    private       String title;

    private       Inventory inventory = null;
    @Getter
    private final UUID      ownerId;

    private ClickAction clickAction = (click) -> click.setCancelled(true);
    private ClickAction clickSelf   = null;
    private DragAction  dragAction  = (drag) -> drag.setCancelled(true);

    @Getter
    private boolean destroyOnExit = false;

    @Getter
    private InventoryPattern pattern;

    @Getter
    private final Map<Integer, MenuItem> items = new HashMap<>();

    @Getter
    private final List<MenuReplacement> textReplacements = new ArrayList<>();

    /**
     * ItemMenus should not be constructed using this method,
     * rather they should be constructed using
     * {@link ItemMenuManager#create(int, String)} and destroyed
     * after use to preserve memory.
     */
    protected ItemMenu(Player owner, int size, String title) {
        this.ownerId = owner.getUniqueId();
        this.size = size;
        this.title = title;
        textReplacements.addAll(MenuReplacement.DataBank.defaultReplacements);
    }

    public void setTitle(String title) {
        this.title = title;
        Inventory tempInv = Bukkit.createInventory(this, inventory.getSize(), title);
        tempInv.setContents(inventory.getContents());
        List<HumanEntity> viewers = inventory.getViewers();
        inventory = tempInv;
        viewers.stream().map(ent -> (Player) ent).forEach(this::open);
    }

    public ItemMenu onClick(ClickAction clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public ItemMenu onClickSelf(ClickAction clickAction) {
        this.clickSelf = clickAction;
        return this;
    }

    public ItemMenu onDrag(DragAction dragAction) {
        this.dragAction = dragAction;
        return this;
    }

    public ItemMenu pattern(InventoryPattern pattern) {
        this.pattern = pattern;
        pattern.apply(this);
        return this;
    }

    public ItemMenu destroyOnExit(boolean destroyOnExit) {
        this.destroyOnExit = destroyOnExit;
        return this;
    }

    protected void click(InventoryClickEvent event) {
        if (event.getClickedInventory().getHolder().equals(this)) {
            if (clickAction != null)
                clickAction.onClick(event);

            int slot = event.getRawSlot();
            if (items.containsKey(slot)) {
                MenuItem item = items.get(slot);
                if (item == null) return;

                MenuAction action = item.getAction();
                if (action == null) return;

                action.click((Player) event.getWhoClicked());
            }

        } else if (event.getClickedInventory().getHolder().equals(event.getWhoClicked()))
            if (clickSelf != null)
                clickSelf.onClick(event);
    }

    protected void drag(InventoryDragEvent event) {
        if (dragAction != null)
            dragAction.onDrag(event);
    }

    public Inventory build() {
        if (inventory != null) return inventory;

        if (size % 9 == 0)
            inventory = Bukkit.createInventory(this, size, title);
        else if (size == 5)
            inventory = Bukkit.createInventory(this, InventoryType.HOPPER, title);

        return inventory;
    }

    public void rebuild() {
        inventory.getViewers().forEach(e -> e.closeInventory());
        inventory = null;
        build();
    }

    public Inventory update() {
        build();
        inventory.clear();

        //Apply pattern
        pattern.apply(this);

        //Apply items
        items.forEach((key, value) -> {
            if (key < size)
                inventory.setItem(key, value.getItem());
            else
                Debugger.warn("There are more items then there are slots to put them in. Skipping item in slot " + key);
        });

        for (HumanEntity ent : inventory.getViewers()) {
            for (int i = 0; i < inventory.getContents().length; i++) {
                ItemStack item = inventory.getContents()[i];
                inventory.setItem(i, applyReplacements(item, (Player) ent));
            }
        }

        return inventory;
    }

    public void open(Player player) {
        if (inventory == null)
            build();

        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack item = inventory.getContents()[i];
            inventory.setItem(i, applyReplacements(item, player));
        }

        player.openInventory(inventory);
    }

    public void close() {
        inventory.getViewers().forEach(e -> e.closeInventory());
    }

    public void close(Player player) {
        Inventory top = player.getOpenInventory().getTopInventory();
        if (top != null && top.getHolder().equals(this))
            player.closeInventory();
    }

    public void destroy() {
        inventory.getViewers().forEach(e -> e.closeInventory());
        ItemMenuManager.destroy(this);
    }

    public void clear() {
        items.clear();
        update();
    }

    public void addItem(ItemStack item, MenuAction menuAction) {
        int slot = firstEmpty();
        setItem(slot, item, menuAction);
    }

    public void setItem(int slot, ItemStack item, MenuAction menuAction) {
        items.put(slot, new MenuItem(item, menuAction));
        getInventory().setItem(slot, item);
    }

    public void removeItem(int slot) {
        items.remove(slot);
        update();
    }

    public ItemStack applyReplacements(ItemStack item) {
        return this.applyReplacements(item, Bukkit.getPlayer(ownerId));
    }

    public ItemStack applyReplacements(ItemStack item, Player player) {
        for (MenuReplacement rep : textReplacements) {
            item = rep.replace(item, player);
        }

        return item;
    }

    public int firstEmpty() {
        build();
        return inventory.firstEmpty();
    }

    public int countOpen() {
        return (int) Arrays.stream(inventory.getContents())
                .filter(i -> i == null || isAir(i.getType())).count();
    }

    @Override
    public Inventory getInventory() {
        build();
        return inventory;
    }
}
