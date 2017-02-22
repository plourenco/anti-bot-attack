package me.imTedzi.ABA.spigot.listeners;

import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.managers.BotManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    Main plugin;

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if(BotManager.getInstance().isEnabled()) {
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                    e.getPlayer().getName()).getBytes());

            if(!e.getPlayer().getUniqueId().equals(uuid)) {
                BotManager.getInstance().bindUUID(e.getPlayer(), uuid);
            }
        }
    }
}
