package me.imTedzi.ABA.spigot.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.imTedzi.ABA.spigot.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Thanks to games647
 *
 * Handles incoming encryption responses from connecting clients.
 * It prevents them from reaching the server because that cannot handle
 * it in offline mode.
 *
 * Moreover this manages spoofing the player uuid
 *
 * Receiving packet information:
 * http://wiki.vg/Protocol#Encryption_Response
 *
 * sharedSecret=encrypted byte array
 * verify token=encrypted byte array
 */
public class EncryptionPacketListener extends PacketAdapter {

    Main plugin;

    public EncryptionPacketListener(Main plugin) {
        super(params(plugin, PacketType.Login.Client.ENCRYPTION_BEGIN).optionAsync());
        this.plugin = plugin;
    }

    public static void register(Main plugin, int workerThreads) {
        ProtocolLibrary.getProtocolManager().getAsynchronousManager()
                .registerAsyncHandler(new EncryptionPacketListener(plugin)).start(workerThreads);
    }

    @Override
    public void onPacketReceiving(PacketEvent packetEvent) {
        if (packetEvent.isCancelled()) {
            return;
        }

        Player sender = packetEvent.getPlayer();
        byte[] sharedSecret = packetEvent.getPacket().getByteArrays().read(0);

        packetEvent.getAsyncMarker().incrementProcessingDelay();
        ResponseTask verifyTask = new ResponseTask(plugin, packetEvent, sender, sharedSecret);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, verifyTask);
    }
}
