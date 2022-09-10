package cn.lanink.gunwar.room.conquest;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityLongFlag;
import cn.lanink.gunwar.room.base.BaseRespawnModeRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.tasks.game.conquest.AsyncFlagRadiusCheckTask;
import cn.lanink.gunwar.tasks.game.conquest.FlagSpawnCheckTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.DummyBossBar;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LT_Name
 */
public class ConquestModeRoom extends BaseRespawnModeRoom {

    @Getter
    private final Vector3 conquestPointA;
    @Getter
    private final Vector3 conquestPointB;
    @Getter
    private final Vector3 conquestPointC;

    public EntityLongFlag aFlag;
    public EntityLongFlag bFlag;
    public EntityLongFlag cFlag;

    private final ConcurrentHashMap<Player, DummyBossBar> bossBarMap = new ConcurrentHashMap<>();
    private int bossBarShowTime = 0;

    /**
     * 初始化
     *
     * @param level  游戏世界
     * @param config 配置文件
     */
    public ConquestModeRoom(@NotNull Level level, @NotNull Config config) throws RoomLoadException {
        super(level, config);

        String aPosString = config.getString("ConquestPointA");
        String bPosString = config.getString("ConquestPointB");
        String cPosString = config.getString("ConquestPointC");
        if ("".equals(aPosString.trim()) ||
                "".equals(bPosString.trim()) ||
                "".equals(cPosString.trim())) {
            throw new RoomLoadException("§c房间：" + level.getFolderName() + " 配置不完整，加载失败！");
        }
        this.conquestPointA = Tools.stringToVector3(aPosString);
        this.conquestPointB = Tools.stringToVector3(bPosString);
        this.conquestPointC = Tools.stringToVector3(cPosString);
    }

    @Override
    public List<String> getListeners() {
        List<String> list = super.getListeners();
        //TODO
        return list;
    }

    @Override
    protected void initData() {
        super.initData();

        this.redScore = this.getVictoryScore() / 2;
        this.blueScore = this.getVictoryScore() / 2;

        if (this.aFlag != null) {
            this.aFlag.close();
            this.aFlag = null;
        }
        if (this.bFlag != null) {
            this.bFlag.close();
            this.bFlag = null;
        }
        if (this.cFlag != null) {
            this.cFlag.close();
            this.cFlag = null;
        }

        if (this.bossBarMap != null && !this.bossBarMap.isEmpty()) {
            for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
                entry.getKey().removeBossBar(entry.getValue().getBossBarId());
            }
            this.bossBarMap.clear();
        }
    }


    @Override
    public void timeTask() {
        super.timeTask();

        if (!this.isRoundEnd()) {
            if (this.aFlag != null && this.bFlag != null && this.cFlag != null) { //初始化时可能为空
                String text = "§aA: " + (this.aFlag.getTeam() == Team.NULL ? "§fX" : this.aFlag.getTeam().getShowName()) +
                        "   §aB: " + (this.bFlag.getTeam() == Team.NULL ? "§fX" : this.bFlag.getTeam().getShowName()) +
                        "   §aC: " + (this.cFlag.getTeam() == Team.NULL ? "§fX" : this.cFlag.getTeam().getShowName());

                for (Player player : this.getPlayers().keySet()) {
                    Tools.createBossBar(player, this.bossBarMap);
                    DummyBossBar bossBar = this.bossBarMap.get(player);
                    bossBar.setText(text);
                    if (this.bossBarShowTime > 3) {
                        bossBar.setColor(BossBarColor.BLUE);
                        bossBar.setLength(Math.max(1, this.blueScore * 1.0f / this.getVictoryScore() * 100));
                    }else {
                        bossBar.setColor(BossBarColor.RED);
                        bossBar.setLength(Math.max(1, this.redScore * 1.0f / this.getVictoryScore() * 100));
                    }
                }
                this.bossBarShowTime++;
                if (this.bossBarShowTime > 6) {
                    this.bossBarShowTime = 0;
                }

                int redFlagCount = 0;
                int blueFlagCount = 0;
                if (this.aFlag.getKeepTime() > 5) {
                    if (this.aFlag.getTeam() == Team.RED) {
                        redFlagCount++;
                    }else if (this.aFlag.getTeam() == Team.BLUE) {
                        blueFlagCount++;
                    }
                    this.aFlag.setKeepTime(0);
                }
                if (this.bFlag.getKeepTime() > 5) {
                    if (this.bFlag.getTeam() == Team.RED) {
                        redFlagCount++;
                    }else if (this.bFlag.getTeam() == Team.BLUE) {
                        blueFlagCount++;
                    }
                    this.bFlag.setKeepTime(0);
                }
                if (this.cFlag.getKeepTime() > 5) {
                    if (this.cFlag.getTeam() == Team.RED) {
                        redFlagCount++;
                    }else if (this.cFlag.getTeam() == Team.BLUE) {
                        blueFlagCount++;
                    }
                    this.cFlag.setKeepTime(0);
                }
                if (redFlagCount > blueFlagCount) {
                    this.redScore++;
                    this.blueScore--;
                }else if (blueFlagCount > redFlagCount) {
                    this.redScore--;
                    this.blueScore++;
                }
            }

            if (this.blueScore >= this.victoryScore || this.redScore <= 0) {
                this.roundEnd(Team.BLUE);
            } else if (this.redScore >= this.victoryScore || this.blueScore <= 0) {
                this.roundEnd(Team.RED);
            }
        }
    }

    @Override
    public void startGame() {
        super.startGame();
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar,
                new FlagSpawnCheckTask(GunWar.getInstance(), this),
                20
        );
        Server.getInstance().getScheduler().scheduleAsyncTask(
                this.gunWar,
                new AsyncFlagRadiusCheckTask(this)
        );
    }

    @Override
    public void roundEnd(Team victory) {
        if (victory == Team.NULL) {
            if (this.redScore > this.blueScore) {
                this.redScore += this.getVictoryScore();
                victory = Team.RED;
            }else if (this.blueScore > this.redScore) {
                this.blueScore += this.getVictoryScore();
                victory = Team.BLUE;
            }
        }
        super.roundEnd(victory);
    }

    @Override
    public void playerDeath(Player player, Entity damager, String killMessage) {
        super.playerDeath(player, damager, killMessage);
        if (this.getPlayerTeam(player) == Team.RED) {
            this.redScore--;
        }else if (this.getPlayerTeam(player) == Team.BLUE) {
            this.blueScore--;
        }
    }

    public int getConquestPointRadius() {
        return 5;
    }

}
