package studio.magemonkey.codex.nms.v1_18_2;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.meta.NBTAttribute;
import studio.magemonkey.codex.compat.Compat;

import java.util.UUID;

public class CompatImpl implements Compat {
    @Override
    public AttributeModifier createAttributeModifier(NBTAttribute attribute,
                                                     double amount,
                                                     AttributeModifier.Operation operation,
                                                     @Nullable EquipmentSlot slot) {
        UUID uuid = slot != null ? attribute.getUUID(slot) : ATTRIBUTE_BONUS_UUID;
        return new AttributeModifier(uuid, attribute.getNmsName(), amount, operation, slot);
    }

    @Override
    public String getAttributeKey(AttributeModifier attributeModifier) {
        return attributeModifier.getName();
    }

    @Override
    public String getAttributeKey(NBTAttribute attribute) {
        return attribute.getAttribute().getKey().toString();
    }
}
