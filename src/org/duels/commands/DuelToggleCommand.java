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
public class DuelToggleCommand implements CommandExecutor{

    DuelsPlugin ref;
    public DuelToggleCommand(DuelsPlugin main){
        ref=main;
    }//constructor
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        ref.doDuels=!ref.doDuels;
        cs.sendMessage(ChatColor.RED+"doDuels set to "+ChatColor.YELLOW+""+ref.doDuels);
        return true;
    }
    
}
