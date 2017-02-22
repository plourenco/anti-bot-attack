package me.imTedzi.ABA.bungee.listeners;

import me.imTedzi.ABA.bungee.Main;
import me.imTedzi.ABA.bungee.managers.BotManager;
import me.imTedzi.ABA.bungee.managers.Config;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.logging.Level;

public class PingListener implements Listener {

    Main plugin;
    BotManager manager;

    public PingListener(Main plugin, BotManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority=64)
    public void onPing(ProxyPingEvent event)
    {
        if(manager.antiBotEnabled)
        {
            if(Config.PROTECTION_PING_ENABLED)
            {
                if(!manager.isPing(event.getConnection().getAddress().getHostName())) {
                    manager.addPing(event.getConnection().getAddress().getHostName());
                    if(Config.LOGIN_DEBUG) {
                        plugin.getLogger().log(Level.INFO, "Added " +
                                event.getConnection().getAddress().getHostName() + " to pinglist");
                    }
                }

            }
            if(Config.PROTECTION_CHANGE0MOTD)
            {
                ServerPing sp = event.getResponse();

                String s = Config.color(Config.MESSAGES_MOTD0PROTECTION0ENABLED).replaceAll("%newline%", "\n");

                sp.setDescription(s);

                event.setResponse(sp);
            }
        }
    }
}
