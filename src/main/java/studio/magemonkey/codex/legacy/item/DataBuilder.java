package studio.magemonkey.codex.legacy.item;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.meta.ItemMeta;
import studio.magemonkey.codex.util.SerializationBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class DataBuilder implements ConfigurationSerializable {
    private static final Map<String, Function<Map<String, Object>, DataBuilder>> builders = new HashMap<>(15);

    static {
        builders.put("book", BookDataBuilder::new);
        builders.put("enchantment_book", EnchantmentStorageBuilder::new);
        builders.put("firework", FireworkBuilder::new);
        builders.put("firework_effect", FireworkEffectBuilder::new);
        builders.put("leather", LeatherArmorBuilder::new);
        builders.put("map", MapBuilder::new);
        builders.put("potion", PotionDataBuilder::new);
        builders.put("skull", SkullBuilder::new);
    }

//    public static DataBuilder build(ItemMeta meta) {
//        if (meta == null)
//            return null;
//
//        return this.use(meta);
//    }

    public static DataBuilder build(final Map<String, Object> data) {
        if ((data == null) || data.isEmpty() || !data.containsKey("TYPE")) {
            return null;
        }
        final Function<Map<String, Object>, DataBuilder> func = builders.get(data.get("TYPE").toString().toLowerCase());
        return (func == null) ? null : func.apply(data);
    }

    public abstract void apply(ItemMeta itemMeta);

    public abstract DataBuilder use(ItemMeta itemMeta);

    public abstract String getType();

    @Override
    public Map<String, Object> serialize() {
        return SerializationBuilder.start(1).append("TYPE", this.getType()).build();
    }

    //    default DataBuilder applyFunc(final UnaryOperator<String> func)
//    {
//        return this;
//    }
}
