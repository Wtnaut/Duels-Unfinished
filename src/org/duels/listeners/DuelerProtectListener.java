/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duels.listeners;

import com.SirBlobman.combatlogx.api.event.PlayerTagEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.duels.Duel;
import org.duels.DuelsPlugin;

/**
 *
 * @author main
 */
public class DuelerProtectListener implements Listener {
    
    public DuelsPlugin ref;

    public DuelerProtectListener(DuelsPlugin main) {
        this.ref = main;
    }//constructor
    
    @EventHandler
    public void onEntityDamagebyEntityEvent(EntityDamageByEntityEvent e){
        if(e.getEntity()instanceof Player){
            if(((Player)e.getEntity()).equals(this.ref.Dueler1)){
                if(!((Player)e.getDamager()).equals(this.ref.Dueler2))
                    e.setCancelled(true);
                else if(this.ref.Dueler1.getHealth()-e.getDamage()<1){
                    this.ref.finishDuel(this.ref.Dueler2);
                    e.setCancelled(true);
                }
            }else if(((Player)e.getEntity()).equals(this.ref.Dueler2)){
                if(!((Player)e.getDamager()).equals(this.ref.Dueler1))
                    e.setCancelled(true);
                else if(this.ref.Dueler2.getHealth()-e.getDamage()<1){
                    this.ref.finishDuel(this.ref.Dueler1);
                    e.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e){
        if((e.getPlayer().equals(this.ref.Dueler1)||e.getPlayer().equals(this.ref.Dueler2))&&!e.getPlayer().hasPermission("duel.moderator")){
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED+"Cannot execute commands while in a duel!");
        }
    }
    
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e){
        if(e.getPlayer().equals(this.ref.Dueler1))
            this.ref.finishDuel(this.ref.Dueler2);
        if(e.getPlayer().equals(this.ref.Dueler2))
            this.ref.finishDuel(this.ref.Dueler1);
    }
    
    @EventHandler
    public void onPlayerTagEvent(PlayerTagEvent e){
        for(Duel comp:this.ref.DuelQueue){
            if(comp.challenger.equals(e.getPlayer())||comp.recipient.equals(e.getPlayer())){
                this.ref.DuelQueue.remove(comp);
                comp.recipient.sendMessage(ChatColor.RED+"Your incoming Duel Invite has been Cancelled due to PVP initiation.");
                comp.challenger.sendMessage(ChatColor.RED+"Your outgoing Duel Invite has been Cancelled due to PVP initiation.");
            }
        }
        for(Duel comp:this.ref.Pending.keySet()){
            if(comp.challenger.equals(e.getPlayer())||comp.recipient.equals(e.getPlayer())){
                this.ref.Pending.remove(comp);
                comp.recipient.sendMessage(ChatColor.RED+"Your pending Duel has been Cancelled due to PVP initiation.");
                comp.challenger.sendMessage(ChatColor.RED+"Your pending Duel has been Cancelled due to PVP initiation.");
            }
        }
    }
}//Listener
