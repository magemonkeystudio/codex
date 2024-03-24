package com.promcteam.risecore.util.patterns;

import com.promcteam.risecore.Core;
import com.promcteam.risecore.legacy.cmds.DelayedCommand;
import com.promcteam.risecore.legacy.util.DeserializationWorker;
import com.promcteam.risecore.legacy.util.SerializationBuilder;
import com.promcteam.risecore.legacy.util.item.ItemBuilder;
import com.promcteam.risecore.util.menu.ItemMenu;
import com.promcteam.risecore.util.menu.MenuAction;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Collectors;

public class InventoryPattern implements ConfigurationSerializable {
    protected final String[]                                       pattern; // _ for ingredients, = for result.
    protected final HashMap<Character, ItemStack>                  items;
    protected final HashMap<Character, Collection<DelayedCommand>> commands          = new HashMap<>();
    protected final List<Character>                                closeOnClickSlots = new ArrayList<>();

    public InventoryPattern(String[] pattern, HashMap<Character, ItemStack> items) {
        this.pattern = pattern;
        this.items = items;
    }

    @SuppressWarnings("unchecked")
    public InventoryPattern(Map<String, Object> map) {

        DeserializationWorker dw   = DeserializationWorker.start(map);
        List<String>          temp = dw.getStringList("pattern");
        this.pattern = temp.toArray(new String[temp.size()]);
        this.items = new HashMap<>();
        DeserializationWorker itemsTemp = DeserializationWorker.start(dw.getSection("items", new HashMap<>(2)));
        for (String entry : itemsTemp.getMap().keySet()) {
            if (entry.contains("."))
                continue;

            Map<String, Object> section = itemsTemp.getSection(entry);
            this.items.put(entry.charAt(0), new ItemBuilder(section).build());

            if (section.containsKey("closeonclick") && (boolean) section.get("closeonclick")) {
                closeOnClickSlots.add(entry.charAt(0));
            }
        }

        final DeserializationWorker commandsTemp =
                DeserializationWorker.start(dw.getSection("commands", new HashMap<>(2)));
        for (final String entry : commandsTemp.getMap().keySet()) {
            this.commands.put(entry.charAt(0),
                    commandsTemp.deserializeCollection(new ArrayList<>(5), entry, DelayedCommand.class));
        }
    }

    public String[] getPattern() {
        return this.pattern;
    }

    public HashMap<Character, ItemStack> getItems() {
        return this.items;
    }

    public Collection<DelayedCommand> getCommands(char c) {
        return this.commands.get(c);
    }

    public List<Character> getCloseOnClickSlots() {
        return closeOnClickSlots;
    }

    public Character getSlot(int slot) {
        if (slot / 9 >= pattern.length)
            return ' ';
        return pattern[slot / 9].charAt(slot % 9);
    }

    public void apply(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            Character c    = getSlot(i);
            ItemStack item = getItems().get(c);
            inv.setItem(i, item);
        }
    }

    public void apply(ItemMenu menu) {
        for (int i = 0; i < menu.getSize(); i++) {
            Character c    = getSlot(i);
            ItemStack item = getItems().get(c);

            Collection<DelayedCommand> cmd = getCommands(c);
            MenuAction action = (player) -> {
                if (cmd != null && !cmd.isEmpty()) {
                    DelayedCommand.invoke(Core.getInstance(), player, cmd);
                }
                if (closeOnClickSlots.contains(c))
                    player.closeInventory();
            };
            menu.setItem(i, item, action);
        }
    }

    /**
     * Determine if the {@link InventoryPattern} contains the given character
     *
     * @param c - The character to find
     * @return boolean
     */
    public boolean contains(char c) {
        return !find(c).isEmpty();
    }

    /**
     * Attempts to find all indexes of char c in the pattern.
     * Returns a list of Integers possibly ranging from 0 to rows * 9
     *
     * @param c - The character to search for
     * @return {@link List List&lt;Integer&gt;}
     */
    public List<Integer> find(char c) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < getPattern().length; i++) {
            String line = getPattern()[i];
            if (!line.contains(String.valueOf(c))) continue;

            for (int k = 0; k < line.length(); k++) {
                if (line.charAt(k) == c) {
                    indexes.add(i * 9 + k);
                }
            }
        }

        return indexes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("pattern", this.pattern)
                .append("items", this.items)
                .toString();
    }

    @Override
    public Map<String, Object> serialize() {
        //noinspection Convert2MethodRef,RedundantCast eclipse...,
        return SerializationBuilder.start(2)
                .append("pattern", this.pattern)
                .appendMap("commands", this.commands)
                .append("items",
                        this.items.entrySet()
                                .stream()
                                .map(e -> new SimpleEntry<>(e.getKey().toString(),
                                        ItemBuilder.newItem(e.getValue()).serialize()))
                                .collect(Collectors.toMap((stringMapSimpleEntry) -> ((SimpleEntry<String, Map<String, Object>>) stringMapSimpleEntry).getKey(),
                                        (stringMapSimpleEntry1) -> ((SimpleEntry<String, Map<String, Object>>) stringMapSimpleEntry1).getValue())))
                .build();
    }
}
