package me.imTedzi.ABA.spigot.protocol;

import com.comphenix.protocol.events.PacketEvent;
import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.managers.BotManager;
import me.imTedzi.ABA.spigot.util.BukkitLoginSession;
import me.imTedzi.ABA.spigot.util.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.logging.Level;

public class LoginFilterTask implements Runnable {

    private final Main plugin;
    private final PacketEvent packetEvent;
    private final Random random;
    private final Player player;
    private final String username;

    public LoginFilterTask(Main plugin, PacketEvent packetEvent, Random random, Player player, String username) {
        this.plugin = plugin;
        this.packetEvent = packetEvent;
        this.random = random;
        this.player = player;
        this.username = username;
    }

    @Override
    public void run() {
        premiumLogin(new ProtocolLibLoginSource(plugin, packetEvent, player, random), username);
    }

    public void premiumLogin(ProtocolLibLoginSource source, String username) {
        try {
            source.setOnlineMode();
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Cannot send encryption packet", ex);
            return;
        }

        String ip = player.getAddress().getAddress().getHostAddress();

        String serverId = source.getServerId();
        byte[] verify = source.getVerifyToken();

        BukkitLoginSession playerSession = new BukkitLoginSession(username, serverId, verify);
        BotManager.getInstance().getLoginSessions().put(player.getAddress().toString(), playerSession);
        // Cancel only if the player has a paid account
        synchronized (packetEvent.getAsyncMarker().getProcessingLock()) {
            packetEvent.setCancelled(true);
        }
    }
}
