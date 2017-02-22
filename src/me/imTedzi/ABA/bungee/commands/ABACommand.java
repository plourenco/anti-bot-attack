package me.imTedzi.ABA.bungee.commands;

import me.imTedzi.ABA.bungee.Main;
import me.imTedzi.ABA.bungee.managers.Config;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class ABACommand extends Command {
    Main plugin;

    public ABACommand(Main plugin)
    {
        super("aba", null, new String[0]);
        this.plugin = plugin;
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 1) {
            if(args[0].equals("reload")) {
                if(sender.hasPermission("aba.admin")) {
                    Config.reloadConfig();
                    sender.sendMessage(Config.color("&aConfig was reloaded"));
                }
            }
            else if(args[0].equals("on")) {
                if(sender.hasPermission("aba.admin")) {
                    Config.PROTECTION_ENABLED = true;
                    Config.saveConfig();
                    sender.sendMessage(Config.color("&7ABA protection was &aenabled"));
                }
            }
            else if(args[0].equals("off")) {
                if(sender.hasPermission("aba.admin")) {
                    Config.PROTECTION_ENABLED = false;
                    Config.saveConfig();
                    sender.sendMessage(Config.color("&7ABA protection was &cdisabled"));
                }
            }
        }
        else if(args.length == 0) {
            if(sender.hasPermission("aba.admin")) {
                sender.sendMessage(Config.color("&a/aba on &7- Toggle on protection"));
                sender.sendMessage(Config.color("&a/aba off &7- Toggle off protection"));
                sender.sendMessage(Config.color("&a/aba reload &7- Reload plugin"));
            }
            else
                sender.sendMessage(Config.color("&cYou don't have permission"));
        }
    }
}
