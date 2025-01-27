package cn.lanink.gunwar.room.conquest;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.flag.EntityLongFlag;
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

    public EntityLongFlag flagA;
    public EntityLongFlag flagB;
    public EntityLongFlag flagC;

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
        if (aPosString.trim().isEmpty() ||
                bPosString.trim().isEmpty() ||
                cPosString.trim().isEmpty()) {
            throw new RoomLoadException("§c房间：" + level.getFolderName() + " 配置不完整，加载失败！");
        }
        this.conquestPointA = Tools.stringToVector3(aPosString);
        this.conquestPointB = Tools.stringToVector3(bPosString);
        this.conquestPointC = Tools.stringToVector3(cPosString);
    }

    @Override
    public void saveConfig() {
        super.saveConfig();

        this.config.set("ConquestPointA", Tools.vector3ToString(this.conquestPointA));
        this.config.set("ConquestPointB", Tools.vector3ToString(this.conquestPointB));
        this.config.set("ConquestPointC", Tools.vector3ToString(this.conquestPointC));

        this.config.save();
    }

    @Override
    protected void initData() {
        super.initData();

        this.redScore = this.getVictoryScore() / 2;
        this.blueScore = this.getVictoryScore() / 2;

        if (this.flagA != null) {
            this.flagA.close();
            this.flagA = null;
        }
        if (this.flagB != null) {
            this.flagB.close();
            this.flagB = null;
        }
        if (this.flagC != null) {
            this.flagC.close();
            this.flagC = null;
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
            if (this.flagA != null && this.flagB != null && this.flagC != null) { //初始化时可能为空
                String text = "§aA: " + (this.flagA.getTeam() == Team.NULL ? "§fX" : this.flagA.getTeam().getShowName()) +
                        "   §aB: " + (this.flagB.getTeam() == Team.NULL ? "§fX" : this.flagB.getTeam().getShowName()) +
                        "   §aC: " + (this.flagC.getTeam() == Team.NULL ? "§fX" : this.flagC.getTeam().getShowName());

                for (Player player : this.getPlayerDataMap().keySet()) {
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

                if (this.gameTime%5 == 0) {
                    int redFlagCount = 0;
                    int blueFlagCount = 0;
                    if (this.flagA.getKeepTime() > 5) {
                        if (this.flagA.getTeam() == Team.RED) {
                            redFlagCount++;
                        } else if (this.flagA.getTeam() == Team.BLUE) {
                            blueFlagCount++;
                        }
                    }
                    if (this.flagB.getKeepTime() > 5) {
                        if (this.flagB.getTeam() == Team.RED) {
                            redFlagCount++;
                        } else if (this.flagB.getTeam() == Team.BLUE) {
                            blueFlagCount++;
                        }
                    }
                    if (this.flagC.getKeepTime() > 5) {
                        if (this.flagC.getTeam() == Team.RED) {
                            redFlagCount++;
                        } else if (this.flagC.getTeam() == Team.BLUE) {
                            blueFlagCount++;
                        }
                    }
                    if (redFlagCount > blueFlagCount) {
                        int difference = redFlagCount - blueFlagCount;
                        this.redScore += difference;
                        this.blueScore -= difference;
                    } else if (blueFlagCount > redFlagCount) {
                        int difference = blueFlagCount - redFlagCount;
                        this.redScore -= difference;
                        this.blueScore += difference;
                    }
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
