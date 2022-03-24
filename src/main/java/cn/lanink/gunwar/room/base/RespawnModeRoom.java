package cn.lanink.gunwar.room.base;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Config;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LT_Name
 */
public abstract class RespawnModeRoom extends BaseRoom {

    /**
     * 玩家重生时间
     */
    protected final HashMap<Player, Integer> playerRespawnTime = new HashMap<>();
    protected int respawnTime = 20;

    /**
     * 初始化
     *
     * @param level  游戏世界
     * @param config 配置文件
     */
    public RespawnModeRoom(@NotNull Level level, @NotNull Config config) throws RoomLoadException {
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
        if (this.playerRespawnTime.containsKey(player)) {
            return this.playerRespawnTime.get(player);
        }
        return 0;
    }

    @Override
    public void playerDeath(Player player, Entity damager, String killMessage) {
        super.playerDeath(player, damager, killMessage);
        this.getPlayerRespawnTime().put(player, this.respawnTime);
    }
}
