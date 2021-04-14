package su.nexmedia.engine.utils.reflection;

import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    public static Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        return con;
    }

    public static Object getCraftPlayer(Player p) throws ClassNotFoundException {
        Class craftClass = getNMSClass("CraftPlayer");
        return craftClass.cast(p);
    }

    public static Object getEntity(Object craftPlayer) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class craftClass = getNMSClass("Entity");

        Method getHandle = craftPlayer.getClass().getMethod("getHandle");

        return craftClass.cast(getHandle.invoke(craftPlayer));
    }

    public static Channel getChannel(Player p) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object conn = getConnection(p);
        Field networkManager = conn.getClass().getField("networkManager");
        Object manager = networkManager.get(conn);
        Object channel = manager.getClass().getField("channel").get(manager);

        return (Channel) channel;
    }

    public static void sendPacket(Player p, Object packet) {
        try {
            Object conn = getConnection(p);
            Class<?> packetClass = getNMSClass("Packet");
            Method sendMethod = conn.getClass().getMethod("sendPacket", packetClass);
            sendMethod.invoke(conn, packet);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            System.err.println("Could not send packet to player " + p.getName());
            e.printStackTrace();
        }
    }

    public static void sendAttackPacket(Player p, int id) {
        try {
            Object craftPlayer = getCraftPlayer(p);
            Object entity = getEntity(craftPlayer);

            Class packetClass = getNMSClass("PacketPlayOutAnimation");
            Constructor ctor = packetClass.getConstructor(entity.getClass(), int.class);
            Object packet = ctor.newInstance(entity, id);

            sendPacket(p, packet);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException e) {
            System.err.println("Could not send attack packet.");
            e.printStackTrace();
        }
    }

}
