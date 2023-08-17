package mc.promcteam.engine.manager.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

public abstract class YAMLListMenu<T> extends YAMLMenu<T> {

    public YAMLListMenu(Plugin plugin, String path) {
        super(plugin, path);
    }

    @Override
    public void reload() {
        super.reload();
        int max = this.rows * 9;
        this.slots.keySet().removeIf(integer -> integer >= max);
    }

    @Override
    public void setSlots(Menu menu, T parameter) {
        menu.slots.clear();
        Player     player  = menu.getPlayer();
        List<Slot> entries = getEntries(parameter);
        int        max     = this.rows * 9;
        int        index   = 0;
        for (Slot entry : entries) {
            while (true) {
                String value = this.slots.get(index % max);
                if (value == null) {
                    break;
                } else {
                    Slot slot = this.getSlot(value, parameter, player);
                    if (slot != null) {menu.setSlot(index, slot);}
                    index++;
                }
            }
            if (entry != null) {
                menu.setSlot(index, entry);
                index++;
            }
        }

        ItemStack emptySlot = this.getItem("empty");
        for (int i = 0, size = menu.slots.lastKey() / (this.getRows() * 9) + 1; i < size; i++) {
            menu.slots.putIfAbsent(i, new Slot(emptySlot));
        }
    }

    public abstract List<Slot> getEntries(T parameter);
}
