/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simplyian.mc.rad;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author simplyianm
 */
public class RADZone {
    private String name;
    private String world;
    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;
    
    public RADZone(String zoneName) {
        Configuration config = new Configuration(RAD.configFile);
        config.load();
        this.name = zoneName;
        this.world = config.getString("radzones." + zoneName + ".world");
        this.minX = config.getInt("radzones." + zoneName + ".minx", 0);
        this.maxX = config.getInt("radzones." + zoneName + ".maxx", 0);
        this.minZ = config.getInt("radzones." + zoneName + ".minz", 0);
        this.maxZ = config.getInt("radzones." + zoneName + ".maxz", 0);
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getWorld() {
        return this.world;
    }
    
    public int getMinX() {
        return this.minX;
    }    
    
    public int getMaxX() {
        return this.maxX;
    }
    
    public int getMinZ() {
        return this.minZ;
    }    
    
    public int getMaxZ() {
        return this.maxZ;
    }
    
    public boolean contains(Location loc) {
        int theX = loc.getBlockX();
        int theZ = loc.getBlockZ();
        return theX >= this.getMinX() && theX <= this.getMaxX() && theZ >= this.getMinZ() && theZ <= this.getMaxZ();
    }
}