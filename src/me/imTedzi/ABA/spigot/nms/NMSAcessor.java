package me.imTedzi.ABA.spigot.nms;

import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class NMSAcessor {

    private static Class<?> CraftServer;
    private static Class<?> BooleanWrapper;
    private static Class<?> CraftPlayer;
    private static Class<?> EntityHuman;
    private static Class<?> Entity;
    private static Method getHandle;

    static {
        try {
            CraftServer = getBMSClass("CraftServer");
            BooleanWrapper = getBMSClass("CraftServer$BooleanWrapper");
            CraftPlayer = getBMSClass("entity.CraftPlayer");
            getHandle = CraftPlayer.getDeclaredMethod("getHandle");
            EntityHuman = getNMSClass("EntityHuman");
            Entity = getNMSClass("Entity");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a class from NMS
     * @param nmsClassString string
     * @return class
     * @throws ClassNotFoundException exception
     */
    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        return Class.forName(name);
    }

    /**
     * Get a class from NMS - bukkit
     * @param nmsClassString string
     * @return class
     * @throws ClassNotFoundException exception
     */
    public static Class<?> getBMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version + nmsClassString;
        return Class.forName(name);
    }

    /**
     * Change the server online mode using reflection
     * @param server server
     * @param value value
     */
    public static void setOnlineMode(Server server, boolean value)
            throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Field onlineMode = CraftServer.getDeclaredField("online");
        onlineMode.setAccessible(true);

        Field omValue = BooleanWrapper.getDeclaredField("value");
        omValue.setAccessible(true);

        Object sv = CraftServer.cast(server);
        Object booleanWrapper = onlineMode.get(sv);
        omValue.set(booleanWrapper, value);
    }

    /**
     * Change player uuid using reflection
     * @param p player
     * @param uuid uuid
     */
    public static void bindUUID(Player p, UUID uuid)
            throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Object craftPlayer = CraftPlayer.cast(p);
        Object entityPlayer = getHandle.invoke(craftPlayer);
        Object entityHuman = EntityHuman.cast(entityPlayer);
        Object entity = Entity.cast(entityPlayer);

        /* Create new GameProfile */
        GameProfile profile = new GameProfile(uuid, p.getName());

        /* Change GameProfile from EntityHuman Class */
        Field bH = EntityHuman.getDeclaredField("bH");
        bH.setAccessible(true);
        bH.set(entityHuman, profile);

        /* Change uniqueID from Entity Class */
        Field uniqueID = Entity.getDeclaredField("uniqueID");
        uniqueID.setAccessible(true);
        uniqueID.set(entity, uuid);
    }
}