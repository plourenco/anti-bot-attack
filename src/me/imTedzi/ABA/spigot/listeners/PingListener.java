package me.imTedzi.ABA.spigot.listeners;

import me.imTedzi.ABA.spigot.managers.Config;
import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.managers.BotManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.logging.Level;

public class PingListener implements Listener {

    Main plugin;

    public PingListener(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if(BotManager.getInstance().isEnabled()) {
            if(Config.PROTECTION_PING_ENABLED)
            {
                if(!BotManager.getInstance().isPing(event.getAddress().getHostName())) {
                   BotManager.getInstance().addPing(event.getAddress().getHostName());
                    if(Config.LOGIN_DEBUG) {
                        plugin.getLogger().log(Level.INFO, "Added " +
                                event.getAddress().getHostName() + " to pinglist");
                    }
                }

            }
            if(Config.PROTECTION_CHANGE0MOTD)
            {
                String s = Config.color(Config.MESSAGES_MOTD0PROTECTION0ENABLED).replaceAll("%newline%", "\n");

                event.setMotd(s);
            }
        }
    }
}
