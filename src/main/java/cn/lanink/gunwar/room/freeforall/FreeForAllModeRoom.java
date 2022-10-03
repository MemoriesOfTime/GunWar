package cn.lanink.gunwar.room.freeforall;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRespawnModeRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.tasks.VictoryTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * F.F.A 个人战 模式房间
 *
 * @author LT_Name
 */
public class FreeForAllModeRoom extends BaseRespawnModeRoom {

    private final ArrayList<Vector3> randomSpawns = new ArrayList<>();

    /**
     * 初始化
     *
     * @param level  游戏世界
     * @param config 配置文件
     */
    public FreeForAllModeRoom(@NotNull Level level, @NotNull Config config) throws RoomLoadException {
        super(level, config);

        //针对未配置的情况，缩短默认的时间
        this.respawnNeedTime = config.getInt("respawn-need-time", 3);

        for (String string : config.getStringList("randomSpawns")) {
            this.randomSpawns.add(Tools.stringToVector3(string));
        }
        if (this.randomSpawns.isEmpty()) {
            throw new RoomLoadException("randomSpawns is empty");
        }
    }

    @Override
    public List<String> getListeners() {
        List<String> list = super.getListeners();
        list.remove("DefaultDamageListener");
        list.add("FFADamageListener");
        return list;
    }

    @Override
    protected void checkTeamPlayerCount() {
        if (this.getPlayers().isEmpty()) {
            this.roundEnd(Team.NULL);
        }
        int killAtMost = this.getKillAtMost();
        this.redScore = killAtMost;
        this.blueScore = this.getVictoryScore();
        if (killAtMost >= this.getVictoryScore()) {
            this.roundEnd(Team.NULL);
        }
    }

    public int getKillAtMost() {
        ArrayList<Map.Entry<Player, Integer>> list = new ArrayList<>(this.playerKillMap.entrySet());
        list.sort((o1, o2) -> o2.getValue() - o1.getValue());
        if (!list.isEmpty()) {
            return list.get(0).getValue();
        }
        return 0;
    }

    @Override
    public void assignTeam() {
        //无需分配队伍
    }

    @Override
    public void roundEnd(Team victory) {
        ArrayList<Map.Entry<Player, Integer>> list = new ArrayList<>(this.playerKillMap.entrySet());
        list.sort((o1, o2) -> o2.getValue() - o1.getValue());
        if (!list.isEmpty()) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar,
                    new VictoryTask(this.gunWar, this, list.get(0).getKey()),
                    20
            );
        }
    }

    @Override
    public void playerRespawn(Player player) {
        super.playerRespawn(player);

        if (this.isRoundEndCleanItem()) {
            player.getInventory().clearAll();
            player.getUIInventory().clearAll();
        }else {
            //清除一些必须清除的特殊物品
            PlayerInventory inventory = player.getInventory();
            Tools.removeGunWarItem(inventory, Tools.getItem(10));
            Tools.removeGunWarItem(inventory, Tools.getItem(11));
            Tools.removeGunWarItem(inventory, Tools.getItem(12));
            Tools.removeGunWarItem(inventory, Tools.getItem(13));
            Tools.removeGunWarItem(inventory, Tools.getItem(201));
        }

        Tools.giveItem(this, player, Team.NULL);

        player.teleport(this.randomSpawns.get(GunWar.RANDOM.nextInt(this.randomSpawns.size())));
    }
}
