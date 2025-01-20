package studio.magemonkey.codex.nms.v1_20_1;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.magemonkey.codex.api.meta.NBTAttribute;
import studio.magemonkey.codex.compat.Compat;

public class CompatImpl implements Compat {
    @Override
    public AttributeModifier createAttributeModifier(NBTAttribute attribute,
                                                     double amount,
                                                     AttributeModifier.Operation operation) {
        return new AttributeModifier(ATTRIBUTE_BONUS_UUID, attribute.getNmsName(), amount, operation);
    }

    @Override
    public String getAttributeKey(AttributeModifier attributeModifier) {
        return attributeModifier.getName();
    }

    @Override
    public String getAttributeKey(NBTAttribute attribute) {
        return attribute.getAttribute().getKey().toString();
    }

    @Override
    public String getItemName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item.getType().toString();
        }

        String name = null;

        if (meta.hasDisplayName()) name = meta.getDisplayName();
        if (name == null && meta.getLore() != null && !meta.getLore().isEmpty()) name = meta.getLore().get(0);
        if (name == null) name =
                LegacyComponentSerializer.legacyAmpersand()
                        .serializeOrNull(Component.translatable(item.getTranslationKey()));

        //noinspection ConstantValue
        return name == null ? item.getType().toString() : name;
    }
}
