package studio.magemonkey.codex.nms.v1_20_4;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.magemonkey.codex.api.meta.NBTAttribute;
import studio.magemonkey.codex.compat.Compat;

import java.util.Locale;

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
        if (name == null) {
            // Create a translatable component using Adventure's API.
            // Then translate it with GlobalTranslator.
            Component translatableComponent = Component.translatable(item.getTranslationKey());
            // TODO: Use the locale from Codex's config.yml -- The caveat here is we need the language and region in
            //  order to create a Locale object.
            Locale    locale   = Locale.ENGLISH;
            Component resolved = GlobalTranslator.render(translatableComponent, locale);
            name = LegacyComponentSerializer.legacyAmpersand().serializeOrNull(resolved);
        }

        //noinspection ConstantValue
        return name == null ? item.getType().toString() : name;
    }
}
