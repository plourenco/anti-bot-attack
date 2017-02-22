package me.imTedzi.ABA.bungee.listeners;

import me.imTedzi.ABA.bungee.Main;
import me.imTedzi.ABA.bungee.managers.BotManager;
import me.imTedzi.ABA.bungee.managers.Config;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.logging.Level;

@Deprecated
public class PreLoginListener implements Listener {

    Main plugin;
    BotManager manager;

    public PreLoginListener(Main plugin, BotManager manager)
    {
        this.manager = manager;
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event) {
        if(Config.PROTECTION_ENABLED) {
            if (manager.antiBotEnabled) {
                if(manager.isPing(event.getConnection().getAddress().getHostName())) {
                    event.getConnection().setOnlineMode(false);
                    if (Config.LOGIN_DEBUG) {
                        plugin.getLogger().log(Level.INFO, "Found " +
                                event.getConnection().getAddress().getHostName() + " from pinglist");
                    }
                }
                return;
            }
            if (manager.loginspersec >= Config.LOGIN_MAX0LOGINS) {
                manager.antiBotEnabled = true;
                manager.antiBotTiming();
            }
            manager.loginspersec++;
        }
    }
}
