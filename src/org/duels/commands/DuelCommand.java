/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duels.commands;


import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.duels.DuelsPlugin;
import org.duels.Duel;

/**
 *
 * @author main
 */
public class DuelCommand implements CommandExecutor{

    final int DELAY = 60;
    DuelsPlugin ref;
    public DuelCommand(DuelsPlugin main){
        ref=main;
    }//constructor
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(cs instanceof Player){
            
        }else
            return true;
        if(!ref.doDuels){
            cs.sendMessage(ChatColor.RED+"Duels Disabled. Check back later.");
            return true;
        }
        
        if(ref.Banned.contains((Player)cs)){
            cs.sendMessage(ChatColor.RED+"You are currently banned from duels. If this is in error, please contact a moderator.");
            return true;
        }
        if(strings.length<1)
            return false;
        else{
            switch(strings[0].toLowerCase()){
                case "challenge":
                        if(strings.length<2)
                            ((Player)(cs)).sendMessage(ChatColor.RED+"Please specify a player.");
                        else{
                            Player challenged=null;
                            for(Player check: ref.getServer().getOnlinePlayers()){
                                if(check.getName().equals(strings[1]))
                                    challenged=check;
                            }//for loop
                            if(challenged==null){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"Please specify a player.");
                                return true;
                            }else if(challenged.equals(((Player)cs))){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"Cannot challenge self.");
                                return true;
                                
                            }else if(!challenged.hasPermission("duel.user")||ref.Ignored.contains(challenged)){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"Player cannot duel.");
                                return true;
                            }else if(ref.isInCombat(challenged)||inSystem(challenged)){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"Player cannot duel.");
                                return true;
                            }else if(ref.isInCombat(((Player)cs))){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"You are in combat!");
                                return true;
                            }else if(inSystem(((Player)cs))){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"You have a pending request or duel. Do "+ChatColor.YELLOW+"/duel cancel"+ChatColor.RED+" or "+ChatColor.YELLOW+"/duel reject"+ChatColor.RED+" to remove the pending duel.");
                                return true;
                            }else if(ref.Elo.get(challenged)-ref.Elo.get((Player)cs)>200||ref.Elo.get(challenged)-ref.Elo.get((Player)cs)<-200){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"Your Elo difference is too great for a ranked duel try "+ChatColor.YELLOW+"/duel practice"+ChatColor.RED+" instead.");
                                return true;
                            }
                            ref.Pending.put(new Duel(((Player)cs),challenged,true),DELAY);
                            challenged.sendMessage(ChatColor.YELLOW+"You have recieved a ranked duel request from "+((Player)cs).getDisplayName()+ChatColor.YELLOW+"! To accept, type "+ChatColor.RED+"/duel accept"+ChatColor.YELLOW+" within "+DELAY+" seconds! To reject, type"+ChatColor.RED+"/duel reject"+ChatColor.YELLOW+"!");
                                
                        }//else if statement
                            
                    break;
                case "practice":
                    if(strings.length<2)
                            ((Player)(cs)).sendMessage(ChatColor.RED+"Please specify a player.");
                        else{
                            Player challenged=null;
                            for(Player check: ref.getServer().getOnlinePlayers()){
                                if(check.getName().equals(strings[1]))
                                    challenged=check;
                            }//for loop
                            if(challenged==null){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"Please specify a player.");
                                return true;
                            }else if(challenged.equals(((Player)cs))){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"Cannot challenge self.");
                                return true;
                                
                            }else if(!challenged.hasPermission("duel.user")||ref.Ignored.contains(challenged)){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"Player cannot duel.");
                                return true;
                            }else if(ref.isInCombat(challenged)||inSystem(challenged)){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"Player cannot duel.");
                                return true;
                            }else if(ref.isInCombat(((Player)cs))){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"You are in combat!");
                                return true;
                            }else if(inSystem(((Player)cs))){
                                ((Player)(cs)).sendMessage(ChatColor.RED+"You have a pending request or duel. Do "+ChatColor.YELLOW+"/duel cancel"+ChatColor.RED+" to remove the pending duel.");
                                return true;
                            }
                            ref.Pending.put(new Duel(((Player)cs),challenged,false),DELAY);
                            challenged.sendMessage(ChatColor.YELLOW+"You have recieved an unranked duel request from "+((Player)cs).getDisplayName()+ChatColor.YELLOW+"! To accept, type "+ChatColor.RED+"/duel accept"+ChatColor.YELLOW+" within "+DELAY+" seconds!");
                                
                        }//else if statement
                    return true;
                    
                case "accept":
                    if(cs.equals(ref.Dueler1)){
                        ref.conf1=true;
                        cs.sendMessage(ChatColor.GREEN+"Duel Confirmed! Waiting on start...");
                        return true;
                    }
                    if(cs.equals(ref.Dueler2)){
                        ref.conf2=true;
                        cs.sendMessage(ChatColor.GREEN+"Duel Confirmed! Waiting on start...");
                        return true;
                    }
                        
                    Duel accepted=null;
                    for(Duel check: ref.Pending.keySet()){
                        if(check.recipient.equals(((Player)cs)))
                            accepted=check;
                    }
                    if(accepted==null){
                        ((Player)cs).sendMessage(ChatColor.RED+"You have no incoming duel requests.");
                        return true;
                    }else if(ref.isInCombat(accepted.challenger)||ref.isInCombat(accepted.recipient)){
                        accepted.challenger.sendMessage(ChatColor.RED+"Error in accepting Duel[PVP ONGOING]. Duel Cancelled.");
                        accepted.recipient.sendMessage(ChatColor.RED+"Error in accepting Duel[PVP ONGOING]. Duel Cancelled.");
                        ref.Pending.remove(accepted);
                        return true;
                    }
                    if(!ref.getServer().getOnlinePlayers().contains(accepted.challenger)){
                        accepted.recipient.sendMessage(ChatColor.RED+"Error in accepting Duel[CHALLENGER OFFLINE]. Duel Cancelled.");
                        ref.Pending.remove(accepted);
                        return true;
                    }
                    
                    ref.Pending.remove(accepted);
                    if(ref.currentDuel){
                    ref.DuelQueue.add(accepted);
                    accepted.challenger.sendMessage(ChatColor.AQUA+"Duel Accepted! You are "+ref.DuelQueue.indexOf(accepted)+"/"+ref.DuelQueue.size()+" in queue.");
                    accepted.recipient.sendMessage(ChatColor.AQUA+"Duel Accepted! You are "+ref.DuelQueue.indexOf(accepted)+"/"+ref.DuelQueue.size()+" in queue.");
                    }else{
                        accepted.challenger.sendMessage(ChatColor.AQUA+"Duel Accepted! Starting now...");
                        accepted.recipient.sendMessage(ChatColor.AQUA+"Duel Accepted! Starting now...");
                        ref.loadDuel(accepted, false);
                        ref.startDuel();
                    }
                 
                    return true;
                    
                case "rank":
                    if(!ref.Elo.keySet().contains((Player)cs))
                        ref.Elo.put(((Player)cs), 1200);
                    
                    int rank=1;
                    for(OfflinePlayer check:ref.Elo.keySet()){
                        if(ref.Elo.get(ref.getServer().getOfflinePlayer(check.getUniqueId())).intValue()>ref.Elo.get(ref.getServer().getOfflinePlayer(((Player)cs).getUniqueId())).intValue())
                            rank++;
                    }//for loop
                    String c="";
                    if(rank==1)
                        c=ChatColor.GOLD+"1st!";
                    if(rank==2)
                        c=ChatColor.GRAY+"2nd!";
                    if(rank==3)
                        c=ChatColor.DARK_RED+"3rd!";
                    if(rank>3)
                        c=ChatColor.YELLOW+""+rank;
                    ((Player)cs).sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"Duel Ranking:");
                    ((Player)cs).sendMessage(ChatColor.RED+"User: "+((Player)cs).getDisplayName());
                    ((Player)cs).sendMessage(ChatColor.RED+"Rank: "+c);
                    ((Player)cs).sendMessage(ChatColor.RED+"Elo:  "+ChatColor.AQUA+""+ref.Elo.get(((Player)cs)));
                        
                    return true;
                case "ignore":
                    if(ref.Ignored.contains((Player)cs)){
                        ref.Ignored.remove((Player)cs);
                        ((Player)cs).sendMessage(ChatColor.RED+"Duel requests unignored.");
                    }else{
                        ref.Ignored.add((Player)cs);
                        ((Player)cs).sendMessage(ChatColor.RED+"Duel requests ignored.");
                    }   
                    return true;
                case "top":
                    int[]rankings ={0,0,0};
                    OfflinePlayer[]playerrank={null,null,null};
                    if(!ref.Elo.containsKey((Player)cs)){
                        ref.Elo.put((Player)cs, 1200);
                    }
                    if(ref.Elo.size()<5){
                        ((Player)cs).sendMessage(ChatColor.RED+"Not enough players have dueled to form a ranking.");
                        return true;
                    }
                    for(OfflinePlayer comp:ref.Elo.keySet()){
                        if(ref.Elo.get(comp)>=rankings[0]){
                            rankings[2]=rankings[1];
                            rankings[1]=rankings[0];
                            rankings[0]=ref.Elo.get(comp);
                            playerrank[2]=playerrank[1];
                            playerrank[1]=playerrank[0];
                            playerrank[0]=comp;
                        }else if(ref.Elo.get(comp)>=rankings[1]){
                            rankings[2]=rankings[1];
                            rankings[1]=ref.Elo.get(comp);
                            playerrank[2]=playerrank[1];
                            playerrank[1]=comp;
                        }else if(ref.Elo.get(comp)>=rankings[2]){
                            rankings[2]=ref.Elo.get(comp);
                            playerrank[2]=comp;
                        }
                    }// for loop
                    ((Player)cs).sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"Top Duelists:");
                    ((Player)cs).sendMessage(ChatColor.RED+"1st: "+ChatColor.AQUA+""+rankings[0]+"- "+ChatColor.RED+""+playerrank[0].getName());
                    ((Player)cs).sendMessage(ChatColor.RED+"2nd: "+ChatColor.AQUA+""+rankings[1]+"- "+ChatColor.RED+""+playerrank[1].getName());
                    ((Player)cs).sendMessage(ChatColor.RED+"3rd: "+ChatColor.AQUA+""+rankings[2]+"- "+ChatColor.RED+""+playerrank[2].getName());  
                   
                    return true;
                default:
                    return false;
                case "cancel":
                    Duel cancelled=null;
                    for(Duel check:ref.Pending.keySet()){
                        if(check.challenger.equals((Player)cs)||check.recipient.equals((Player)cs))
                            cancelled=check;
                    }
                    if(cancelled!=null){
                        cancelled.challenger.sendMessage(ChatColor.RED+"Your duel request has been rejected.");
                        cancelled.recipient.sendMessage(ChatColor.RED+"Rejected duel request.");
                        ref.Pending.remove(cancelled);
                        return true;
                    }
                    for(int i=0;i<ref.DuelQueue.size();i++){
                        if(ref.DuelQueue.get(i).equals((Player)cs)||ref.DuelQueue.get(i).equals((Player)cs))
                            cancelled=ref.DuelQueue.get(i);
                    }
                    if(cancelled!=null){
                        cancelled.challenger.sendMessage(ChatColor.RED+"Your queued duel has been cancelled.");
                        cancelled.recipient.sendMessage(ChatColor.RED+"Your queued duel has been cancelled.");
                        ref.DuelQueue.remove(cancelled);
                        return true;
                    }
                    ((Player)cs).sendMessage(ChatColor.RED+"No Duels to cancel!");
                    return true;
                    
                case "ban":
                    if(!cs.hasPermission("duel.moderator")||strings.length<2)
                        return false;
                    Player change=ref.getServer().getPlayer(strings[1]);
                    if(change==null){
                        cs.sendMessage(ChatColor.RED+"Player "+ChatColor.AQUA+strings[1]+""+ChatColor.RED+" not found.");
                        return true;
                    }
                    if(ref.Banned.contains(change)){
                        cs.sendMessage(ChatColor.RED+"Player "+ChatColor.AQUA+strings[1]+""+ChatColor.RED+" already banned from duels.");
                        return true;
                    }
                    cs.sendMessage(ChatColor.RED+"Player "+ChatColor.AQUA+strings[1]+""+ChatColor.RED+" banned from duels.");   
                    ref.Banned.add(change);
                    return true;
                    
                case "unban":
                    if(!cs.hasPermission("duel.moderator")||strings.length<2)
                        return false;
                    Player changer=ref.getServer().getPlayer(strings[1]);
                    if(changer==null){
                        cs.sendMessage(ChatColor.RED+"Player "+ChatColor.AQUA+strings[1]+""+ChatColor.RED+" not found.");
                        return true;
                    }
                    if(ref.Banned.contains(changer)){
                        cs.sendMessage(ChatColor.RED+"Player "+ChatColor.AQUA+strings[1]+""+ChatColor.RED+" unbanned from duels.");
                        ref.Banned.remove(changer);
                        return true;
                    }
                    cs.sendMessage(ChatColor.RED+"Player "+ChatColor.AQUA+strings[1]+""+ChatColor.RED+" not banned from duels.");   
                    
                    return true;
                case "canduel":
                    if(!cs.hasPermission("duel.moderator")||strings.length<2)
                        return false;
                    Player changed=ref.getServer().getPlayer(strings[1]);
                    if(changed==null){
                        cs.sendMessage(ChatColor.RED+"Player "+ChatColor.AQUA+strings[1]+""+ChatColor.RED+" not found.");
                        return true;
                    }
                    if(ref.Banned.contains(changed)){
                        cs.sendMessage(ChatColor.RED+"Player "+ChatColor.AQUA+strings[1]+""+ChatColor.RED+" banned from duels.");
                        return true;
                    }
                    cs.sendMessage(ChatColor.RED+"Player "+ChatColor.AQUA+strings[1]+""+ChatColor.RED+" not banned from duels.");   
                    return true;
                case "help":
                    ((Player)cs).sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"Duel Command Help:");
                    ((Player)cs).sendMessage(ChatColor.YELLOW+"/duel accept"+ChatColor.RED+" - Accepts an incoming duel request.");
                    ((Player)cs).sendMessage(ChatColor.YELLOW+"/duel cancel"+ChatColor.RED+" - Rejects an incoming duel request or cancels a pending duel or duel request.");
                    ((Player)cs).sendMessage(ChatColor.YELLOW+"/duel challenge [playername]"+ChatColor.RED+" - Challenges a player to a duel.");
                    ((Player)cs).sendMessage(ChatColor.YELLOW+"/duel help"+ChatColor.RED+" - Displays duel system commands.");
                    ((Player)cs).sendMessage(ChatColor.YELLOW+"/duel ignore"+ChatColor.RED+" - Auto-rejects all incoming requests. Resets on server reset.");
                    ((Player)cs).sendMessage(ChatColor.YELLOW+"/duel practice"+ChatColor.RED+" - Challenges a player to a friendly match.");
                    ((Player)cs).sendMessage(ChatColor.YELLOW+"/duel rank"+ChatColor.RED+" - Displays your current elo and rank.");
                    ((Player)cs).sendMessage(ChatColor.YELLOW+"/duel top"+ChatColor.RED+" - Displays the top 3 Players by Elo score.");
                    
                    return true;
            }//switch
        }//first check
        return true;
        
    }
    public boolean inSystem(Player p){
        
        for(Duel check: ref.Pending.keySet()){
           if(check.recipient.equals(p)||check.challenger.equals(p))
                   return true;
         }
        for(int i=0;i<ref.DuelQueue.size();i++){
           if(ref.DuelQueue.get(1).recipient.equals(p)||ref.DuelQueue.get(1).challenger.equals(p))
                   return true;
        }
        if(ref.Dueler1.equals(p)||ref.Dueler2.equals(p))
            return true;
        
        return false;
    }//inSystem
}
