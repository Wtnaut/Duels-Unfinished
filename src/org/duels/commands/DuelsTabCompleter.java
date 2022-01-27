/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duels.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.duels.DuelsPlugin;

/**
 *
 * @author main
 */
public class DuelsTabCompleter implements TabCompleter{

    public DuelsPlugin ref;
    public DuelsTabCompleter(DuelsPlugin main){
        ref=main;
    }//constructor
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String string, String[] strings) {
        List<String> toReturn = new ArrayList<String>();
        if(cmnd.getName().equals("duel")){
            if(strings.length==1){
                toReturn.add("accept");
                toReturn.add("cancel");
                toReturn.add("challenge");
                toReturn.add("help");
                toReturn.add("ignore");
                toReturn.add("practice");
                toReturn.add("rank");
                toReturn.add("top");
                
                if(((Player)cs).hasPermission("duel.moderator")){
                    toReturn.add("canduel");
                    toReturn.add("ban");
                    toReturn.add("unban");
                }
                
            }else if(strings.length==2){
                if(strings[0].equalsIgnoreCase("challenge")||strings[0].equalsIgnoreCase("practice")||strings[0].equalsIgnoreCase("ban")&&((Player)cs).hasPermission("duel.moderator")||strings[0].equalsIgnoreCase("unban")&&((Player)cs).hasPermission("duel.moderator")||strings[0].equalsIgnoreCase("canduel")&&((Player)cs).hasPermission("duel.moderator")){
                    for(Player p:ref.getServer().getOnlinePlayers())
                        toReturn.add(p.getName());
                }
            }//inner if tree
        }else if(cmnd.getName().equals("dueltoggle")){
            
        }else if(cmnd.getName().equals("elochange")){
            if(strings.length<2){
            for(Player p:ref.getServer().getOnlinePlayers())
                        toReturn.add(p.getName());
            }
        }else if(cmnd.getName().equals("setduelspawn")&&strings.length<2){
            toReturn.add("1");
            toReturn.add("2");
        }//if tree
        
        return toReturn;
    }//onTabComplete
    
}//main class
