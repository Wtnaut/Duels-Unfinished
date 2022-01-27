/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duels.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.duels.DuelsPlugin;

/**
 *
 * @author main
 */
public class EloChangeCommand implements CommandExecutor{

    DuelsPlugin ref;
    public EloChangeCommand(DuelsPlugin main){
        ref=main;
    }//constructor
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(strings.length>=2){
            try{
                int x= Integer.parseInt(strings[1]);
                ref.Elo.put(ref.getServer().getOfflinePlayer(strings[0]), x);
            }catch(Exception e){
                cs.sendMessage(ChatColor.RED+"[Duels] Command Failed.");
            }
        }
        else
            cs.sendMessage(ChatColor.RED+"[Duels] Command Failed.");
        
        return true;
    }
    
    
}
