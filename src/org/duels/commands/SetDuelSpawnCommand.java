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
import org.bukkit.entity.Player;
import org.duels.DuelsPlugin;

/**
 *
 * @author main
 */
public class SetDuelSpawnCommand implements CommandExecutor{

    DuelsPlugin ref;
    
    public SetDuelSpawnCommand(DuelsPlugin main){
        ref=main;
    }//constructor method
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(strings.length<1)
            return false;
        if(strings[0].equals("1")){
            ref.Duelspawn1=((Player)cs).getLocation();
            ((Player)cs).sendMessage(ChatColor.RED+"Set First Duel Spawn Location.");
            return true;
        }
        if(strings[0].equals("2")){
            ref.Duelspawn2=((Player)cs).getLocation();
            ((Player)cs).sendMessage(ChatColor.RED+"Set Second Duel Spawn Location.");
            return true;
        }
        return false;
    }//onCommand
    
}
