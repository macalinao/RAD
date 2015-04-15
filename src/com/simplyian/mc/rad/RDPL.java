/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simplyian.mc.rad;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author simplyianm
 */
class RDPL extends PlayerListener {
    private static RAD rd;
    
    public RDPL(RAD instance) {
        rd = instance;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        String pname = player.getName();
        Block oldBlock;
        Block newBlock = player.getLocation().getBlock();
        oldBlock = rd.pBlock.containsKey(pname) == true ? rd.pBlock.get(pname) : newBlock;
        rd.pBlock.put(pname, newBlock);
        if (oldBlock != newBlock) {
            RADZone currentZone = null;
            RADZone oldZone = null;
            for (RADZone zone : RAD.zones.values()) {
                if (zone.contains(newBlock.getLocation()) == true) {
                    currentZone = zone;
                }
                if (zone.contains(oldBlock.getLocation()) == true) {
                    oldZone = zone;
                }
            }
            if (oldZone == null && currentZone == null) { //Walking through wilderness
                //player.sendMessage("You are in wilderness.");
            } else if (oldZone == null && currentZone != null) { //Into a RADZone 
                player.sendMessage(ChatColor.RED + "You have entered a radiated zone.");
            } else if (oldZone != null && currentZone == null) { //Out of a RADZone
                player.sendMessage(ChatColor.RED + "You have left a radiated zone.");
            } else if (oldZone.equals(currentZone)) { //Moving through a plot
                //player.sendMessage("You are moving through a radiated zone.");
            } else if (!oldZone.equals(currentZone)) { //Moving between plots
                //player.sendMessage("You have transferred betwen radiated zones.");
            } else {
                RAD.toConsole("onPlayerMove error");
                player.sendMessage("Error!");
            }
        }
    }
}
