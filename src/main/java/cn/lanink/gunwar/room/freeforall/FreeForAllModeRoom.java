package cn.lanink.gunwar.room.freeforall;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRespawnModeRoom;
import cn.lanink.gunwar.room.base.PlayerGameData;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.tasks.VictoryTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
    public void saveConfig() {
        super.saveConfig();

        //移除FFA模式不需要的配置
        config.remove("redTeamInitialItems");
        config.remove("blueTeamInitialItems");
        config.remove("roundEndCleanItem");

        ArrayList<String> randomSpawnList = new ArrayList<>();
        for (Vector3 vector3 : this.randomSpawns) {
            randomSpawnList.add(Tools.vector3ToString(vector3));
        }
        config.set("randomSpawns", randomSpawnList);

        config.save();
    }

    @Override
    public boolean canDamageTeammates() {
        return true;
    }

    @Override
    protected void checkTeamPlayerCount() {
        if (this.getPlayerDataMap().size() < 2) {
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
        ArrayList<PlayerGameData> list = new ArrayList<>(this.getPlayerDataMap().values());
        list.sort((o1, o2) -> o2.getKillCount() - o1.getKillCount());
        if (!list.isEmpty()) {
            return list.get(0).getKillCount();
        }
        return 0;
    }

    @Override
    public void assignTeam() {
        for (Map.Entry<Player, PlayerGameData> entry : this.getPlayerDataMap().entrySet()) {
            entry.getValue().setTeam(Team.RED);
        }
    }

    @Override
    public void roundEnd(Team victory) {
        ArrayList<PlayerGameData> list = new ArrayList<>(this.getPlayerDataMap().values());
        list.sort((o1, o2) -> o2.getKillCount() - o1.getKillCount());
        this.setStatus(ROOM_STATUS_VICTORY);
        if (!list.isEmpty()) {
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar,
                    new VictoryTask(this.gunWar, this, list.get(0).getPlayer()),
                    20
            );
        } else {
            this.endGame();
        }
    }

    @Override
    public void playerRespawn(Player player) {
        super.playerRespawn(player);

        player.getInventory().clearAll();
        player.getUIInventory().clearAll();

        Tools.giveItem(this, player, Team.NULL, !this.isRoundEndCleanItem());

        //player.teleport(this.randomSpawns.get(GunWar.RANDOM.nextInt(this.randomSpawns.size())));
    }

    @Override
    public Position getRedSpawn() {
        return Position.fromObject(this.randomSpawns.get(GunWar.RANDOM.nextInt(this.randomSpawns.size())), this.getLevel());
    }

    @Override
    public Position getBlueSpawn() {
        return Position.fromObject(this.randomSpawns.get(GunWar.RANDOM.nextInt(this.randomSpawns.size())), this.getLevel());
    }

    @Override
    public ArrayList<String> getRedTeamInitialItems() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<String> getBlueTeamInitialItems() {
        return new ArrayList<>();
    }
}
