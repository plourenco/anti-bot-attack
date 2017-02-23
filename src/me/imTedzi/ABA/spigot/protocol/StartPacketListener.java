package me.imTedzi.ABA.spigot.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.managers.BotManager;
import me.imTedzi.ABA.spigot.util.LoginSession;
import me.imTedzi.ABA.spigot.util.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.security.PublicKey;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Credits to games467
 *
 * Handles incoming start packets from connecting clients. It
 * checks if we can start checking if the player is premium and
 * start a request to the client that it should start online mode
 * login.
 *
 * Receiving packet information:
 * http://wiki.vg/Protocol#Login_Start
 *
 * String=Username
 */
public class StartPacketListener extends PacketAdapter {

    private final Main plugin;
    private final Random random = new Random();

    public StartPacketListener(Main plugin) {
        super(params(plugin, PacketType.Login.Client.START).optionAsync());
        this.plugin = plugin;
    }

    public static void register(Main plugin, int threads) {
        ProtocolLibrary.getProtocolManager().getAsynchronousManager()
                .registerAsyncHandler(new StartPacketListener(plugin)).start(threads);
    }

    /**
     * C->S : Handshake State=2
     * C->S : Login Start
     * S->C : Encryption Key Request
     * (Client Auth)
     * C->S : Encryption Key Response
     * (Server Auth, Both enable encryption)
     * S->C : Login Success (*)
     */
    @Override
    public void onPacketReceiving(PacketEvent packetEvent) {
        if (packetEvent.isCancelled()
                || !BotManager.getInstance().isEnabled()) {
            return;
        }

        Player player = packetEvent.getPlayer();

        // This includes ip:port. Should be unique for an incoming login request with a timeout of 2 minutes
        String sessionKey = player.getAddress().toString();

        // Remove old data every time on a new login in order to keep the session only for one person
        BotManager.getInstance().getLoginSessions().remove(sessionKey);

        // player.getName() won't work at this state
        PacketContainer packet = packetEvent.getPacket();

        String username = packet.getGameProfiles().read(0).getName();

        packetEvent.getAsyncMarker().incrementProcessingDelay();
        LoginFilterTask task = new LoginFilterTask(plugin, packetEvent, random, player, username);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
}
