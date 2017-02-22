package me.imTedzi.ABA.bungee.managers;

import me.imTedzi.ABA.bungee.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.conf.Configuration;
import net.md_5.bungee.connection.InitialHandler;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BotManager {

    public Main plugin;
    public ScheduledTask task;
    public int loginspersec = 0;
    public boolean antiBotEnabled = false;
    public HashMap<String, Long> hosts = new HashMap<String, Long>();

    public BotManager(Main plugin) {
        this.plugin = plugin;
    }

    public void AntiBot() {
        plugin.getProxy().getScheduler().schedule(plugin, new Runnable()
        {
            public void run() {
                if(loginspersec != 0)
                    loginspersec = 0;
            }
        }, 0, Config.LOGIN_PER0HOW0MUCH0SECONDS, TimeUnit.SECONDS);
    }

    public void antiBotTiming() {
        if (task == null) {
            plugin.getLogger().log(Level.WARNING, "Anti-Bot was enabled.");
            changeOnlineMode(true);
        }

        task = plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            public void run() {
                if (antiBotEnabled) {
                    antiBotEnabled = false;
                    changeOnlineMode(false);
                    plugin.getLogger().log(Level.WARNING, "Anti-Bot was disabled");
                    task = null;
                }
            }
        }, Config.LOGIN_ANTIBOT0TIME, TimeUnit.SECONDS);
    }

    public void refreshPing() {
        plugin.getProxy().getScheduler().schedule(plugin, new Runnable()
        {
            public void run() {
                Iterator<Map.Entry<String, Long>> it = hosts.entrySet().iterator();
                while(it.hasNext()) {
                    if(it.next().getValue() + Config.PROTECTION_PING_REFRESH0TIME*1000 > System.currentTimeMillis())
                        it.remove();
                }
            }
        }, 0, Config.PROTECTION_PING_REFRESH0TIME, TimeUnit.SECONDS);
    }

    public void addPing(String host) {
        if(hosts.size() < Config.PROTECTION_PING_CACHE0SIZE)
            hosts.put(host, System.currentTimeMillis());
    }

    public boolean isPing(String host) {
        return hosts.containsKey(host) && hosts.get(host) +
                Config.PROTECTION_PING_REFRESH0TIME*1000 <= System.currentTimeMillis();
    }

    public boolean bindUUID(ProxiedPlayer p, UUID uuid) {
        try
        {
            InitialHandler handler = (InitialHandler)p.getPendingConnection();

            Field sf = handler.getClass().getDeclaredField("uniqueId");
            sf.setAccessible(true);
            sf.set(handler, uuid);

            sf = handler.getClass().getDeclaredField("offlineId");
            sf.setAccessible(true);
            sf.set(handler, uuid);

            Collection<String> g = this.plugin.getProxy().getConfigurationAdapter().getGroups(p.getName());
            g.addAll(this.plugin.getProxy().getConfigurationAdapter().getGroups(p.getUniqueId().toString()));

            UserConnection userConnection = (UserConnection)p;

            for (String s : g) {
                userConnection.addGroups(s);
            }

            return true;
        }
        catch (Exception e)
        {
            p.disconnect(Config.color(Config.MESSAGES_UNABLE0TO0LOGIN));

            this.plugin.getLogger().warning("[ABACommand] Internal error for " + p.getName() + ", preventing login.");

            e.printStackTrace();

            return false;
        }
    }

    public void changeOnlineMode(boolean onlinemode) {
        try {
            Configuration handler = BungeeCord.getInstance().config;

            Field sf = null;
            sf = handler.getClass().getDeclaredField("onlineMode");
            sf.setAccessible(true);
            sf.set(handler, onlinemode);
        } catch (Exception e) {
            this.plugin.getLogger().warning("[ABACommand] Internal error for prevented protection from enabling");
        }
    }
}
