package me.imTedzi.ABA.spigot.managers;

import com.google.common.cache.CacheLoader;
import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.nms.NMSAcessor;
import me.imTedzi.ABA.spigot.protocol.CompatibleCacheBuilder;
import me.imTedzi.ABA.spigot.protocol.LoginSession;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BotManager {

    private Main plugin;
    private BukkitRunnable task;
    private int loginspersec = 0;
    private boolean antiBotEnabled = false;
    private HashMap<String, Long> hosts = new HashMap<String, Long>();
    private final ConcurrentMap<String, LoginSession> loginSession = buildCache(1, -1);

    private static BotManager instance;

    public BotManager(Main plugin) {
        instance = this;
        this.plugin = plugin;
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
            setOnlineMode(true);
        }
        task = new BukkitRunnable() {
            public void run() {
                if (antiBotEnabled) {
                    antiBotEnabled = false;
                    setOnlineMode(false);
                    plugin.getLogger().log(Level.WARNING, "Anti-Bot was disabled");
                    task = null;
                }
            }
        };
        task.runTaskLater(Main.getInstance(), Config.LOGIN_ANTIBOT0TIME * 20);
    }

    public void refreshPing() {
        new BukkitRunnable() {

            public void run() {
                Iterator<Map.Entry<String, Long>> it = hosts.entrySet().iterator();
                while(it.hasNext()) {
                    if(it.next().getValue() + Config.PROTECTION_PING_REFRESH0TIME * 1000 > System.currentTimeMillis())
                        it.remove();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, Config.PROTECTION_PING_REFRESH0TIME * 20);
    }

    public void addPing(String host) {
        if(hosts.size() < Config.PROTECTION_PING_CACHE0SIZE)
            hosts.put(host, System.currentTimeMillis());
    }

    public boolean isPing(String host) {
        return hosts.containsKey(host) && hosts.get(host) +
                Config.PROTECTION_PING_REFRESH0TIME * 1000 <= System.currentTimeMillis();
    }

    public void setOnlineMode(boolean onlinemode) {
        try {
            NMSAcessor.setOnlineMode(Bukkit.getServer(), onlinemode);
        }
        catch (Exception e) {
            this.plugin.getLogger()
                    .warning("[ABACommand] Internal error for prevented protection from enabling");
        }
    }

    public ConcurrentMap<String, LoginSession> getLoginSessions() {
        return loginSession;
    }

    public static <K, V> ConcurrentMap<K, V> buildCache(int expireAfterWrite, int maxSize) {
        CompatibleCacheBuilder<Object, Object> builder = CompatibleCacheBuilder.newBuilder();

        if (expireAfterWrite > 0) {
            builder.expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES);
        }

        if (maxSize > 0) {
            builder.maximumSize(maxSize);
        }

        return builder.build(CacheLoader.from(() -> {
            throw new UnsupportedOperationException();
        }));
    }

    public static BotManager getInstance() {
        return instance;
    }
}
