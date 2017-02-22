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
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.logging.Level;

/**
 *  Thanks to games647
 */
public class ResponseTask implements Runnable {

    private final Main plugin;
    private final PacketEvent packetEvent;
    private final Player fromPlayer;
    private final byte[] sharedSecret;

    public ResponseTask(Main plugin, PacketEvent packetEvent, Player fromPlayer, byte[] sharedSecret) {
        this.plugin = plugin;
        this.packetEvent = packetEvent;
        this.fromPlayer = fromPlayer;
        this.sharedSecret = sharedSecret;
    }

    @Override
    public void run() {
        try {
            LoginSession session = BotManager.getInstance()
                    .getLoginSessions().get(fromPlayer.getAddress().toString());
            if (session == null) {
                disconnect("invalid-request", true, "Player " + fromPlayer.getAddress() +
                        " tried to send encryption response at invalid state");
            } else {
                response(session);
            }
        }
        finally {
            //this is a fake packet; it shouldn't be send to the server
            synchronized (packetEvent.getAsyncMarker().getProcessingLock()) {
                packetEvent.setCancelled(true);
            }
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(packetEvent);
        }
    }

    private void response(LoginSession session) {
        String username = session.getUsername();
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                username).getBytes());

        System.out.println("Setting spoofed uuid and receiving packet " + username);

        setSpoofedUUID(uuid);
        receiveFakeStartPacket(username);
    }

    private void setSpoofedUUID(UUID spoofedUUID) {
        if (spoofedUUID != null) {
            try {
                Object networkManager = getNetworkManager();
                FieldUtils.writeField(networkManager, "spoofedUUID", spoofedUUID, true);
            }
            catch (Exception exc) {
                plugin.getLogger().log(Level.SEVERE, "Error setting premium uuid", exc);
            }
        }
    }

    /**
     * Try to get the networkManager from ProtocolLib
     */
    private Object getNetworkManager() throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        Object injectorContainer = TemporaryPlayerFactory.getInjectorFromPlayer(fromPlayer);

        //ChannelInjector
        Class<?> injectorClass = Class.forName("com.comphenix.protocol.injector.netty.Injector");
        Object rawInjector = FuzzyReflection.getFieldValue(injectorContainer, injectorClass, true);
        return FieldUtils.readField(rawInjector, "networkManager", true);
    }

    private void disconnect(String kickReason, boolean debug, String logMessage, Object... arguments) {
        if (debug) {
            plugin.getLogger().log(Level.FINE, logMessage, arguments);
        } else {
            plugin.getLogger().log(Level.SEVERE, logMessage, arguments);
        }

        kickPlayer(packetEvent.getPlayer(), kickReason);
    }

    private void kickPlayer(Player player, String reason) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        PacketContainer kickPacket = protocolManager.createPacket(PacketType.Login.Server.DISCONNECT);
        kickPacket.getChatComponents().write(0, WrappedChatComponent.fromText(reason));

        try {
            //send kick packet at login state
            //the normal event.getPlayer.kickPlayer(String) method does only work at play state
            protocolManager.sendServerPacket(player, kickPacket);
            //tell the server that we want to close the connection
            player.kickPlayer("Disconnect");
        } catch (InvocationTargetException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error sending kickpacket", ex);
        }
    }

    /**
     * Fake a new login packet in order to let the server handle all the other stuff
     * @param username
     */
    private void receiveFakeStartPacket(String username) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        
        //see StartPacketListener for packet information
        PacketContainer startPacket = protocolManager.createPacket(PacketType.Login.Client.START);

        //uuid is ignored by the packet definition
        WrappedGameProfile fakeProfile = new WrappedGameProfile(UUID.randomUUID(), username);
        startPacket.getGameProfiles().write(0, fakeProfile);
        try {
            //we don't want to handle our own packets so ignore filters
            protocolManager.recieveClientPacket(fromPlayer, startPacket, false);
        }
        catch (InvocationTargetException | IllegalAccessException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to fake a new start packet", ex);
            //cancel the event in order to prevent the server receiving an invalid packet
            kickPlayer(fromPlayer, "Unable to connect");
        }
    }
}
