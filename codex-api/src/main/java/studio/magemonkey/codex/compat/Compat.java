package studio.magemonkey.codex.compat;

import org.bukkit.attribute.AttributeModifier;
import studio.magemonkey.codex.api.meta.NBTAttribute;

import java.util.UUID;

public interface Compat {
    UUID                     ATTRIBUTE_BONUS_UUID = UUID.fromString("11f1173c-6666-4444-8888-02cb0285f9c1");

    AttributeModifier createAttributeModifier(NBTAttribute attribute, double amount, AttributeModifier.Operation operation);

    String getAttributeKey(AttributeModifier attributeModifier);
}
