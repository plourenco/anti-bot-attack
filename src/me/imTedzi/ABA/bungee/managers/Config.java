package me.imTedzi.ABA.bungee.managers;

import me.imTedzi.ABA.bungee.Main;
import net.md_5.bungee.config.Configuration;

import java.lang.reflect.Field;

public class Config {

    public static int LOGIN_MAX0LOGINS = 10;
    public static int LOGIN_PER0HOW0MUCH0SECONDS = 1;
    public static int LOGIN_ANTIBOT0TIME = 60;
    public static boolean LOGIN_DEBUG = false;
    public static String MESSAGES_UNABLE0TO0LOGIN = "&cUnable to login!";
    public static String MESSAGES_MOTD0PROTECTION0ENABLED = "Your-motd-firstline%newline%Your-motd-secondline";
    public static boolean PROTECTION_ENABLED = true;
    public static boolean PROTECTION_CHANGE0MOTD = true;
    public static boolean PROTECTION_OFFLINE = true;
    public static boolean PROTECTION_PING_ENABLED = true;
    public static int PROTECTION_PING_REFRESH0TIME = 300;
    public static int PROTECTION_PING_CACHE0SIZE = 1000;

    public static void loadConfig()
    {
        try {
            Main.plugin.loadConfig();
            Configuration c = Main.configuration;
            for (Field f : Config.class.getFields())
            {
                if (f.getName().startsWith("_"))
                    continue;
                if (isSet(c, "config." + f.getName().toLowerCase().replace("_", ".").replace("0", "-"))) {
                    f.set(null, c.get("config." + f.getName().toLowerCase().replace("_", ".").replace("0", "-")));
                }
            }
        }
        catch (Exception localException)
        {
        }
    }

    public static void saveConfig()
    {
        try {
            Configuration c = Main.configuration;
            for (Field f : Config.class.getFields())
            {
                if (f.getName().startsWith("_"))
                    continue;
                c.set("config." + f.getName().toLowerCase().replace("_", ".").replace("0", "-"), f.get(null));
            }
            Main.plugin.saveConfig();
        }
        catch (Exception localException)
        {
        }
    }

    public static void reloadConfig() {
        loadConfig();

    }

    public static boolean isSet(Configuration c, String o) {
        if(c.get(o) != null)
            return true;
        return false;
    }

    public static String color(String string)
    {
        return string.replaceAll("&([0-9a-z])", "§$1");
    }
}
