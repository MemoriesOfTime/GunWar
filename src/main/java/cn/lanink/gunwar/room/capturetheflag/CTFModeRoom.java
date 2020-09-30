package cn.lanink.gunwar.room.capturetheflag;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.event.GunWarRoomRoundEndEvent;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.room.classic.ClassicModeRoom;
import cn.lanink.gunwar.tasks.VictoryTask;
import cn.lanink.gunwar.tasks.game.FlagPickupCheckTask;
import cn.lanink.gunwar.tasks.game.FlagTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lt_name
 */
public class CTFModeRoom extends ClassicModeRoom {

    private final HashMap<Player, Integer> playerRespawnTime = new HashMap<>();
    public Player haveRedFlag, haveBlueFlag;
    public EntityFlagStand redFlagStand, blueFlagStand;
    public EntityFlag redFlag, blueFlag;

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
        if (this.getPlayers().size() < 1) {
            this.endGame();
            return;
        }
        if (this.gameTime > 0) {
            this.gameTime--;
        }else {
            Server.getInstance().getScheduler().scheduleTask(this.gunWar, () -> this.roundEnd(0));
            this.gameTime = this.getSetGameTime();
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
        if (this.redScore >= this.victoryScore) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 1), 20);
            return;
        }
        if (this.blueScore >= this.victoryScore) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 2), 20);
            return;
        }
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
        this.playerRespawnTime.clear();
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
        for (Player player : this.getPlayers().keySet()) {
            for (GunWeapon weapon : ItemManage.getGunWeaponMap().values()) {
                weapon.getMagazineMap().remove(player);
            }
        }
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
            } else {
                this.gameTime = this.getSetGameTime() / 5;
                //TODO 添加加时赛提示
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
        if (this.redScore >= this.victoryScore) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 1), 20);
            return;
        }
        if (this.blueScore >= this.victoryScore) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 2), 20);
            return;
        }
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
    public void playerDeath(Player player, Player damager) {
        super.playerDeath(player, damager);
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
