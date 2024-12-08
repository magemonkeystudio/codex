package studio.magemonkey.codex.manager.api;

import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public enum ClickType {
    LEFT,
    RIGHT,
    MIDDLE,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    ;

    @NotNull
    public static ClickType from(@NotNull InventoryClickEvent e) {
        if (e.isShiftClick()) {
            return e.isLeftClick() ? SHIFT_LEFT : SHIFT_RIGHT;
        }
        if (e.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE) {
            return MIDDLE;
        }
        if (e.isRightClick()) {
            return RIGHT;
        }
        return LEFT;
    }

    public static ClickType from(Action action, boolean shift) {
        if (shift) {
            return (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) ? SHIFT_LEFT : SHIFT_RIGHT;
        }
        return (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) ? LEFT : RIGHT;
    }
}
