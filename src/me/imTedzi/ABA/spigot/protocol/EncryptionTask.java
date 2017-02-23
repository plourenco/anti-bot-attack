package me.imTedzi.ABA.spigot.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.managers.BotManager;
import me.imTedzi.ABA.spigot.managers.Config;
import me.imTedzi.ABA.spigot.util.BukkitLoginSession;
import me.imTedzi.ABA.spigot.util.LoginSession;
import me.imTedzi.ABA.spigot.util.MojangAPI;
import org.bukkit.entity.Player;

import javax.crypto.SecretKey;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

/**
 *  Credits to games647
 */
public class EncryptionTask implements Runnable {

    private final Main plugin;
    private final PacketEvent packetEvent;
    private final Player player;
    private final byte[] sharedSecret;

    public EncryptionTask(Main plugin, PacketEvent packetEvent, Player player, byte[] sharedSecret) {
        this.plugin = plugin;
        this.packetEvent = packetEvent;
        this.player = player;
        this.sharedSecret = sharedSecret;
    }

    @Override
    public void run() {
        try {
            BukkitLoginSession session = BotManager.getInstance().getLoginSessions()
                    .get(player.getAddress().toString());
            if (session == null) {
                kickPlayer(packetEvent.getPlayer(), "Invalid Request");
            }
            else {
                response(session);
            }
        }
        finally {
            // This is a fake packet; it shouldn't be send to the server
            synchronized (packetEvent.getAsyncMarker().getProcessingLock()) {
                packetEvent.setCancelled(true);
            }

            ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(packetEvent);
        }
    }

    private void response(BukkitLoginSession session) {
        PrivateKey privateKey = plugin.getServerKey().getPrivate();

        SecretKey loginKey = EncryptionUtil.decryptSharedKey(privateKey, sharedSecret);
        if (!checkVerifyToken(session, privateKey) || !encryptConnection(loginKey)) {
            return;
        }

        /* This makes sure the request from the client is for us
           http://www.sk89q.com/2011/09/minecraft-name-spoofing-exploit/ */
        String generatedId = session.getServerId();

        /* Generate the server id based on client and server data
           https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/LoginListener.java#L193 */
        byte[] serverIdHash = EncryptionUtil.getServerIdHash(generatedId, plugin.getServerKey().getPublic(), loginKey);
        String serverId = (new BigInteger(serverIdHash)).toString(16);

        String username = session.getUsername();
        if (MojangAPI.getInstance().hasJoinedServer(session, serverId)) {
            session.setVerified(true);
            setPremiumUUID(Config.PROTECTION_OFFLINE ?
                    UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes()) : session.getUuid());
            receiveFakeStartPacket(username);
        }
        else {
            //user tried to fake a authentication
            kickPlayer(player, "Invalid Session");
        }
    }

    private void setPremiumUUID(UUID premiumUUID) {
        if (premiumUUID != null) {
            try {
                Object networkManager = getNetworkManager();
                // https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/NetworkManager.java#L69
                FieldUtils.writeField(networkManager, "spoofedUUID", premiumUUID, true);
            } catch (Exception exc) {
                plugin.getLogger().log(Level.SEVERE, "Error setting premium uuid", exc);
            }
        }
    }

    private boolean checkVerifyToken(BukkitLoginSession session, PrivateKey privateKey) {
        byte[] requestVerify = session.getVerifyToken();
        // Encrypted verify token
        byte[] responseVerify = packetEvent.getPacket().getByteArrays().read(1);

        // https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/LoginListener.java#L182
        if (!Arrays.equals(requestVerify, EncryptionUtil.decryptData(privateKey, responseVerify))) {
            // Check if the verify token are equal to the server sent one
            kickPlayer(player, "Invalid Verify Token");
            return false;
        }

        return true;
    }

    /**
     * Get the networkManager from ProtocolLib
     * @return networkManager
     */
    private Object getNetworkManager() throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        Object injectorContainer = TemporaryPlayerFactory.getInjectorFromPlayer(player);

        // ChannelInjector
        Class<?> injectorClass = Class.forName("com.comphenix.protocol.injector.netty.Injector");
        Object rawInjector = FuzzyReflection.getFieldValue(injectorContainer, injectorClass, true);
        return FieldUtils.readField(rawInjector, "networkManager", true);
    }

    private boolean encryptConnection(SecretKey loginKey) throws IllegalArgumentException {
        try {
            // Get the NMS connection handle of this player
            Object networkManager = getNetworkManager();

            // Try to detect the method by parameters
            Method encryptMethod = FuzzyReflection
                    .fromObject(networkManager).getMethodByParameters("a", SecretKey.class);

            // Encrypt/decrypt following packets
            // The client expects this behaviour
            encryptMethod.invoke(networkManager, loginKey);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't enable encryption", ex);
            kickPlayer(player, "Couldn't enable encryption");
            return false;
        }

        return true;
    }

    private void kickPlayer(Player player, String reason) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        PacketContainer kickPacket = protocolManager.createPacket(PacketType.Login.Server.DISCONNECT);
        kickPacket.getChatComponents().write(0, WrappedChatComponent.fromText(reason));

        try {
            // Send kick packet at login state
            // The normal event.getPlayer.kickPlayer(String) method does only work at play state
            protocolManager.sendServerPacket(player, kickPacket);
            // Tell the server that we want to close the connection
            player.kickPlayer("Disconnect");
        } catch (InvocationTargetException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error sending kickpacket", ex);
        }
    }

    /**
     * Fake a new login packet
     * @param username
     */
    private void receiveFakeStartPacket(String username) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        //  See StartPacketListener for packet information
        PacketContainer startPacket = protocolManager.createPacket(PacketType.Login.Client.START);

        // uuid is ignored by the packet definition
        WrappedGameProfile fakeProfile = new WrappedGameProfile(UUID.randomUUID(), username);
        startPacket.getGameProfiles().write(0, fakeProfile);
        try {
            // We don't want to handle our own packets so ignore filters
            protocolManager.recieveClientPacket(player, startPacket, false);
        }
        catch (InvocationTargetException | IllegalAccessException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to fake a new start packet", ex);
            // Cancel the event in order to prevent the server receiving an invalid packet
            kickPlayer(player, "Invalid Packet");
        }
    }
}
