package studio.magemonkey.codex.manager.api.menu;

import org.bukkit.inventory.ItemStack;

public class Slot {
    protected Integer   i = null;
    protected Menu      menu;
    protected ItemStack itemStack;

    public Slot(ItemStack itemStack) {this.itemStack = itemStack;} // Add permission

    void setMenu(int i, Menu menu) {
        this.i = i;
        this.menu = menu;
    }

    public ItemStack getItemStack() {return itemStack == null ? null : itemStack.clone();}

    public void setItemStack(ItemStack itemStack) {this.itemStack = itemStack;}

    public void onLeftClick() {}

    public void onShiftLeftClick() {onLeftClick();}

    public void onRightClick() {onLeftClick();}

    public void onShiftRightClick() {onRightClick();}

    public void onMiddleClick() {onLeftClick();}

    public void onNumberClick(int numberKey) {onLeftClick();}

    public void onDoubleClick() {onLeftClick();}

    public void onDrop() {onLeftClick();}

    public void onControlDrop() {onDrop();}

    public void onSwapOffhand() {onLeftClick();}
}
