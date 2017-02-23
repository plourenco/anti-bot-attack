package me.imTedzi.ABA.spigot.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.util.LoginSource;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.Random;

public class ProtocolLibLoginSource implements LoginSource {

    private final Main plugin;

    private final PacketEvent packetEvent;
    private final Player player;
    private final Random random;

    private String serverId;
    private static final int VERIFY_TOKEN_LENGTH = 4;
    private final byte[] verifyToken = new byte[VERIFY_TOKEN_LENGTH];

    public ProtocolLibLoginSource(Main plugin, PacketEvent packetEvent, Player player, Random random) {
        this.plugin = plugin;
        this.packetEvent = packetEvent;
        this.player = player;
        this.random = random;
    }

    @Override
    public void setOnlineMode() throws Exception {
        /* Randomized server id to make sure the request is for our server
           http://www.sk89q.com/2011/09/minecraft-name-spoofing-exploit/ */
        serverId = Long.toString(random.nextLong(), 16);

        // Generate a random token which should be the same when we receive it from the client
        random.nextBytes(verifyToken);
        
        sentEncryptionRequest();
    }

    @Override
    public void kick(String message) throws Exception {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        PacketContainer kickPacket = protocolManager.createPacket(PacketType.Login.Server.DISCONNECT);
        kickPacket.getChatComponents().write(0, WrappedChatComponent.fromText(message));

        try {
            // Send kick packet at login state
            // The normal event.getPlayer.kickPlayer(String) method does only work at play state
            protocolManager.sendServerPacket(player, kickPacket);
        }
        finally {
            // Tell the server that we want to close the connection
            player.kickPlayer("Disconnect");
        }
    }

    @Override
    public InetSocketAddress getAddress() {
        return packetEvent.getPlayer().getAddress();
    }

    /**
     * Only online players send encryption request, this will force to create one
     * Packet Information: http://wiki.vg/Protocol#Encryption_Request
     *
     * ServerID="" (String) key=public server key verifyToken=random 4 byte array
     */
    private void sentEncryptionRequest() throws InvocationTargetException {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        PacketContainer newPacket = protocolManager.createPacket(PacketType.Login.Server.ENCRYPTION_BEGIN);

        newPacket.getStrings().write(0, serverId);
        newPacket.getSpecificModifier(PublicKey.class).write(0, plugin.getServerKey().getPublic());

        newPacket.getByteArrays().write(0, verifyToken);

        // serverId is a empty string
        protocolManager.sendServerPacket(player, newPacket);
    }

    public String getServerId() {
        return serverId;
    }

    public byte[] getVerifyToken() {
        return verifyToken;
    }
}
