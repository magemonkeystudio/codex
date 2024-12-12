package studio.magemonkey.codex.nms.v1_21_1;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import studio.magemonkey.codex.compat.Compat;
import studio.magemonkey.codex.api.meta.NBTAttribute;

public class CompatImpl implements Compat {
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public AttributeModifier createAttributeModifier(NBTAttribute attribute, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(attribute.getAttribute().getKey(), amount, operation, EquipmentSlotGroup.ANY);
    }

    @Override
    public String getAttributeKey(AttributeModifier attributeModifier) {
        return attributeModifier.getKey().toString();
    }
}
