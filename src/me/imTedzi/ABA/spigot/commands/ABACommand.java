package me.imTedzi.ABA.spigot.commands;


import me.imTedzi.ABA.spigot.Main;
import me.imTedzi.ABA.spigot.managers.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ABACommand implements CommandExecutor {

    Main plugin;

    public ABACommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 1) {
            if(args[0].equals("reload")) {
                if(sender.hasPermission("aba.admin")) {
                    Config.reloadConfig();
                    sender.sendMessage(Config.color("&aConfig was reloaded"));
                }
                return true;
            }
            else if(args[0].equals("on")) {
                if(sender.hasPermission("aba.admin")) {
                    Config.PROTECTION_ENABLED = true;
                    Config.saveConfig();
                    sender.sendMessage(Config.color("&7ABA protection was &aenabled"));
                }
                return true;
            }
            else if(args[0].equals("off")) {
                if(sender.hasPermission("aba.admin")) {
                    Config.PROTECTION_ENABLED = false;
                    Config.saveConfig();
                    sender.sendMessage(Config.color("&7ABA protection was &cdisabled"));
                }
                return true;
            }
        }
        else if(args.length == 0) {
            if(sender.hasPermission("aba.admin")) {
                sender.sendMessage(Config.color("&a/aba on &7- Toggle on protection"));
                sender.sendMessage(Config.color("&a/aba off &7- Toggle off protection"));
                sender.sendMessage(Config.color("&a/aba reload &7- Reload plugin"));
                return true;
            }
            else {
                sender.sendMessage(Config.color("&cYou don't have permission"));
                return true;
            }
        }
        return false;
    }
}
