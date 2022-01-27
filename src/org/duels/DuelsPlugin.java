package org.duels;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.SirBlobman.combatlogx.api.ICombatLogX;
import com.SirBlobman.combatlogx.api.event.PlayerUntagEvent;
import com.SirBlobman.combatlogx.api.utility.ICombatManager;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.duels.commands.DuelCommand;
import org.duels.commands.DuelToggleCommand;
import org.duels.commands.DuelsTabCompleter;
import org.duels.commands.EloChangeCommand;
import org.duels.commands.SetDuelSpawnCommand;
import org.duels.listeners.DuelerProtectListener;

/**
 *
 * @author Wtnaut
 */
public class DuelsPlugin extends JavaPlugin{
    public Location Duelspawn1, Duelspawn2;
    public Player Dueler1, Dueler2;
    public Inventory Dueler1Inv, Dueler2Inv;
    public HashMap<Duel, Integer> Pending = new HashMap<>();
    public HashMap<OfflinePlayer, Integer> Elo = new HashMap<>();
    public ArrayList<Duel> DuelQueue = new ArrayList<>();
    public ArrayList<Player> Ignored = new ArrayList<>();
    public ArrayList<OfflinePlayer> Banned= new ArrayList<>();
    public boolean currentDuel=false, doElo=true, doDuels=false, d1PVP=false,d2PVP=false,conf1=false,conf2=false;
    public int winner=0, timer=0;
            
    @Override
    public void onEnable(){
        //load old data
        load();
        //set up commands
        this.getCommand("dueltoggle").setExecutor(new DuelToggleCommand(this));
        this.getCommand("duel").setExecutor(new DuelCommand(this));
        this.getCommand("elochange").setExecutor(new EloChangeCommand(this));
        this.getCommand("setduelspawn").setExecutor(new SetDuelSpawnCommand(this));
        
        //set up tab completion
        DuelsTabCompleter completor=new DuelsTabCompleter(this);
        this.getCommand("dueltoggle").setTabCompleter(completor);
        this.getCommand("duel").setTabCompleter(completor);
        this.getCommand("elochange").setTabCompleter(completor);
        this.getCommand("setduelspawn").setTabCompleter(completor);
        
        this.getServer().getPluginManager().registerEvents(new DuelerProtectListener(this), this);
        
        
        this.Dueler1Inv=Bukkit.createInventory(null, InventoryType.PLAYER);
        this.Dueler2Inv=Bukkit.createInventory(null, InventoryType.PLAYER);
                
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                dueltick();
            }
        }, 0, 5);
        
        
    }//onEnable
    
    @Override
    public void onDisable(){
        if(this.Dueler1!=null){
        this.doElo=false;
        this.Dueler1.sendMessage("[Duels Plugin Disabled] Converting to practice match & ending");
        this.Dueler2.sendMessage("[Duels Plugin Disabled] Converting to practice match & ending");
        finishDuel(this.Dueler1);
        }
        save();
        
    }
    
    //----------------------dueling stuff-------------------------
    
    
    public void startDuel(){
        //set elo if none
        if(!this.Elo.containsKey(Dueler1))
            this.Elo.put(Dueler1,1200);
        if(!this.Elo.containsKey(Dueler2))
            this.Elo.put(Dueler2,1200);
            
        
        //TODO starting the duel logic & message players
        this.Dueler1.sendMessage(ChatColor.GREEN+"Your duel is starting!");
        this.Dueler2.sendMessage(ChatColor.GREEN+"Your duel is starting!");
        this.currentDuel=true;
        
        //TODO disable pvp protections
        YamlConfiguration D1config = ((ICombatLogX)this.getServer().getPluginManager().getPlugin("ICombatLogX")).getDataFile(this.Dueler1);
        YamlConfiguration D2config = ((ICombatLogX)this.getServer().getPluginManager().getPlugin("ICombatLogX")).getDataFile(this.Dueler2);
        this.d1PVP=D1config.getBoolean("newbie-helper.protected", false);
        this.d2PVP=D2config.getBoolean("newbie-helper.protected", false);
        D1config.set("newbie-helper.protected", false);
        D2config.set("newbie-helper.protected", false);
        //TODO save inventories
        this.Dueler1Inv.setContents(this.Dueler1.getInventory().getContents().clone());
        this.Dueler2Inv.setContents(this.Dueler2.getInventory().getContents().clone());
        
        //TODO teleport players
        this.Dueler1.teleport(Duelspawn1);
        this.Dueler1.teleport(Duelspawn2);
        
        //TODO set inventories
        Inventory inv = Bukkit.createInventory(null, InventoryType.PLAYER);
        inv.setItem(-106,new ItemStack(Material.SHIELD,1));
        inv.setItem(0,new ItemStack(Material.DIAMOND_SWORD,1));
        inv.setItem(1,new ItemStack(Material.IRON_AXE,1));
        inv.setItem(2,new ItemStack(Material.BOW,1));
        inv.setItem(3,this.getSpecial());
        inv.setItem(4,new ItemStack(Material.ENDER_PEARL,2));
        inv.setItem(6,new ItemStack(Material.ARROW,16));
        inv.setItem(7,new ItemStack(Material.GOLDEN_APPLE,4));
        inv.setItem(8,new ItemStack(Material.COOKED_BEEF,64));
        inv.setItem(100,new ItemStack(Material.IRON_BOOTS,1));
        inv.setItem(101,new ItemStack(Material.IRON_LEGGINGS,1));
        inv.setItem(102,new ItemStack(Material.IRON_CHESTPLATE,1));
        inv.setItem(103,new ItemStack(Material.IRON_HELMET,1));
        
        //transfer to players
        this.Dueler1.getInventory().setContents(inv.getContents().clone());
        this.Dueler2.getInventory().setContents(inv.getContents().clone());
        this.Dueler1.updateInventory();
        this.Dueler2.updateInventory();
        
        //TODO set health
        this.Dueler1.setHealth(this.Dueler1.getMaxHealth());
        this.Dueler2.setHealth(this.Dueler2.getMaxHealth());
        this.clearEffects(this.Dueler1);
        this.clearEffects(this.Dueler2);
    }
    
    public ItemStack getSpecial(){
        switch((int)(Math.random()*12)){
            default:
                return new ItemStack(Material.COBWEB,8);
            case 1:
                return new ItemStack(Material.FISHING_ROD);
            case 2:
                return new ItemStack(Material.WATER_BUCKET);
            case 3:
                ItemStack stack = new ItemStack(Material.SPLASH_POTION);
                PotionMeta meta = (PotionMeta)stack.getItemMeta();

                meta.setBasePotionData(new PotionData(PotionType.STRENGTH, true,true));
                stack.setItemMeta(meta);
                
                return stack;
            case 4:
                ItemStack stack2 = new ItemStack(Material.SPLASH_POTION);
                PotionMeta meta2 = (PotionMeta)stack2.getItemMeta();

                meta2.setBasePotionData(new PotionData(PotionType.SPEED, true,true));
                stack2.setItemMeta(meta2);
                
                return stack2;
            case 5:
                ItemStack stack3 = new ItemStack(Material.SPLASH_POTION);
                PotionMeta meta3 = (PotionMeta)stack3.getItemMeta();

                meta3.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, true,true));
                stack3.setItemMeta(meta3);
                
                return stack3;
            case 6:
                ItemStack stack4 = new ItemStack(Material.SPLASH_POTION);
                PotionMeta meta4 = (PotionMeta)stack4.getItemMeta();

                meta4.setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE, true,true));
                stack4.setItemMeta(meta4);
                
                return stack4;
            case 7:
                return new ItemStack(Material.TOTEM_OF_UNDYING);
            case 8:
                return new ItemStack(Material.TRIDENT);
            case 9:
                return new ItemStack(Material.CROSSBOW);
            case 10:
                ItemStack stack5 = new ItemStack(Material.ARROW,4);
                PotionMeta meta5 = (PotionMeta)stack5.getItemMeta();

                meta5.setBasePotionData(new PotionData(PotionType.SLOWNESS, true,true));
                stack5.setItemMeta(meta5);
                
                return stack5;
            case 11:
                return new ItemStack(Material.LAVA_BUCKET);     
        }
    }
    
    public void clearEffects (Player player){
        for (PotionEffect effect :player.getActivePotionEffects()){
            player.removePotionEffect(effect.getType());
        }
    }
    
    public void finishDuel(Player winner){
        if(winner.equals(this.Dueler1)||winner.equals(this.Dueler2)){
            Player loser;
            if(winner.equals(this.Dueler1))
                loser=this.Dueler2;
            else
                loser=this.Dueler1;
            
            if(this.doElo){
                int diff = this.Elo.get(winner)-this.Elo.get(loser);
                diff= (diff/20)+20;

                this.Elo.put(winner,this.Elo.get(winner)+diff);
                this.Elo.put(loser,this.Elo.get(winner)-diff);
                winner.sendMessage(ChatColor.GREEN+"You Won! "+ChatColor.AQUA+"+"+diff+" ELO (TOTAL:"+this.Elo.get(winner)+")");
                loser.sendMessage(ChatColor.RED+"You Lost... "+ChatColor.AQUA+"-"+diff+" ELO (TOTAL:"+this.Elo.get(winner)+")");
            }else{
                winner.sendMessage(ChatColor.GREEN+"You Won! [No ELO in practice]");
                loser.sendMessage(ChatColor.RED+"You Lost... [No ELO in practice]");
            }
            
            //TODO revert inventories
            this.Dueler1.getInventory().setContents(this.Dueler1Inv.getContents().clone());
            this.Dueler2.getInventory().setContents(this.Dueler2Inv.getContents().clone());
            this.Dueler1.updateInventory();
            this.Dueler2.updateInventory();
            //TODO teleport both to spawn
            this.Dueler1.teleport(this.getServer().getWorld("world").getSpawnLocation());
            this.Dueler2.teleport(this.getServer().getWorld("world").getSpawnLocation());
            //PVP protections
            this.Dueler1.setHealth(this.Dueler1.getMaxHealth());
            this.Dueler2.setHealth(this.Dueler2.getMaxHealth());
            this.clearEffects(this.Dueler1);
            this.clearEffects(this.Dueler2);
            
            ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
            ICombatManager combatManager = plugin.getCombatManager();
            combatManager.untag(Dueler1, PlayerUntagEvent.UntagReason.EXPIRE);
            combatManager.untag(Dueler2, PlayerUntagEvent.UntagReason.EXPIRE);
        
            ((ICombatLogX)this.getServer().getPluginManager().getPlugin("ICombatLogX")).getDataFile(this.Dueler1).set("newbie-helper.protected", this.d1PVP);
            ((ICombatLogX)this.getServer().getPluginManager().getPlugin("ICombatLogX")).getDataFile(this.Dueler2).set("newbie-helper.protected", this.d2PVP);
            
            this.Dueler1=null;
            this.Dueler2=null;
            this.conf1=false;
            this.conf2=false;
            this.currentDuel=false;
            this.timer=-1;
            this.currentDuel=false;
            this.Dueler1=null;
            this.Dueler2=null;
        
            save();
        }//safety check. does nothing otherwise
            
    }//finish duel
    
    public void loadDuel(Duel toLoad, boolean sendMessage){
        this.Dueler1=toLoad.challenger;
        this.Dueler2=toLoad.recipient;
        this.doElo=toLoad.doElo;
        
        if(sendMessage){
        this.Dueler1.sendMessage(ChatColor.RED+"Your Duel with "+Dueler2.getName()+" is next in queue! "+ChatColor.GREEN+"Please type "+ChatColor.YELLOW+"/duel accept"+ChatColor.GREEN+" to start your duel.");
        this.Dueler2.sendMessage(ChatColor.RED+"Your Duel with "+Dueler1.getName()+" is next in queue! "+ChatColor.GREEN+"Please type "+ChatColor.YELLOW+"/duel accept"+ChatColor.GREEN+" to start your duel.");
        this.timer=60*4;
        }
        
        
    }
    
    public boolean dueltick(){
        if(this.Dueler1==null||this.Dueler2==null){
            if(this.DuelQueue.size()>=1){
                this.loadDuel(this.DuelQueue.get(1),true);
                this.DuelQueue.remove(1);
            }
        }else if(this.currentDuel)
            return true;
        else if(this.conf1&&this.conf2)
            startDuel();
        else if(this.timer>0){
            this.timer-=1;
            if(this.timer==30*4){
                this.Dueler1.sendMessage(ChatColor.RED+"You have 30 seconds to start your duel.");
                this.Dueler2.sendMessage(ChatColor.RED+"You have 30 seconds to start your duel.");
            }
            if(this.timer==15*4){
                this.Dueler1.sendMessage(ChatColor.RED+"You have 15 seconds to start your duel.");
                this.Dueler2.sendMessage(ChatColor.RED+"You have 15 seconds to start your duel.");
            }
                
        }else if (this.timer==0){
            this.Dueler1.sendMessage(ChatColor.RED+"Your duel start time has expired.");
            this.Dueler2.sendMessage(ChatColor.RED+"Your duel start time has expired.");
            this.timer=-1;
            if(this.DuelQueue.size()>=1){
                this.loadDuel(this.DuelQueue.get(1),true);
                this.DuelQueue.remove(1);
            }
                
        }
            
        
        return true;
    }
    
    //----------------------load and save-------------------------
    
    public void save(){
         try{
            File saveloc = new File(this.getDataFolder() + "/Duels/otherdata.txt");
            if (!saveloc.exists()) {
                saveloc.createNewFile();
            }//if for creating file
            PrintWriter saveother = new PrintWriter(saveloc);
            saveother.println(this.Duelspawn1.getWorld().getUID().toString());
            saveother.print(this.Duelspawn1.getX()+"\n"+this.Duelspawn1.getY()+"\n"+this.Duelspawn1.getZ()+"\n"+this.Duelspawn2.getX()+"\n"+this.Duelspawn2.getY()+"\n"+this.Duelspawn2.getZ());
            saveother.close();
            
            File saveloc2 = new File(this.getDataFolder() + "/Duels/banned.txt");
            if (!saveloc2.exists()) {
                saveloc2.createNewFile();
            }//if for creating file
            saveother = new PrintWriter(saveloc2);
            for (OfflinePlayer uuid : this.Banned) {
                saveother.println(uuid.getUniqueId().toString());
            }
            saveother.print("stop");
            saveother.close();
            
            File saveloc3 = new File(this.getDataFolder() + "/Duels/elo.txt");
            if (!saveloc3.exists()) {
                saveloc3.createNewFile();
            }//if for creating file
            saveother = new PrintWriter(saveloc3);
            for (OfflinePlayer uuid : this.Elo.keySet()) {
                saveother.println(uuid.getUniqueId().toString());
                saveother.println(this.Elo.get(uuid));
            }
            saveother.print("stop");
            saveother.close();
            
        }catch(Exception e){
            this.getServer().getConsoleSender().sendMessage("[DUELS] Failed to save data");
        }//try-catch
         
         
    }//save
    
    public void load(){
        try{
            Scanner sc= new Scanner (new File(this.getDataFolder() + "/Duels/otherdata.txt"));
            World x = this.getServer().getWorld(UUID.fromString(sc.nextLine()));
            this.Duelspawn1= new Location(x, sc.nextDouble(),sc.nextDouble(),sc.nextDouble());
            this.Duelspawn2= new Location(x, sc.nextDouble(),sc.nextDouble(),sc.nextDouble());
            sc.close();
            
            sc= new Scanner( new File(this.getDataFolder() + "/Duels/elo.txt"));
            while(sc.hasNext()){
                String s = sc.nextLine();
                if(!s.equals("stop"))
                    this.Elo.put(this.getServer().getPlayer(UUID.fromString(s)), Integer.parseInt(sc.nextLine()));
            }
            sc.close();
            
            sc= new Scanner( new File(this.getDataFolder() + "/Duels/banned.txt"));
            while(sc.hasNext()){
                String s = sc.nextLine();
                if(!s.equals("stop"))
                    this.Banned.add(this.getServer().getPlayer(UUID.fromString(s)));
            }
            sc.close();
            
        }catch(Exception e){
            this.getServer().getConsoleSender().sendMessage("[DUELS] Failed to load saved data");
            this.Duelspawn1=this.getServer().getWorld("world").getSpawnLocation();
            this.Duelspawn2=this.getServer().getWorld("world").getSpawnLocation();
            
        }//try-catch
    }//load
    
    public boolean isInCombat(Player player) {
    // Make sure to check that CombatLogX is enabled before using it for anything.
    ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
    ICombatManager combatManager = plugin.getCombatManager();
    return combatManager.isInCombat(player);
    }//isincombat
    
}//duelsplugin
