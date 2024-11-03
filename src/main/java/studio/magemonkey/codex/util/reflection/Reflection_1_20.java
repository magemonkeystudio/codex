package studio.magemonkey.codex.util.reflection;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.core.Version;
import studio.magemonkey.codex.util.Reflex;

import java.lang.reflect.Method;

public class Reflection_1_20 extends Reflection_1_18 {
    @Override
    public Object getConnection(Player player) {
        try {
            Class craftPlayerClass = getCraftClass("entity.CraftPlayer");

            Method getHandle = Reflex.getMethod(craftPlayerClass, "getHandle");
            Object nmsPlayer = Reflex.invokeMethod(getHandle, getCraftPlayer(player));

            String fieldName = Version.CURRENT.isAtLeast(Version.V1_21_R2) ? "f" : "c";
            Object con       = Reflex.getFieldValue(nmsPlayer, fieldName); //WHY must you obfuscate
            if (!con.getClass().getSimpleName().equals("PlayerConnection") && !con.getClass()
                    .getSimpleName()
                    .equals("ServerGamePacketListenerImpl") && !con.getClass()
                    .getSimpleName()
                    .equals("GeneratedInterceptor")) {
                CodexEngine.get()
                        .getLogger()
                        .warning("Expected PlayerConnection, got " + con.getClass().getSimpleName() + " instead!");
                throw new ClassNotFoundException(
                        "Could not get connection from CraftPlayer using field " + fieldName + "\nNMS Player: "
                                + nmsPlayer + "\n");
            }
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        if (Version.CURRENT.isAtLeast(Version.V1_21_R2)) return getAttributeValue(itemStack, Attribute.ATTACK_DAMAGE);
        else return super.getDefaultDamage(itemStack);
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        if (Version.CURRENT.isAtLeast(Version.V1_21_R2)) return getAttributeValue(itemStack, Attribute.ATTACK_SPEED);
        else return super.getDefaultSpeed(itemStack);
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        if (Version.CURRENT.isAtLeast(Version.V1_21_R2)) return getAttributeValue(itemStack, Attribute.ARMOR);
        else return super.getDefaultArmor(itemStack);
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        if (Version.CURRENT.isAtLeast(Version.V1_21_R2)) return getAttributeValue(itemStack, Attribute.ARMOR_TOUGHNESS);
        else return super.getDefaultToughness(itemStack);
    }
}
