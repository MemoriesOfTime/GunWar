package cn.lanink.gunwar.room.capturetheflag;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.event.GunWarRoomRoundEndEvent;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.tasks.VictoryTask;
import cn.lanink.gunwar.tasks.game.ctf.FlagPickupCheckTask;
import cn.lanink.gunwar.tasks.game.ctf.FlagTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lt_name
 */
public class CTFModeRoom extends BaseRoom {

    private final HashMap<Player, Integer> playerRespawnTime = new HashMap<>();
    public Player haveRedFlag, haveBlueFlag;
    public EntityFlagStand redFlagStand, blueFlagStand;
    public EntityFlag redFlag, blueFlag;
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
        if (this.haveRedFlag != null) {
            this.haveRedFlag.addEffect(Effect.getEffect(2).setDuration(40).setVisible(false));
        }
        if (this.haveBlueFlag != null) {
            this.haveBlueFlag.addEffect(Effect.getEffect(2).setDuration(40).setVisible(false));
        }
        int red = 0, blue = 0;
        for (Map.Entry<Player, Integer> entry : this.getPlayerRespawnTime().entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
                if (entry.getValue() == 0) {
                    Server.getInstance().getScheduler().scheduleTask(this.gunWar, new Task() {
                        @Override
                        public void onRun(int i) {
                            playerRespawn(entry.getKey());
                        }
                    });
                }else if (entry.getValue() <= 5) {
                    Tools.addSound(entry.getKey(), Sound.RANDOM_CLICK);
                }
            }
        }
        if (victoryJudgment()) return;
        for (int team : this.getPlayers().values()) {
            switch (team) {
                case 1:
                case 11:
                    red++;
                    break;
                case 2:
                case 12:
                    blue++;
                    break;
            }
        }
        if (red == 0) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 2), 20);
        } else if (blue == 0) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 1), 20);
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
        if (this.playerRespawnTime != null) {
            this.playerRespawnTime.clear();
        }
        this.overtime = false;
    }

    @Override
    public void startGame() {
        super.startGame();
        Server.getInstance().getScheduler().scheduleRepeatingTask(this.gunWar,
                new FlagTask(this.gunWar, this), 5);
    }

    public void roundEnd(int victory) {
        GunWarRoomRoundEndEvent ev = new GunWarRoomRoundEndEvent(this, victory);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        int v = ev.getVictory();
        Tools.cleanEntity(this.getLevel(), true);
        //本回合胜利计算
        if (v == 0) {
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
                Tools.sendTitle(this, this.language.game_ctf_overtime, "");
                return;
            }else {
                this.setStatus(3);
                Server.getInstance().getScheduler().scheduleRepeatingTask(
                        this.gunWar, new VictoryTask(this.gunWar, this, 0), 20);
                return;
            }
        }else if (v == 1) {
            this.redScore++;
            Tools.sendRoundVictoryTitle(this, 1);
        }else {
            this.blueScore++;
            Tools.sendRoundVictoryTitle(this, 2);
        }
        //房间胜利计算
        if (victoryJudgment()) return;
        this.roundStart();
    }

    /**
     * 获取玩家重生时间
     * @return 玩家重生时间Map
     */
    public HashMap<Player, Integer> getPlayerRespawnTime() {
        return this.playerRespawnTime;
    }

    public int getPlayerRespawnTime(Player player) {
        if (this.playerRespawnTime.containsKey(player)) {
            return this.playerRespawnTime.get(player);
        }
        return 0;
    }

    @Override
    public void playerDeath(Player player, Player damager, String killMessage) {
        super.playerDeath(player, damager, killMessage);
        this.getPlayerRespawnTime().put(player, 20);
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

}
