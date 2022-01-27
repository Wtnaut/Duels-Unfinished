/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duels;

import org.bukkit.entity.Player;

/**
 *
 * @author main
 */
public class Duel {
    public Player challenger,recipient;
    public boolean doElo;
    public Duel(Player chal, Player rec, boolean elo){
        this.challenger=chal;
        this.recipient=rec;
        this.doElo=elo;
    }
}
