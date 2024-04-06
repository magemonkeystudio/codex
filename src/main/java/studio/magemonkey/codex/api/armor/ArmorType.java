package studio.magemonkey.codex.api.armor;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum ArmorType {
    BOOTS(36), LEGGINGS(37), CHESTPLATE(38), HELMET(39), OFFHAND(40), MAIN_HAND(-1);

    private final int slot;

    ArmorType(int slot) {
        this.slot = slot;
    }

    /**
     * Attempts to match the ArmorType for the specified ItemStack.
     *
     * @param itemStack The ItemStack to parse the type of.
     * @return The parsed ArmorType
     */
    @Nullable
    public static ArmorType matchType(final ItemStack itemStack) {
        if (ArmorListener.isAirOrNull(itemStack)) return null;
        String type = itemStack.getType().name();
        if (type.endsWith("_HELMET") || type.endsWith("_SKULL") || type.endsWith("_HEAD")) return HELMET;
        else if (type.endsWith("_CHESTPLATE") || type.equals("ELYTRA")) return CHESTPLATE;
        else if (type.endsWith("_LEGGINGS")) return LEGGINGS;
        else if (type.endsWith("_BOOTS")) return BOOTS;
        else if (type.equals("SHIELD")) return OFFHAND;
        else return MAIN_HAND;
    }

    public int getSlot() {
        return slot;
    }

    public boolean matchesSlot(int slot, int heldSlot) {
        switch (this) {
            case MAIN_HAND, OFFHAND -> {
                return slot == 40 || slot == heldSlot;
            }
            default -> {
                return this.slot == slot;
            }
        }
    }
}