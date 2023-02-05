package cn.lanink.gunwar.room.capturetheflag;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.entity.flag.EntityFlag;
import cn.lanink.gunwar.entity.flag.EntityFlagStand;
import cn.lanink.gunwar.event.GunWarRoomRoundEndEvent;
import cn.lanink.gunwar.room.base.BaseRespawnModeRoom;
import cn.lanink.gunwar.room.base.IntegralConfig;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.tasks.VictoryTask;
import cn.lanink.gunwar.tasks.game.ctf.FlagPickupCheckTask;
import cn.lanink.gunwar.tasks.game.ctf.FlagTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author lt_name
 */
public class CTFModeRoom extends BaseRespawnModeRoom {

    public Player haveRedFlag;
    public Player haveBlueFlag;
    public EntityFlagStand redFlagStand;
    public EntityFlagStand blueFlagStand;
    public EntityFlag redFlag;
    public EntityFlag blueFlag;
    private boolean overtime = false;

    public CTFModeRoom(Level level, Config config) throws RoomLoadException {
        super(level, config);
    }

    @Override
    public List<String> getListeners() {
        List<String> list = super.getListeners();
        list.add("CTFDamageListener");
        return list;
    }

    @Override
    public void timeTask() {
        super.timeTask();

        //旗帜自动拾取/传递
        if (this.haveRedFlag != null) {
            this.checkSlownessEffect(this.haveRedFlag);
            if (this.blueFlagStand.distance(this.haveRedFlag) <= 1.5) {
                this.redFlagDelivery(this.haveRedFlag);
            }
        }else if (this.redFlag != null && !this.redFlag.isClosed()) {
            for (Player player : this.getPlayersAccurate(Team.BLUE)) {
                if (player.distance(this.redFlag) <= 1.5) {
                    this.redFlagPickup(player);
                    break;
                }
            }
        }
        if (this.haveBlueFlag != null) {
            this.checkSlownessEffect(this.haveBlueFlag);
            if (this.redFlagStand.distance(this.haveBlueFlag) <= 1.5) {
                this.blueFlagDelivery(this.haveBlueFlag);
            }
        }else if (this.blueFlag != null && !this.blueFlag.isClosed()) {
            for (Player player : this.getPlayersAccurate(Team.RED)) {
                if (player.distance(this.blueFlag) <= 1.5) {
                    this.blueFlagPickup(player);
                    break;
                }
            }
        }

        this.victoryJudgment();
    }

    private void checkSlownessEffect(@NotNull Player player) {
        Effect effect = player.getEffect(Effect.SLOWNESS);
        if (effect == null || effect.getDuration() < 30) {
            player.addEffect(Effect.getEffect(Effect.SLOWNESS).setDuration(40).setVisible(false));
        }
    }

    /**
     * 队伍胜利判断
     * @return 有队伍符合胜利条件
     */
    private boolean victoryJudgment() {
        if (this.redScore >= this.victoryScore) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 1), 20);
            return true;
        }
        if (this.blueScore >= this.victoryScore) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 2), 20);
            return true;
        }
        return false;
    }

    /**
     * 初始化部分参数
     */
    @Override
    protected void initData() {
        super.initData();
        this.haveRedFlag = null;
        this.haveBlueFlag = null;
        this.redFlagStand = null;
        this.blueFlagStand = null;
        this.redFlag = null;
        this.blueFlag = null;
        this.overtime = false;
    }

    @Override
    public void startGame() {
        super.startGame();
        Server.getInstance().getScheduler().scheduleRepeatingTask(this.gunWar,
                new FlagTask(this.gunWar, this), 5);
    }

    @Override
    public void roundEnd(Team victory) {
        if (victory == Team.RED_DEATH) {
            victory = Team.RED;
        }else if (victory == Team.BLUE_DEATH) {
            victory = Team.BLUE;
        }

        GunWarRoomRoundEndEvent ev = new GunWarRoomRoundEndEvent(this, victory);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        Team v = ev.getVictoryTeam();
        Tools.cleanEntity(this.getLevel(), true);
        //本回合胜利计算
        if (v == Team.NULL) {
            if ((this.redScore - this.blueScore) > 0) {
                this.setStatus(3);
                Server.getInstance().getScheduler().scheduleRepeatingTask(
                        this.gunWar, new VictoryTask(this.gunWar, this, 1), 20);
                return;
            }else if ((this.blueScore - this.redScore) > 0) {
                this.setStatus(3);
                Server.getInstance().getScheduler().scheduleRepeatingTask(
                        this.gunWar, new VictoryTask(this.gunWar, this, 2), 20);
                return;
            }else if (!this.overtime) {
                this.overtime = true;
                this.gameTime = this.getSetGameTime() / 5;
                Tools.sendTitle(this, this.language.translateString("game_ctf_overtime"), "");
                return;
            }else {
                this.setStatus(3);
                Server.getInstance().getScheduler().scheduleRepeatingTask(
                        this.gunWar, new VictoryTask(this.gunWar, this, 0), 20);
                return;
            }
        }else if (v == Team.RED) {
            this.redScore++;
            Tools.sendRoundVictoryTitle(this, Team.RED);
        }else {
            this.blueScore++;
            Tools.sendRoundVictoryTitle(this, Team.BLUE);
        }
        //房间胜利计算
        if (this.victoryJudgment()) {
            return;
        }
        this.roundStart();
    }

    @Override
    public void playerDeath(Player player, Entity damager, String killMessage) {
        super.playerDeath(player, damager, killMessage);
        if (this.haveRedFlag == player) {
            this.haveRedFlag = null;
            this.redFlag.y -= 1.5;
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.gunWar,
                    new FlagPickupCheckTask(this.gunWar, this, this.redFlag), 20);
        }else if (this.haveBlueFlag == player) {
            this.haveBlueFlag = null;
            this.blueFlag.y -= 1.5;
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.gunWar,
                    new FlagPickupCheckTask(this.gunWar, this, this.blueFlag), 20);
        }
    }

    /**
     * 拾取红队旗帜
     *
     * @param player 拾取旗帜的玩家
     */
    public void redFlagPickup(Player player) {
        this.haveRedFlag = player;
        Tools.sendTitle(this, "",
                language.translateString("game_ctf_playerPickUpTheFlag", player.getName(), Tools.getShowTeamName(Team.RED)));
    }

    /**
     * 拾取蓝队旗帜
     *
     * @param player 拾取旗帜的玩家
     */
    public void blueFlagPickup(Player player) {
        this.haveBlueFlag = player;
        Tools.sendTitle(this, "",
                language.translateString("game_ctf_playerPickUpTheFlag", player.getName(), Tools.getShowTeamName(Team.BLUE)));
    }

    /**
     * 红方旗帜被送达到蓝方
     *
     * @param player 运送旗帜的玩家
     */
    public void redFlagDelivery(@NotNull Player player) {
        this.redFlag.teleport(new Vector3(this.getRedSpawn().getX(),
                this.getRedSpawn().getY() + 0.3D,
                this.getRedSpawn().getZ()));
        this.blueScore++;
        this.haveRedFlag = null;
        Tools.playSound(this, Sound.RANDOM_LEVELUP);
        this.addPlayerIntegral(player, IntegralConfig.getIntegral(IntegralConfig.IntegralType.FLAG_GET_SCORE));
        Tools.sendTitle(this, "", this.language.translateString("game_ctf_playerDeliveryFlag", player.getName(), Team.BLUE.getShowName()));
    }

    /**
     * 蓝方旗帜被送达到红方
     *
     * @param player 运送旗帜的玩家
     */
    public void blueFlagDelivery(@NotNull Player player) {
        this.blueFlag.teleport(new Vector3(this.getBlueSpawn().getX(),
                this.getBlueSpawn().getY() + 0.3D,
                this.getBlueSpawn().getZ()));
        this.redScore++;
        this.haveBlueFlag = null;
        Tools.playSound(this, Sound.RANDOM_LEVELUP);
        this.addPlayerIntegral(player, IntegralConfig.getIntegral(IntegralConfig.IntegralType.FLAG_GET_SCORE));
        Tools.sendTitle(this, "", this.language.translateString("game_ctf_playerDeliveryFlag", player.getName(), Team.RED.getShowName()));
    }

}
