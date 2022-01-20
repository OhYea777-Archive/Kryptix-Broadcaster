package com.ohyea777.kryptix;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Broadcaster extends JavaPlugin {

    public static Broadcaster INSTANCE;

    private Map<Integer, String> broadcasts;
    private int currentBroadcast;
    private int scheduleId;

    @Override
    public void onEnable() {
        INSTANCE = this;
        broadcasts = new HashMap<Integer, String>();
        currentBroadcast = 0;
        scheduleId = -1;

        saveResource("config.yml", false);
        reload();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
                if (sender.hasPermission("kryptix.reload")) {
                    reload();
                    sender.sendMessage(_(replacePrefix(getConfig().getString("Messages.Reloaded", "%prefix% Config Reloaded!"))));

                    return true;
                } else {
                    sender.sendMessage(_(replacePrefix(getConfig().getString("Messages.NoPermReload", "%prefix% &4You Do Not Have Permission to Reload the Config!"))));

                    return true;
                }
            } else if (args[0].equalsIgnoreCase("broadcast") || args[0].equalsIgnoreCase("b")) {
                sender.sendMessage(_(replacePrefix(getConfig().getString("Messages.Broadcast", "%prefix% To Broadcast a Message do&8: &e/pz b <message>"))));

                return true;
            } else {
                int index;

                try {
                    index = Integer.valueOf(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(_(replacePrefix(getConfig().getString("Messages.InvalidArguments", "%prefix% &4Invalid Arguments!"))));

                    return true;
                }

                if (sender.hasPermission("kryptix.broadcast")) {
                    if (broadcasts.containsKey(index - 1)) {
                        getServer().broadcast(_(replacePrefix(getConfig().getString("Options.BroadcastFormat", "%prefix% %broadcast%")).replace("%broadcast%", broadcasts.get(index - 1))), "kryptix.broadcast.receive");

                        return true;
                    } else {
                        sender.sendMessage(_(replacePrefix(getConfig().getString("Messages.InvalidBroadcast", "%prefix% &4No Broadcast Exists For That Index!"))));

                        return true;
                    }
                } else {
                    sender.sendMessage(_(replacePrefix(getConfig().getString("Messages.NoPermBroadcast", "%prefix% &4You Do Not Have Permission to Force a Broadcast!"))));

                    return true;
                }
            }
        } else if (args.length > 1 && (args[0].equalsIgnoreCase("broadcast") || args[0].equalsIgnoreCase("b"))) {
            if (sender.hasPermission("kryptix.broadcast")) {
                StringBuilder str = new StringBuilder(_(args[1]));

                if (args.length > 2) {
                    for (int i = 2; i < args.length; i ++) {
                        str.append(" ").append(_(args[i]));
                    }
                }

                getServer().broadcast(_(replacePrefix(getConfig().getString("Options.BroadcastFormat", "%prefix% %broadcast%")).replace("%broadcast%", str.toString())), "kryptix.broadcast.receive");

                return true;
            } else {
                sender.sendMessage(_(replacePrefix(getConfig().getString("Messages.NoPermBroadcastMessage", "%prefix% &4You Do Not Have Permission to Broadcast a Message!"))));

                return true;
            }
        }

        sender.sendMessage(_(replacePrefix("%prefix% To Reload the Config do&8: &e/pz reload")));
        sender.sendMessage(_(replacePrefix("%prefix% To Force a Broadcast do&8: &e/pz <1 to " + broadcasts.size() + "&e>")));
        sender.sendMessage(_(replacePrefix(getConfig().getString("Messages.Broadcast", "%prefix% To Broadcast a Message do&8: &e/pz b <message>"))));

        return true;
    }

    public String _(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private void reload() {
        reloadConfig();

        int i = 0;

        for (String broadcast : getConfig().getStringList("Broadcasts")) {
            broadcasts.put(i, broadcast);

            i += 1;
        }

        if (scheduleId != -1)
            getServer().getScheduler().cancelTask(scheduleId);

        scheduleId = getServer().getScheduler().scheduleSyncRepeatingTask(INSTANCE, new Runnable() {

            @Override
            public void run() {
                broadcastNextMessage();
            }

        }, getConfig().getLong("Options.BroadcastFrequency", 600), getConfig().getLong("Options.BroadcastFrequency", 600));
    }

    private void broadcastNextMessage() {
        if (currentBroadcast >= broadcasts.size()) {
            currentBroadcast = 0;
        }

        if (broadcasts.get(currentBroadcast) != null) {
            getServer().broadcast(_(replacePrefix(getConfig().getString("Options.BroadcastFormat", "%prefix% %broadcast%")).replace("%broadcast%", broadcasts.get(currentBroadcast))), "kryptix.broadcast.receive");
        }

        currentBroadcast += 1;
    }

    private String replacePrefix(String message) {
        return message.replace("%prefix%", getConfig().getString("Messages.Prefix", "&8[&6KryptixBroadcaster&8]&7"));
    }

}
