/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simplyian.mc.rad;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author simplyianm
 */
public class RAD extends JavaPlugin {
    static final Logger log = Logger.getLogger("Minecraft");
    private final RDPL pl = new RDPL(this);
    public HashMap<String, Block> pBlock = new HashMap<String, Block>();
    public static HashMap<String, RADZone> zones = new HashMap<String, RADZone>();
    public HashMap<String, List<Location>> selections = new HashMap<String, List<Location>>();
    public static File configFile = new File("plugins/RAD/config.yml");
    public static PermissionHandler permissionHandler;
    
    @Override
    public void onEnable() {
        setupPermissions();
        new File("plugins" + File.separator + "RAD").mkdir();
        if (!configFile.exists()) {
            toConsole("Config file not found, generating one right now...");
            try {
                if (configFile.createNewFile()) toConsole("File created!");
            } catch (IOException ex) {
                Logger.getLogger(RAD.class.getName()).log(Level.SEVERE, null, ex);
                toConsole("File not created!");
            }
        }
        Configuration config = new Configuration(configFile);
        loadRADZones();
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new RADTimer(), 600, 600);
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_MOVE, pl, Event.Priority.Normal, this);
        toConsole("Plugin enabled.");
    }
    
    @Override
    public void onDisable() {
        toConsole("Plugin disabled.");
    }
    
    public class RADTimer implements Runnable {
        @Override
        public void run() {
            List<Player> players = getAllInRADZones();
            for (Player radguy : players) {
                radguy.damage(1);
            }
        }
    }
    
    private void setupPermissions() {
        if (permissionHandler != null) return;
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
        if (permissionsPlugin == null) {
            toConsole("Permission system not detected, defaulting to OP");
            return;
        }
        permissionHandler = ((Permissions) permissionsPlugin).getHandler();
        toConsole("Found and will use plugin "+((Permissions)permissionsPlugin).getDescription().getFullName());
    }
    
    private List<Player> getAllInRADZones() {
        Player[] online = this.getServer().getOnlinePlayers();
        List<Player> radiated = new ArrayList<Player>();
        for (Player guy: online) {
            if (!hasPermission(guy, "rad.immune")) {
                //guy.sendMessage("You aren't immune!");
                for (RADZone zone : RAD.zones.values()) {
                    if (zone.contains(guy.getLocation())) radiated.add(guy);
                }
            }
        }
        return radiated;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (hasPermission(player, "rad.create")) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("select")) {
                        List<Location> playerSelection;
                        if (selections.containsKey(player.getName()) == false) {
                            playerSelection = new ArrayList<Location>();
                        } else {
                            playerSelection = selections.get(player.getName());
                        }
                        if (playerSelection.size() == 2) playerSelection.remove(0);
                        Location set = player.getLocation();
                        playerSelection.add(set);
                        selections.put(player.getName(), playerSelection);
                        sender.sendMessage("Selection made at " + set.getBlockX() + ", " + set.getBlockZ());
                    } else if (args[0].equalsIgnoreCase("set")) {
                        if (selections.get(player.getName()) != null && selections.get(player.getName()).size() == 2) {
                            String zname;
                            if (args.length <= 1) {
                                sender.sendMessage("Generating a random name...");
                                Random randy = new Random(player.getLocation().toString().length());
                                zname = "Rand" + randy.nextInt(1000000);
                            } else {
                                zname = args[1];
                            }
                            if (selections.containsKey(zname)) {
                                zname = zname + ".0";
                            }
                            if (selections.get(player.getName()).get(0).getWorld().getName().equals(selections.get(player.getName()).get(1).getWorld().getName())) {
                                List<Location> playerSelection = selections.get(player.getName());
                                Location min = playerSelection.get(0);
                                Location max = playerSelection.get(1);
                                if (min.getBlockX() > max.getBlockX()) {
                                    int newMaxX = min.getBlockX();
                                    min.setX(max.getBlockX());
                                    max.setX(newMaxX);
                                }
                                if (min.getBlockZ() > max.getBlockZ()) {
                                    int newMaxZ = min.getBlockZ();
                                    min.setZ(max.getBlockZ());
                                    max.setZ(newMaxZ);
                                }
                                sender.sendMessage("Creating the zone " + zname + " at " + min.getBlockX() + ", " + min.getBlockZ() + " to " + max.getBlockX() + ", " + max.getBlockZ() + ".");
                                insertZone(zname, min, max);
                            }
                        } else {
                            sender.sendMessage("Your selection is incomplete. Define more points.");
                        }
                    } else if (args[0].equalsIgnoreCase("delete")) {
                        for (RADZone lazone: zones.values()) {
                            if (lazone.contains(player.getLocation())) {
                                sender.sendMessage("Deleting zone " + lazone.getName());
                                Configuration config = new Configuration(configFile);
                                config.load();
                                config.removeProperty("radzones." + lazone.getName());
                                config.save();
                                zones.remove(lazone.getName());
                            }
                        }
                    } else {
                        sender.sendMessage("Available commands: select, set, delete");
                    }
                } else {
                    sender.sendMessage("Please input a command for /rad. (select, set, delete)");
                }
            } else {
                sender.sendMessage("You don't have permission to do that.");
            }
        } else {
            sender.sendMessage("In-game only, sorry.");
        }
        return true;
    }
    
    public static void toConsole(String message) {
        log.info("[RAD] " + message);
    }
    
    public static void loadRADZones() {
        Configuration config = new Configuration(configFile);
        config.load();
        List<String> radZones = config.getKeys("radzones");
        if (radZones != null) {
            Iterator it = radZones.iterator();
            while (it.hasNext()) {
                String zoneName = (String) it.next();
                zones.put(zoneName, new RADZone(zoneName));
            }
        }
        config.save();
    }
    
    public static boolean hasPermission(Player player, String permission) {
        return permissionHandler.has(player, permission) ? true : false;
    }
    
    public void insertZone(String zoneName, Location min, Location max) {
        Configuration config = new Configuration(configFile);
        config.load();
        String worldName = min.getWorld().getName();
        config.setProperty("radzones." + zoneName + ".world", worldName);
        config.setProperty("radzones." + zoneName + ".minx", min.getBlockX());
        config.setProperty("radzones." + zoneName + ".maxx", max.getBlockX());
        config.setProperty("radzones." + zoneName + ".minz", min.getBlockZ());
        config.setProperty("radzones." + zoneName + ".maxz", max.getBlockZ());
        config.save();
        zones.put(zoneName, new RADZone(zoneName));
        //Hey guys, my config file isn't generating properly, can anyone take a look? http://pastebin.com/y3qdPsqH The generation code is at the bottom; it returns a YML file with null and blank values
    }
}