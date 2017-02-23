package me.imTedzi.ABA.spigot.managers;

import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.nms.NMSAcessor;
import me.imTedzi.ABA.spigot.util.BukkitLoginSession;
import me.imTedzi.ABA.spigot.util.LRUCache;
import me.imTedzi.ABA.spigot.util.LoginSession;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

public class BotManager {

    private Main plugin;
    private BukkitRunnable task;
    private int loginspersec = 0;
    private boolean antiBotEnabled = false;
    private LRUCache<String, Long> hosts;
    private LRUCache<String, BukkitLoginSession> loginSession;

    private static BotManager instance;

    public BotManager(Main plugin) {
        instance = this;
        this.plugin = plugin;
        this.hosts = new LRUCache<String, Long>(Config.PROTECTION_CACHE0SIZE);
        this.loginSession = new LRUCache<String, BukkitLoginSession>(Config.PROTECTION_CACHE0SIZE);
    }

    public boolean isEnabled() {
        return antiBotEnabled;
    }

    public int getLoginsPerSec() {
        return loginspersec;
    }

    public void setAntiBot(boolean bot) {
        this.antiBotEnabled = bot;
    }

    public void incLoginsPerSec() { this.loginspersec++; }

    public void AntiBot() {
        new BukkitRunnable() {
            public void run() {
                if(loginspersec != 0)
                    loginspersec = 0;
            }
        }.runTaskTimer(Main.getInstance(), 0, Config.LOGIN_PER0HOW0MUCH0SECONDS * 20);
    }

    public void antiBotTiming() {
        if (task == null) {
            this.plugin.getLogger().log(Level.WARNING, "Anti-Bot was enabled.");
        }
        task = new BukkitRunnable() {
            public void run() {
                if (antiBotEnabled) {
                    antiBotEnabled = false;
                    plugin.getLogger().log(Level.WARNING, "Anti-Bot was disabled");
                    task = null;
                }
            }
        };
        task.runTaskLater(Main.getInstance(), Config.LOGIN_ANTIBOT0TIME * 20);
    }

    public void addPing(String host) {
        hosts.put(host, System.currentTimeMillis());
    }

    public boolean isPing(String host) {
        return hosts.containsKey(host) && hosts.get(host) +
                Config.PROTECTION_PING_REFRESH0TIME * 1000 <= System.currentTimeMillis();
    }

    public LRUCache<String, BukkitLoginSession> getLoginSessions() {
        return loginSession;
    }

    public static BotManager getInstance() {
        return instance;
    }
}
