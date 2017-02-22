package me.imTedzi.ABA.bungee.listeners;

import me.imTedzi.ABA.bungee.Main;
import me.imTedzi.ABA.bungee.managers.BotManager;
import me.imTedzi.ABA.bungee.managers.Config;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class PostLoginListener implements Listener {

    Main plugin;
    BotManager manager;

    public PostLoginListener(Main plugin, BotManager manager)
    {
        this.manager = manager;
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority=64)
    public void onPostLogin(PostLoginEvent event) {
        if(Config.PROTECTION_ENABLED) {
            if (manager.antiBotEnabled) {
                if (event.getPlayer().getPendingConnection().isOnlineMode()) {
                    if(Config.PROTECTION_OFFLINE) {
                        manager.bindUUID(event.getPlayer(), UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                                event.getPlayer().getName()).getBytes()));
                    }
                }
            }
        }
    }
}