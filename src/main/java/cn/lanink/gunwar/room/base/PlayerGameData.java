package cn.lanink.gunwar.room.base;

import cn.nukkit.Player;
import lombok.Data;

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

    public PlayerGameData(Player player) {
        this.player = player;
        this.team = Team.NULL;
        this.health = 20F;
        this.integral = Integer.MAX_VALUE;
    }
}
