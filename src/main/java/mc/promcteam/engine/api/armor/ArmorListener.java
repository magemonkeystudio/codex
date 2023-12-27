package mc.promcteam.engine.api.armor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static mc.promcteam.engine.api.armor.ArmorEquipEvent.EquipMethod;
import static org.bukkit.event.inventory.InventoryType.CRAFTING;

public class ArmorListener implements Listener {

    private final List<Material> blockedMaterials = Arrays.asList(
            Material.FURNACE,
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.BEACON,
            Material.DISPENSER,
            Material.DROPPER,
            Material.HOPPER,
            Material.CRAFTING_TABLE,
            Material.ENCHANTING_TABLE,
            Material.ENDER_CHEST,
            Material.ANVIL,
            Material.WHITE_BED,
            Material.ORANGE_BED,
            Material.MAGENTA_BED,
            Material.LIGHT_BLUE_BED,
            Material.YELLOW_BED,
            Material.LIME_BED,
            Material.PINK_BED,
            Material.GRAY_BED,
            Material.LIGHT_GRAY_BED,
            Material.CYAN_BED,
            Material.PURPLE_BED,
            Material.BLUE_BED,
            Material.BROWN_BED,
            Material.GREEN_BED,
            Material.RED_BED,
            Material.BLACK_BED,
            Material.OAK_FENCE_GATE,
            Material.SPRUCE_FENCE_GATE,
            Material.BIRCH_FENCE_GATE,
            Material.ACACIA_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE,
            Material.CRIMSON_FENCE_GATE,
            Material.WARPED_FENCE_GATE,
            Material.IRON_DOOR,
            Material.CRIMSON_DOOR,
            Material.OAK_DOOR,
            Material.WARPED_DOOR,
            Material.SPRUCE_DOOR,
            Material.BIRCH_DOOR,
            Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR,
            Material.ACACIA_BUTTON,
            Material.BIRCH_BUTTON,
            Material.CRIMSON_BUTTON,
            Material.DARK_OAK_BUTTON,
            Material.JUNGLE_BUTTON,
            Material.OAK_BUTTON,
            Material.SPRUCE_BUTTON,
            Material.WARPED_BUTTON,
            Material.STONE_BUTTON,
            Material.ACACIA_TRAPDOOR,
            Material.BIRCH_TRAPDOOR,
            Material.CRIMSON_TRAPDOOR,
            Material.DARK_OAK_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR,
            Material.OAK_TRAPDOOR,
            Material.SPRUCE_TRAPDOOR,
            Material.WARPED_TRAPDOOR,
            Material.IRON_TRAPDOOR,
            Material.REPEATER,
            Material.COMPARATOR,
            Material.SPRUCE_FENCE,
            Material.BIRCH_FENCE,
            Material.JUNGLE_FENCE,
            Material.DARK_OAK_FENCE,
            Material.ACACIA_FENCE,
            Material.CRIMSON_FENCE,
            Material.WARPED_FENCE,
            Material.NETHER_BRICK_FENCE,
            Material.BREWING_STAND,
            Material.CAULDRON,
            Material.ACACIA_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.CRIMSON_SIGN,
            Material.CRIMSON_WALL_SIGN,
            Material.WARPED_SIGN,
            Material.WARPED_WALL_SIGN,
            Material.BIRCH_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.DARK_OAK_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.OAK_SIGN,
            Material.OAK_WALL_SIGN,
            Material.SPRUCE_SIGN,
            Material.SPRUCE_WALL_SIGN,
            Material.LEVER,
            Material.BLACK_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.DAYLIGHT_DETECTOR,
            Material.BARREL,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.CARTOGRAPHY_TABLE,
            Material.COMPOSTER,
            Material.GRINDSTONE,
            Material.LECTERN,
            Material.LOOM,
            Material.STONECUTTER,
            Material.BELL
    );

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public final void inventoryClick(final InventoryClickEvent e) {
        if (e.getAction() == InventoryAction.NOTHING) {return;}
        InventoryView view = e.getView();
        if (!(view.getBottomInventory() instanceof PlayerInventory)) {
            return;
        }
        PlayerInventory playerInventory = ((PlayerInventory) view.getBottomInventory());
        if (!(playerInventory.getHolder() instanceof Player)) {
            return;
        }
        Player player = (Player) playerInventory.getHolder();
        int slot = e.getSlot();
        int heldSlot = playerInventory.getHeldItemSlot();
        boolean clickedPlayer = e.getClickedInventory() == playerInventory;
        switch (e.getClick()) {
            case SHIFT_LEFT, SHIFT_RIGHT: {
                ArmorType armorType = ArmorType.matchType(e.getCurrentItem());
                switch (Objects.requireNonNull(armorType)) {
                    case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> {
                        if (!clickedPlayer) {
                            return;
                        }
                        boolean equipping = slot != armorType.getSlot();
                        if (equipping == isAirOrNull(playerInventory.getItem(armorType.getSlot()))) {
                            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.SHIFT_CLICK, armorType,
                                    equipping ? null : e.getCurrentItem(), equipping ? e.getCurrentItem() : null);
                            if (isChange(armorEquipEvent)) {
                                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                                if (armorEquipEvent.isCancelled()) {
                                    e.setCancelled(true);
                                }
                            }
                        }
                    }
                    default -> {
                        ItemStack     currentItem      = Objects.requireNonNull(e.getCurrentItem()).clone();
                        List<Integer> destinationSlots = new ArrayList<>();
                        if (view.getTopInventory().getType() == CRAFTING) {
                            if (clickedPlayer) {
                                if (slot != 40 && armorType == ArmorType.OFFHAND) {
                                    destinationSlots.addAll(getDestinationSlots(currentItem, playerInventory, 40, 40));
                                }
                                if (slot == 40) {
                                    destinationSlots.addAll(getDestinationSlots(currentItem, playerInventory, 9, 35));
                                    destinationSlots.addAll(getDestinationSlots(currentItem, playerInventory, 0, 8));
                                } else if (slot >= 0 && slot <= 8) {
                                    destinationSlots.addAll(getDestinationSlots(currentItem, playerInventory, 9, 35));
                                } else {
                                    destinationSlots.addAll(getDestinationSlots(currentItem, playerInventory, 0, 8));
                                }
                            } else {
                                destinationSlots.addAll(getDestinationSlots(currentItem, playerInventory, 9, 35));
                                destinationSlots.addAll(getDestinationSlots(currentItem, playerInventory, 0, 8));
                            }
                        } else {
                            if (clickedPlayer) {
                                // Don't count slots from another inventory
                                getDestinationSlots(currentItem, view.getTopInventory());
                            } else {
                                destinationSlots.addAll(getDestinationSlots(currentItem, playerInventory, 8, 0));
                                destinationSlots.addAll(getDestinationSlots(currentItem, playerInventory, 35, 9));
                            }
                        }
                        boolean unequipped = currentItem.getAmount() <= 0 && clickedPlayer && (slot == 40 || slot == heldSlot);
                        boolean equipped   = destinationSlots.contains(40) || destinationSlots.contains(heldSlot);

                        if (unequipped != equipped) {
                            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.SHIFT_CLICK, armorType,
                                    unequipped ? e.getCurrentItem() : null, unequipped ? null : e.getCurrentItem());
                            if (isChange(armorEquipEvent)) {
                                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                                if (armorEquipEvent.isCancelled()) {
                                    e.setCancelled(true);
                                }
                            }
                        }
                    }
                }
                break;
            }
            case SWAP_OFFHAND: {
                ItemStack offHand = playerInventory.getItemInOffHand();
                if (clickedPlayer && slot >= 36 && slot <= 39) { // Armor slots
                    ArmorType armorType = ArmorType.matchType(offHand);
                    if (armorType == null) {
                        armorType = ArmorType.matchType(e.getCurrentItem());
                        if (armorType == null || armorType.getSlot() != slot) {
                            return;
                        }
                    } else if (armorType.getSlot() != slot) {
                        return;
                    }
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.HOTBAR_SWAP, armorType,
                            playerInventory.getItem(slot), offHand);
                    if (isChange(armorEquipEvent)) {
                        Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                        if (armorEquipEvent.isCancelled()) {
                            e.setCancelled(true);
                        }
                    }
                } else if (clickedPlayer && slot == 40) {
                    return;
                } else if (!clickedPlayer || slot != heldSlot) {
                    ArmorType armorType = ArmorType.matchType(offHand);
                    if (armorType != ArmorType.MAIN_HAND && armorType != ArmorType.OFFHAND) {
                        armorType = ArmorType.matchType(e.getCurrentItem());
                        if (armorType != ArmorType.MAIN_HAND && armorType != ArmorType.OFFHAND) {
                            return;
                        }
                    }
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.HOTBAR_SWAP, armorType,
                            offHand, e.getCurrentItem());
                    if (isChange(armorEquipEvent)) {
                        Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                        if (armorEquipEvent.isCancelled()) {
                            e.setCancelled(true);
                        }
                    }
                }
                break;
            }
            case NUMBER_KEY: {
                int       numberKey  = e.getHotbarButton();
                ItemStack hotbarItem = playerInventory.getItem(numberKey);
                if (clickedPlayer && slot >= 36 && slot <= 39) { // Armor slots
                    ArmorType armorType = ArmorType.matchType(hotbarItem);
                    if (armorType == null) {
                        armorType = ArmorType.matchType(e.getCurrentItem());
                        if (armorType == null || armorType.getSlot() != slot) {
                            return;
                        }
                    } else if (armorType.getSlot() != slot) {
                        return;
                    }
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.HOTBAR_SWAP, armorType,
                            playerInventory.getItem(slot), hotbarItem);
                    if (isChange(armorEquipEvent)) {
                        Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                        if (armorEquipEvent.isCancelled()) {
                            e.setCancelled(true);
                        }
                    }
                } else if (clickedPlayer && slot == numberKey) {
                    return;
                } else if (heldSlot == numberKey && (!clickedPlayer || slot != 40)) {
                    ArmorType armorType = ArmorType.matchType(hotbarItem);
                    if (armorType != ArmorType.MAIN_HAND && armorType != ArmorType.OFFHAND) {
                        armorType = ArmorType.matchType(e.getCurrentItem());
                        if (armorType != ArmorType.MAIN_HAND && armorType != ArmorType.OFFHAND) {
                            return;
                        }
                    }
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.HOTBAR_SWAP, armorType,
                            hotbarItem, e.getCurrentItem());
                    if (isChange(armorEquipEvent)) {
                        Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                        if (armorEquipEvent.isCancelled()) {
                            e.setCancelled(true);
                        }
                    }
                } else if (clickedPlayer && slot == heldSlot) {
                    ArmorType armorType = ArmorType.matchType(hotbarItem);
                    if (armorType != ArmorType.MAIN_HAND && armorType != ArmorType.OFFHAND) {
                        armorType = ArmorType.matchType(e.getCurrentItem());
                        if (armorType != ArmorType.MAIN_HAND && armorType != ArmorType.OFFHAND) {
                            return;
                        }
                    }
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.HOTBAR_SWAP, armorType,
                            e.getCurrentItem(), hotbarItem);
                    if (isChange(armorEquipEvent)) {
                        Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                        if (armorEquipEvent.isCancelled()) {
                            e.setCancelled(true);
                        }
                    }
                }
                break;
            }
            case DROP:
            case CONTROL_DROP: {
                ItemStack item = e.getCurrentItem();
                if (!clickedPlayer || isAirOrNull(item)) {
                    return;
                }
                ArmorType armorType = ArmorType.matchType(item);
                if (Objects.requireNonNull(armorType).matchesSlot(slot, heldSlot)
                        && (e.getClick() == ClickType.CONTROL_DROP || item.getAmount() == 1)) {
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.DROP, armorType,
                            e.getCurrentItem(), null);
                    if (isChange(armorEquipEvent)) {
                        Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                        if (armorEquipEvent.isCancelled()) {
                            e.setCancelled(true);
                        }
                    }
                }
                break;
            }
            case RIGHT: {
                if (!clickedPlayer) {
                    return;
                }
                ItemStack cursor      = e.getCursor();
                ItemStack currentItem = e.getCurrentItem();
                if (isAirOrNull(cursor)) {
                    if (isAirOrNull(currentItem)) {
                        return;
                    } else if (currentItem.getAmount() > 1) { // Would only take half
                        return;
                    } // Else go to LEFT
                } else {
                    if (isAirOrNull(currentItem)) {
                        if (cursor.getAmount() > 1) {
                            return;
                        }  // Else go to LEFT
                    } else if (cursor.isSimilar(currentItem)) {
                        return;
                    } // Else go to LEFT
                }
            }
            case LEFT: {
                ItemStack cursor      = e.getCursor();
                ItemStack currentItem = e.getCurrentItem();
                ArmorType armorType   = ArmorType.matchType(cursor);
                if (armorType == null || !armorType.matchesSlot(slot, heldSlot)) {
                    armorType = ArmorType.matchType(currentItem);
                    if (armorType == null || !armorType.matchesSlot(slot, heldSlot)) {
                        return;
                    }
                }
                if (slot >= 36 && slot <= 39) {
                    if (armorType.getSlot() != slot) {
                        return;
                    }
                }
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, EquipMethod.PICK_DROP, armorType,
                        currentItem, cursor);
                if (isChange(armorEquipEvent)) {
                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                    if (armorEquipEvent.isCancelled()) {
                        e.setCancelled(true);
                    }
                }
                break;
            }
            case DOUBLE_CLICK: { // TODO
                break;
            }
        }
    }

    private boolean isChange(ArmorEquipEvent e) {
        if (e.getOldArmorPiece() == null) {
            return e.getNewArmorPiece() != null;
        } else return !e.getOldArmorPiece().isSimilar(e.getNewArmorPiece());
    }

    private List<Integer> getDestinationSlots(ItemStack item, Inventory inventory, int first, int last) {
        List<Integer> destinationSlots = new ArrayList<>();
        int maxAmount = item.getMaxStackSize();
        boolean inverted = first > last;
        for (int i = inverted ? last : first; (item.getAmount() > 0) && (inverted ? i >= last : i <= last); i += inverted ? -1 : 1) {
            int currentAmount;
            ItemStack itemStack = inventory.getItem(i);
            if (!isAirOrNull(itemStack) && !itemStack.isSimilar(item)) {continue;}
            currentAmount = itemStack == null ? 0 : itemStack.getAmount();
            if (currentAmount == 0) {
                destinationSlots.add(i);
            }
            item.setAmount(Math.max(0, item.getAmount()+currentAmount-maxAmount));
        }
        return destinationSlots;
    }

    private List<Integer> getDestinationSlots(ItemStack item, Inventory inventory) {
        return getDestinationSlots(item, inventory, 0, inventory.getSize()-1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public final void onHeldSlotChange(final PlayerItemHeldEvent e) {
        PlayerInventory inventory = e.getPlayer().getInventory();
        ItemStack oldItem = inventory.getItem(e.getPreviousSlot());
        ItemStack newItem = inventory.getItem(e.getNewSlot());
        ArmorType armorType   = ArmorType.matchType(newItem);
        if (armorType != ArmorType.MAIN_HAND && armorType != ArmorType.OFFHAND) {
            armorType = ArmorType.matchType(oldItem);
            if (armorType != ArmorType.MAIN_HAND && armorType != ArmorType.OFFHAND) {
                return;
            }
        }
        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), EquipMethod.HELD_SLOT_CHANGE,
                ArmorType.MAIN_HAND, oldItem, newItem);
        if (isChange(armorEquipEvent)) {
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if (armorEquipEvent.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteractEvent(PlayerInteractEvent e) { // Fixme doesn't work on air
        if (e.useItemInHand().equals(Result.DENY)) {return;}
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = e.getPlayer();
        PlayerInventory inventory = player.getInventory();
        if (!e.useInteractedBlock().equals(Result.DENY)) {
            if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {// Having both of these checks is useless, might as well do it though.
                // Some blocks have actions when you right-click them which stops the client from equipping the
                // armor in hand.
                Material mat = e.getClickedBlock().getType();
                for (Material s : blockedMaterials) {
                    if (mat == s) return;
                }
            }
        }
        ArmorType armorType = ArmorType.matchType(e.getItem());
        if (armorType == null) {
            return;
        }
        switch (armorType) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> {
                ItemStack currentItem = inventory.getItem(armorType.getSlot());
                if (!isAirOrNull(currentItem)) {
                    ItemMeta meta = currentItem.getItemMeta();
                    if (meta != null && meta.hasEnchant(Enchantment.BINDING_CURSE)) {
                        return;
                    }
                }
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), EquipMethod.HOTBAR, armorType,
                        currentItem, e.getItem());
                if (isChange(armorEquipEvent)) {
                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                    if (armorEquipEvent.isCancelled()) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemConsumeEvent(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getAmount() > 1) {
            return;
        }
        ArmorType armorType = ArmorType.matchType(item);
        if (armorType != ArmorType.MAIN_HAND && armorType != ArmorType.OFFHAND) {
            return;
        }
        try {
            switch (event.getHand()) {
                case HAND -> armorType = ArmorType.MAIN_HAND;
                case OFF_HAND -> armorType = ArmorType.OFFHAND;
            }
        } catch (NoSuchMethodError e) {
            boolean inMainHand = event.getPlayer().getInventory().getItemInMainHand().equals(item);
            armorType = inMainHand ? ArmorType.MAIN_HAND : ArmorType.OFFHAND;
        }
        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(event.getPlayer(), EquipMethod.CONSUME,
                armorType, item, null);
        if (isChange(armorEquipEvent)) {
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if (armorEquipEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryDrag(InventoryDragEvent event) {
        ArmorType armorType = ArmorType.matchType(event.getOldCursor());
        if (armorType == null) {
            return;
        }
        InventoryView view = event.getView();
        for (int rawSlot : event.getRawSlots()) {
            Inventory inventory = view.getInventory(rawSlot);
            if (inventory instanceof PlayerInventory) {
                PlayerInventory playerInventory = (PlayerInventory) inventory;
                int heldSlot = playerInventory.getHeldItemSlot();
                int slot = view.convertSlot(rawSlot);
                if (armorType.matchesSlot(slot, heldSlot)) {
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) playerInventory.getHolder(), EquipMethod.DRAG,
                            armorType, view.getItem(rawSlot), event.getNewItems().get(rawSlot));
                    if (isChange(armorEquipEvent)) {
                        Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                        if (armorEquipEvent.isCancelled()) {
                            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                            if (armorEquipEvent.isCancelled()) {
                                event.setResult(Result.DENY);
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void dispenseArmorEvent(BlockDispenseArmorEvent event) {
        ArmorType type = ArmorType.matchType(event.getItem());
        if (type != null) {
            if (event.getTargetEntity() instanceof Player) {
                Player          p               = (Player) event.getTargetEntity();
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.DISPENSER, type, null, event.getItem());
                if (isChange(armorEquipEvent)) {
                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                    if (armorEquipEvent.isCancelled()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent e) {
        ItemStack item = e.getBrokenItem();
        if (item.getAmount() > 1) {
            return;
        }
        ArmorType armorType = ArmorType.matchType(item);
        if (armorType == null) {
            return;
        }
        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), EquipMethod.BROKE, armorType,
                item, null);
        if (isChange(armorEquipEvent)) {
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if (armorEquipEvent.isCancelled()) {
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                if (armorEquipEvent.isCancelled()) {
                    throw new UnsupportedOperationException("Cannot cancel ArmorEquipEvent with BROKE EquipMethod");
                }
            }
        }
    }

    /**
     * A utility method to support versions that use null or air ItemStacks.
     */
    public static boolean isAirOrNull(ItemStack item) {
        return item == null || item.getType().isAir();
    }

}
