package me.imTedzi.ABA.spigot.listeners;

import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.managers.BotManager;
import me.imTedzi.ABA.spigot.managers.Config;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;
import java.util.logging.Level;

public class PreLoginListener implements Listener {

    Main plugin;

    public PreLoginListener(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        if(Config.PROTECTION_ENABLED) {
            /* If we're under attack, the protection kicks in */
            if (BotManager.getInstance().isEnabled()) {
                if(Config.PROTECTION_PING_ENABLED) {
                    if (BotManager.getInstance().isPing(e.getAddress().getHostName())) {
                        e.allow();
                        if (Config.LOGIN_DEBUG) {
                            plugin.getLogger().log(Level.INFO, "Found " +
                                    e.getAddress().getHostName() + " from pinglist");
                        }
                    }
                    else {
                        /* Disconnect the player */
                        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Anti-Bot Protection");
                    }
                }
                /* Otherwise online mode will get it */
                return;
            }
            /* Counting the number of attacks until the protection */
            if (BotManager.getInstance().getLoginsPerSec() >= Config.LOGIN_MAX0LOGINS) {
                BotManager.getInstance().setAntiBot(true);
                BotManager.getInstance().antiBotTiming();
            }
            BotManager.getInstance().incLoginsPerSec();
        }
    }
}