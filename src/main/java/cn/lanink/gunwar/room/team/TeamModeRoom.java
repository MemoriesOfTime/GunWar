package cn.lanink.gunwar.room.team;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.room.base.BaseRespawnModeRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

/**
 * @author LT_Name
 */
public class TeamModeRoom extends BaseRespawnModeRoom {

    /**
     * 初始化
     *
     * @param level  游戏世界
     * @param config 配置文件
     */
    public TeamModeRoom(Level level, Config config) throws RoomLoadException {
        super(level, config);
        this.respawnNeedTime = 3; //团队模式缩短复活时间
    }

    @Override
    public void timeTask() {
        super.timeTask();
        if (!this.isRoundEnd()) {
            if (this.blueScore >= this.victoryScore) {
                this.roundEnd(Team.BLUE);
            } else if (this.redScore >= this.victoryScore) {
                this.roundEnd(Team.RED);
            }
        }
    }

    @Override
    public void playerDeath(Player player, Entity damager, String killMessage) {
        if (damager instanceof Player) {
            if (this.getPlayerTeam(player) == Team.RED) {
                this.blueScore++;
            }else {
                this.redScore++;
            }
        }
        super.playerDeath(player, damager, killMessage);
    }
}
