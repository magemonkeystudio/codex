package studio.magemonkey.codex.nms.v1_16_5;

import org.bukkit.attribute.AttributeModifier;
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
}
