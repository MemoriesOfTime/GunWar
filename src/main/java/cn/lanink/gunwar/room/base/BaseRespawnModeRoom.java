package cn.lanink.gunwar.room.base;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Config;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 单回合可重生房间类型
 *
 * @author LT_Name
 */
public abstract class BaseRespawnModeRoom extends BaseRoom {

    //玩家复活所需时间
    @Getter
    protected int respawnNeedTime = 20;

    /**
     * 玩家重生时间
     */
    protected final HashMap<Player, Integer> playerRespawnTime = new HashMap<>();

    /**
     * 初始化
     *
     * @param level  游戏世界
     * @param config 配置文件
     */
    public BaseRespawnModeRoom(@NotNull Level level, @NotNull Config config) throws RoomLoadException {
        super(level, config);
    }

    @Override
    protected void initData() {
        super.initData();

        if (this.playerRespawnTime != null) {
            this.playerRespawnTime.clear();
        }
    }

    @Override
    public void timeTask() {
        super.timeTask();

        //玩家复活计算
        for (Map.Entry<Player, Integer> entry : this.getPlayerRespawnTime().entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
                if (entry.getValue() == 0) {
                    this.playerRespawn(entry.getKey());
                }else if (entry.getValue() <= 5) {
                    Tools.playSound(entry.getKey(), Sound.RANDOM_CLICK);
                }
            }
        }
    }

    /**
     * 获取玩家重生剩余时间
     * @return 玩家重生剩余时间Map
     */
    public HashMap<Player, Integer> getPlayerRespawnTime() {
        return this.playerRespawnTime;
    }

    /**
     * 获取玩家重生剩余时间
     *
     * @param player 玩家
     * @return 重生剩余时间
     */
    public int getPlayerRespawnTime(@NotNull Player player) {
        return this.playerRespawnTime.getOrDefault(player, 0);
    }

    @Override
    public void playerDeath(Player player, Entity damager, String killMessage) {
        super.playerDeath(player, damager, killMessage);
        this.getPlayerRespawnTime().put(player, this.respawnNeedTime);
    }
}
