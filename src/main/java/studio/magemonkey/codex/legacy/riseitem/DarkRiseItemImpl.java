package studio.magemonkey.codex.legacy.riseitem;

import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.api.DelayedCommand;
import studio.magemonkey.codex.api.Replacer;
import studio.magemonkey.codex.legacy.item.ItemBuilder;
import studio.magemonkey.codex.util.SerializationBuilder;
import studio.magemonkey.risecore.legacy.util.DeserializationWorker;
import lombok.Getter;
import org.apache.commons.lang3.DoubleRange;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

@SerializableAs("DarkRiseItemImpl")
public class DarkRiseItemImpl implements DarkRiseItem {
    @Getter
    private final String id;

    private final ItemStack item;

    @Getter
    private final boolean dropOnDeath;

    private final int removeOnDeath;

    @Getter
    private final boolean confirmOnUse;

    private final int removeOnUse;

    private final boolean canDrop;

    @Getter
    private final boolean tradeable;

    @Getter
    private final boolean enabledEnchantedDurability;

    private final DoubleRange chanceToLostDurability;

    @Getter
    private final List<DelayedCommand> commands;

    private final List<String> permissionList = new ArrayList<>();

    @Getter
    private final String permissionMessage;

//    private final boolean twoHand;
//
//    private final DivineItemsMeta divineItemsMeta;

    public DarkRiseItemImpl(Map<String, Object> map) {
        DeserializationWorker w = DeserializationWorker.start(map);
        this.id = w.getString("id").intern();
        if (w.getTypedObject("item") instanceof ItemStack)
            this.item = w.getTypedObject("item", ItemStack.class);//.build();
        else
            this.item = w.getTypedObject("item", ItemBuilder.class).build();
        this.dropOnDeath = w.getBoolean("dropOnDeath", true);
        this.removeOnDeath = w.getInt("removeOnDeath", 1);
        this.confirmOnUse = w.getBoolean("confirmOnUse", false);
        this.removeOnUse = w.getInt("removeOnUse", 0);
        this.canDrop = w.getBoolean("canDrop", true);
        this.tradeable = w.getBoolean("tradeable", true);
//        this.twoHand = w.getBoolean("twoHand", false);
//        if (map.containsKey("divineItemsMeta")) {
//            this.divineItemsMeta = w.getTypedObject("divineItemsMeta", DivineItemsMeta.class);
//        } else {
//            this.divineItemsMeta = new DivineItemsMeta();
//        }
        if (w.getMap().containsKey("permission")) {
            DeserializationWorker permSec = DeserializationWorker.start(w.getSection("permission", new HashMap<>()));
            if (permSec.getObject("node") instanceof java.util.Collection) {
                this.permissionList.addAll(permSec.getList("node", new ArrayList()));
            } else {
                this.permissionList.add(permSec.getString("node"));
            }
            this.permissionMessage = permSec.getString("message", "&4You don't have permission to use this");
        } else {
            this.permissionMessage = null;
        }
        this.enabledEnchantedDurability = w.getBoolean("enabledEnchantedDurability", false);
        String[] rangeString = w.getString("chanceToLostDurability", "0.0 - 0.0").split("-");
        this.chanceToLostDurability =
                DoubleRange.of(Double.parseDouble(rangeString[0].trim()), Double.parseDouble(rangeString[1].trim()));

        this.commands = ((List<Map<String, Object>>) map.get("commands"))
                .stream()
                .map(DelayedCommand::new)
                .collect(Collectors.toList());

        ItemMeta im = item.getItemMeta();
        if (im.hasDisplayName())
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', im.getDisplayName()));
        if (im.hasLore()) {
            List<String> lore = new ArrayList<>();
            im.getLore().forEach(str -> lore.add(ChatColor.translateAlternateColorCodes('&', str)));
            im.setLore(lore);
        }

        item.setItemMeta(im);
    }

    public DarkRiseItemImpl(String id, ItemStack item) {
        this.id = id.intern();
        this.item = item;
        this.dropOnDeath = true;
        this.removeOnDeath = 1;
        this.confirmOnUse = false;
        this.removeOnUse = 0;
        this.canDrop = true;
        this.tradeable = true;
        this.enabledEnchantedDurability = false;
        this.chanceToLostDurability = DoubleRange.of(0d, 0d);
        this.commands = new ArrayList<>();
        this.permissionMessage = null;
//        this.twoHand = false;
//        this.divineItemsMeta = null;
    }

    public DarkRiseItemImpl(String id,
                            ItemStack item,
                            boolean dropOnDeath,
                            int removeOnDeath,
                            boolean confirmOnUse,
                            int removeOnUse,
                            boolean canDrop,
                            boolean enabledEnchantedDurability,
                            DoubleRange chanceToLostDurability,
                            List<DelayedCommand> commands) {
        this(id,
                item,
                dropOnDeath,
                removeOnDeath,
                confirmOnUse,
                removeOnUse,
                canDrop,
                enabledEnchantedDurability,
                chanceToLostDurability,
                commands,
                -1);
    }

    public DarkRiseItemImpl(String id,
                            ItemStack item,
                            boolean dropOnDeath,
                            int removeOnDeath,
                            boolean confirmOnUse,
                            int removeOnUse,
                            boolean canDrop,
                            boolean enabledEnchantedDurability,
                            DoubleRange chanceToLostDurability,
                            List<DelayedCommand> commands,
                            int modelData) {
        this.id = id;
        this.item = item.clone();
        if (modelData != -1) {
            ItemMeta im = this.item.getItemMeta();
            im.setCustomModelData(modelData);
            this.item.setItemMeta(im);
        }
        this.dropOnDeath = dropOnDeath;
        this.removeOnDeath = removeOnDeath;
        this.confirmOnUse = confirmOnUse;
        this.removeOnUse = removeOnUse;
        this.canDrop = canDrop;
        this.tradeable = true;
        this.enabledEnchantedDurability = enabledEnchantedDurability;
        this.chanceToLostDurability = chanceToLostDurability;
        this.commands = commands;
        this.permissionMessage = null;
//        this.twoHand = false;
//        this.divineItemsMeta = null;
    }

    public String getName() {
        return isVanilla() ? this.item.getType().name() : this.item.getItemMeta().getDisplayName();
    }

    public int isRemoveOnDeath() {
        return this.removeOnDeath;
    }

    public int isRemoveOnUse() {
        return this.removeOnUse;
    }

    public boolean canDrop() {
        return this.canDrop;
    }

    public DoubleRange chanceToLostDurability() {
        return this.chanceToLostDurability;
    }

//    public boolean isTwoHand() {
//        return this.twoHand;
//    }

    public List<String> getPermission() {
        return this.permissionList;
    }

    public ItemStack getItem(int amount) {
        ItemStack clone = this.item.clone();
        clone.setAmount(amount);
        return clone;
    }

    public boolean isVanilla() {
        return getId().startsWith("vanilla_");
    }

    public void invoke(CommandSender sender) {
        if (this.commands.isEmpty())
            return;
        DelayedCommand.invoke(CodexEngine.get(),
                sender,
                this.commands,
                Replacer.replacer("{canDrop}", Boolean.valueOf(this.canDrop)),
                Replacer.replacer("{enabledEnchantedDurability}", Boolean.valueOf(this.enabledEnchantedDurability)),
                Replacer.replacer("{chanceToLostDurability}", this.chanceToLostDurability.toString()),
                Replacer.replacer("{dropOnDeath}", Boolean.valueOf(this.dropOnDeath)),
                Replacer.replacer("{removeOnDeath}", Integer.valueOf(this.removeOnDeath)),
                Replacer.replacer("{confirmOnUse}", Boolean.valueOf(this.confirmOnUse)),
                Replacer.replacer("removeOnUse", Integer.valueOf(this.removeOnUse)),
                Replacer.replacer("{id}", this.id),
                Replacer.replacer("{name}", getName()));
    }

    public Map<String, Object> serialize() {
        SerializationBuilder sb =
                SerializationBuilder.start(10)
                        .append("id", this.id)
                        .append("canDrop", Boolean.valueOf(this.canDrop))
                        .append("tradeable", Boolean.valueOf(this.tradeable))
                        .append("enabledEnchantedDurability", Boolean.valueOf(this.enabledEnchantedDurability))
                        .append("chanceToLostDurability",
                                this.chanceToLostDurability.getMinimum() + "-"
                                        + this.chanceToLostDurability.getMaximum())
                        .append("dropOnDeath", Boolean.valueOf(this.dropOnDeath))
                        .append("removeOnDeath", Integer.valueOf(this.removeOnDeath))
                        .append("confirmOnUse", Boolean.valueOf(this.confirmOnUse))
                        .append("removeOnUse",
                                Integer.valueOf(this.removeOnUse))//.append("twoHand", Boolean.valueOf(this.twoHand))
                        .append("permission",
                                SerializationBuilder.start(2)
                                        .append("node", this.permissionList)
                                        .append("message", this.permissionMessage))
                        .append("commands",
                                this.commands.stream().map(DelayedCommand::serialize).collect(Collectors.toList()));

        Map<String, Object> item = new HashMap<>(this.item.serialize());
        item.put("==", "org.bukkit.inventory.ItemStack");
        ItemMeta im = this.item.getItemMeta();
        if (im != null) {
            Map<String, Object> meta = new HashMap<>(im.serialize());
            if (im.hasDisplayName())
                meta.put("display-name", im.getDisplayName());
            if (im.hasLore())
                meta.put("lore", im.getLore());

            meta.put("==", "ItemMeta");
            item.put("meta", meta);
        }


        sb.append("item", /*ItemBuilder.newItem(*/this.item/*)*/);
        sb.append("item", item);
//        if (this.divineItemsMeta != null)
//            sb.append("divineItemsMeta", this.divineItemsMeta);
        return sb.build();
    }

    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof DarkRiseItemImpl))
            return false;

        DarkRiseItemImpl riseItem = (DarkRiseItemImpl) object;
        return Objects.equals(getId(), riseItem.getId());
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return (new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE))
                .append("id", this.id)
                .append("item", this.item)
//                .append("twoHand", this.twoHand)
                .append("dropOnDeath", this.dropOnDeath)
                .append("removeOnDeath", this.removeOnDeath)
                .append("confirmOnUse", this.confirmOnUse)
                .append("removeOnUse", this.removeOnUse)
                .append("canDrop", this.canDrop)
                .append("tradeable", this.tradeable)
                .append("enabledEnchantedDurability", this.enabledEnchantedDurability)
                .append("chanceToLostDurability", this.chanceToLostDurability)
                .append("commands", this.commands)
                .append("name", getName())
                .toString();
    }

    public static class DivineItemsMeta implements ConfigurationSerializable {
        public boolean enabled;

        public String tierName;

        public Double tierLevel;

        public DivineItemsMeta(Map<String, Object> map) {
            DeserializationWorker w = DeserializationWorker.start(map);
            this.enabled = w.getBoolean("enabled", false);
            this.tierName = w.getString("tierName");
            this.tierLevel = Double.valueOf(w.getDouble("tierLevel"));
        }

        public DivineItemsMeta() {
            this.enabled = false;
            this.tierName = "";
            this.tierLevel = Double.valueOf(0.0D);
        }

        public Map<String, Object> serialize() {
            return SerializationBuilder.start(3)
                    .append("enabled", Boolean.valueOf(this.enabled))
                    .append("tierName", this.tierName)
                    .append("tierLevel", this.tierLevel)
                    .build();
        }
    }
}
