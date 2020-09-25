package cn.lanink.gunwar.room;

import cn.lanink.gamecore.utils.SavePlayerInventory;
import cn.lanink.gamecore.utils.Tips;
import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.event.GunWarRoomEndEvent;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.tasks.WaitTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 房间
 */
public class Room extends BaseRoom {

    private final GameMode gameMode;

    //夺旗模式数据
    private final HashMap<Player, Integer> playerRespawnTime = new HashMap<>();
    public Player haveRedFlag, haveBlueFlag;
    public EntityFlagStand redFlagStand, blueFlagStand;
    public EntityFlag redFlag, blueFlag;

    /**
     * 初始化
     * @param config 配置文件
     */
    public Room(Level level, Config config) throws RoomLoadException {
        super(level, config);
        switch (config.getInt("gameMode", 0)) {
            case 1:
                this.gameMode = GameMode.CTF;
                break;
            case 0:
            default:
                this.gameMode = GameMode.CLASSIC;
                break;
        }
        this.initData();
        if (this.getLevel() == null) {
            Server.getInstance().loadLevel(this.getLevelName());
        }
        this.status = 0;
    }

    /**
     * 初始化Task
     */
    @Override
    protected void initTask() {
        this.setStatus(1);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                GunWar.getInstance(), new WaitTask(GunWar.getInstance(), this), 20);
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
    }

    /**
     * 结束房间
     */
    @Override
    public synchronized void endGame(int victory) {
        this.status = 0;
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomEndEvent(this, victory));
        if (this.players.size() > 0) {
            Iterator<Map.Entry<Player, Integer>> it = players.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Player, Integer> entry = it.next();
                it.remove();
                this.quitRoom(entry.getKey());
            }
        }
        this.players.clear();
        this.playerHealth.clear();
        this.playerRespawnTime.clear();
        initData();
        Tools.cleanEntity(getLevel(), true);
    }

    /**
     * 加入房间
     * @param player 玩家
     */
    @Override
    public void joinRoom(Player player, boolean spectator) {
        if (this.status == 0) {
            this.initTask();
        }
        this.players.put(player, 0);
        this.playerHealth.put(player, 20F);
        SavePlayerInventory.save(GunWar.getInstance(), player);
        Tools.rePlayerState(player, true);
        player.teleport(this.getWaitSpawn());
        player.getInventory().setItem(3, Tools.getItem(11));
        player.getInventory().setItem(5, Tools.getItem(12));
        player.getInventory().setItem(8, Tools.getItem(10));
        if (GunWar.getInstance().isHasTips()) {
            Tips.closeTipsShow(this.getLevelName(), player);
        }
        player.sendMessage(this.language.joinRoom.replace("%name%", this.getLevelName()));
        Server.getInstance().getScheduler().scheduleDelayedTask(GunWar.getInstance(), new Task() {
            @Override
            public void onRun(int i) {
                if (player.getLevel() != getLevel()) {
                    quitRoom(player, true);
                }
            }
        }, 20);
    }

    public GameMode getGameMode() {
        return this.gameMode;
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

}
