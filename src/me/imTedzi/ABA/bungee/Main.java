package me.imTedzi.ABA.bungee;

import me.imTedzi.ABA.bungee.commands.ABACommand;
import me.imTedzi.ABA.bungee.listeners.HandShakeListener;
import me.imTedzi.ABA.bungee.listeners.PingListener;
import me.imTedzi.ABA.bungee.listeners.PostLoginListener;
import me.imTedzi.ABA.bungee.managers.BotManager;
import me.imTedzi.ABA.bungee.managers.Config;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.logging.Level;

public class Main extends Plugin {

    public static Main plugin;
    public static BotManager manager;
    public static Configuration configuration;
    public static File configFile;

    @Override
    public void onEnable() {
        plugin = this;
        manager = new BotManager(this);
        manager.AntiBot();
        new PostLoginListener(this, manager);
        new PingListener(this, manager);
        new HandShakeListener(this, manager);
        new ABACommand(this);
        Config.loadConfig();
        if(Config.PROTECTION_ENABLED) {
            this.getLogger().log(Level.INFO, "Protection is " + ChatColor.GREEN + "enabled " +
                    "(" + getDescription().getVersion() + ")");
        }
        else {
            this.getLogger().log(Level.INFO, "Protection is " + ChatColor.GREEN + "not enabled " +
                    "(" + getDescription().getVersion() + ")");
        }
    }

    public void loadConfig() throws IOException {
        if(!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        if(configFile == null)
            configFile = new File(getDataFolder(), "config.yml");

        if(!configFile.exists()) {
            copy(getResourceAsStream("config.yml"), configFile);
        }

        configuration =
                ConfigurationProvider.getProvider(YamlConfiguration.class).
                        load(configFile);
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).
                save(configuration, configFile);
    }
}
