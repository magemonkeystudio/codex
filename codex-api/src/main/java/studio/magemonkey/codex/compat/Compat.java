package studio.magemonkey.codex.compat;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.api.meta.NBTAttribute;

import java.util.UUID;

public interface Compat {
    UUID ATTRIBUTE_BONUS_UUID = UUID.fromString("11f1173c-6666-4444-8888-02cb0285f9c1");

    AttributeModifier createAttributeModifier(NBTAttribute attribute,
                                              double amount,
                                              AttributeModifier.Operation operation);

    String getAttributeKey(AttributeModifier attributeModifier);

    /**
     * Gets the top inventory from an InventoryEvent's InventoryView. We do
     * this here because InventoryView is an interface in 1.21+.
     * Running the code here allows us to compile against an older bytecode version
     * and thus allow Spigot's auto-conversion system to upgrade the bytecode to look
     * for the interface instead of the class where applicable.
     *
     * @param event The generic InventoryEvent with an InventoryView to inspect.
     * @return The top Inventory object from the event's InventoryView.
     */
    default Inventory getTopInventory(InventoryEvent event) {
        InventoryView view = event.getView();
        return view.getTopInventory();
    }

    /**
     * Gets the top inventory from the player's open inventory. We do
     * this here because the InventoryView class is actually an interface in 1.21+.
     * Running the code here allows us to compile against an older bytecode version
     * and thus allow Spigot's auto-conversion system to upgrade the bytecode to look
     * for the interface instead of the class where applicable.
     *
     * @param player The player to get the top inventory from.
     * @return The top Inventory object from the player's open inventory.
     */
    @NotNull
    default Inventory getTopInventory(Player player) {
        InventoryView view = player.getOpenInventory();
        return view.getTopInventory();
    }

    /**
     * Gets the bottom inventory from the InventoryEvent's InventoryView. We do
     * this here because the InventoryView class is actually an interface in 1.21+.
     * Running the code here allows us to compile against an older bytecode version
     * and thus allow Spigot's auto-conversion system to upgrade the bytecode to look
     * for the interface instead of the class where applicable.
     *
     * @param event The generic InventoryEvent with an InventoryView to inspect.
     * @return The bottom Inventory object from the event's InventoryView.
     */
    default Inventory getBottomInventory(InventoryEvent event) {
        InventoryView view = event.getView();
        return view.getBottomInventory();
    }

    /**
     * Again, version compatibility stuff.
     *
     * @param event The generic InventoryEvent with an InventoryView to modify.
     * @param item  The ItemStack to set as the cursor in the event's InventoryView.
     */
    default void setCursor(InventoryEvent event, ItemStack item) {
        InventoryView view = event.getView();
        view.setCursor(item);
    }

    /**
     * Sets the item in the specified slot of the player's open inventory. Doing here for compatibility.
     *
     * @param player the player to set the item for
     * @param slot   the target slot
     * @param item   the item to set
     */
    default void setItem(Player player, int slot, ItemStack item) {
        InventoryView view = player.getOpenInventory();
        view.setItem(slot, item);
    }

    /**
     * In API versions 1.20.6 and earlier, InventoryView is a class.
     * In versions 1.21 and later, it is an interface.
     * This code is compiled against 1.16.5 to avoid runtime errors.
     * The slot parameter is used to determine which Inventory object to return.
     * If the slot is in the top Inventory, the top Inventory is returned.
     * If the slot is in the bottom Inventory, the bottom Inventory is returned.
     * If the slot is not in either Inventory, null.
     *
     * @param event The generic InventoryEvent with an InventoryView to inspect.
     * @param slot  The slot index to check in the InventoryView.
     * @return The Inventory object from the event's InventoryView at the specified slot.
     */
    default Inventory getInventory(InventoryEvent event, int slot) {
        InventoryView view = event.getView();
        return view.getInventory(slot);
    }

    /**
     * In API versions 1.20.6 and earlier, InventoryView is a class.
     * In versions 1.21 and later, it is an interface.
     * This code is compiled against 1.16.5 to avoid runtime errors.
     * The slot parameter is used to determine which ItemStack to return.
     *
     * @param event The generic InventoryEvent with an InventoryView to inspect.
     * @param slot  The slot index to check in the InventoryView.
     * @return The ItemStack from the event's InventoryView at the specified slot.
     */
    default ItemStack getItem(InventoryEvent event, int slot) {
        InventoryView view = event.getView();
        return view.getItem(slot);
    }

    /**
     * In API versions 1.20.6 and earlier, InventoryView is a class.
     * In versions 1.21 and later, it is an interface.
     * This code is compiled against 1.16.5 to avoid runtime errors.
     * The slot parameter is used to determine which slot index to convert.
     *
     * @param event The generic InventoryEvent with an InventoryView to inspect.
     * @param slot  The slot index to convert in the InventoryView.
     * @return The converted slot index from the event's InventoryView.
     */
    default int convertSlot(InventoryEvent event, int slot) {
        InventoryView view = event.getView();
        return view.convertSlot(slot);
    }

    default String getItemName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item.getType().toString();
        }

        String name = null;

        if (meta.hasDisplayName()) name = meta.getDisplayName();
        if (name == null && meta.getLore() != null && !meta.getLore().isEmpty()) name = meta.getLore().get(0);

        return name == null ? item.getType().toString() : name;
    }
}
