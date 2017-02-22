package me.imTedzi.ABA.bungee.listeners;

import me.imTedzi.ABA.bungee.Main;
import me.imTedzi.ABA.bungee.managers.BotManager;
import me.imTedzi.ABA.bungee.managers.Config;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.logging.Level;

public class HandShakeListener implements Listener {

    Main plugin;
    BotManager manager;

    public HandShakeListener(Main plugin, BotManager manager)
    {
        this.manager = manager;
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHandShake(PlayerHandshakeEvent e) {
        if(Config.PROTECTION_ENABLED) {
            if(e.getHandshake().getRequestedProtocol() == 2) {
                /* If we're under attack, the protection kicks in */
                if (manager.antiBotEnabled) {
                    if(Config.PROTECTION_PING_ENABLED) {
                        if (manager.isPing(e.getConnection().getAddress().getHostName())) {
                            e.getConnection().setOnlineMode(false);
                            if (Config.LOGIN_DEBUG) {
                                plugin.getLogger().log(Level.INFO, "Found " +
                                        e.getConnection().getAddress().getHostName() + " from pinglist");
                            }
                        }
                        else {
                            /* Disconnect the player with protocol "Wrong Version" */
                            e.getHandshake().setProtocolVersion(-1);
                        }
                    }
                    /* Otherwise Online mode will get it */
                    return;
                }
                /* Counting the number of attacks until the protection */
                if (manager.loginspersec >= Config.LOGIN_MAX0LOGINS) {
                    manager.antiBotEnabled = true;
                    manager.antiBotTiming();
                }
                manager.loginspersec++;
            }
        }
    }
}
