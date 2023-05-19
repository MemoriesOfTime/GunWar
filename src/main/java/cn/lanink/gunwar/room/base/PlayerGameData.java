package cn.lanink.gunwar.room.base;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.Player;
import lombok.Data;

import java.util.HashMap;

/**
 * @author LT_Name
 */
@Data
public class PlayerGameData {

    private final Player player;
    private Team team;
    private float health;
    private int invincibleTime;
    private int integral;
    private int killCount;
    private int assistsKillCount;

    private Player lastDamagePlayer;
    private final HashMap<Player, Float> damager = new HashMap<>();

    public PlayerGameData(Player player) {
        this.player = player;
        this.team = Team.NULL;
        if (GunWar.getInstance().isEnableAloneHealth()) {
            this.health = 20F;
        } else {
            this.health = player.getMaxHealth();
        }
        this.integral = Integer.MAX_VALUE;
        this.killCount = 0;
        this.assistsKillCount = 0;
    }

    public void setHealth(float health) {
        this.health = health;
        if (!GunWar.getInstance().isEnableAloneHealth()) {
            this.player.setHealth(health);
        }
    }

    public void addKillCount() {
        this.killCount++;
    }

    public void addAssistsKillCount() {
        this.assistsKillCount++;
    }
}
