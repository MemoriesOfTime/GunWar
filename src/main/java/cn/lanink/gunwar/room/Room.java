package cn.lanink.gunwar.room;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.item.base.BaseItem;
import cn.lanink.gunwar.tasks.WaitTask;
import cn.lanink.gunwar.utils.SavePlayerInventory;
import cn.lanink.gunwar.utils.Tips;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.*;

/**
 * 房间
 */
public class Room extends BaseRoom {

    private final String redSpawn, blueSpawn;
    private final HashMap<Player, Float> playerHealth = new HashMap<>(); //玩家血量
    public int redScore, blueScore; //队伍得分
    private final GameMode gameMode;
    public HashSet<Player> swordAttackCD = new HashSet<>();
    public final int victoryScore; //胜利需要分数

    protected HashMap<BaseItem.ItemType, ArrayList<String>> initialItems = new HashMap<>();

    //夺旗模式数据
    private final HashMap<Player, Integer> playerRespawnTime = new HashMap<>();
    public Player haveRedFlag, haveBlueFlag;
    public EntityFlagStand redFlagStand, blueFlagStand;
    public EntityFlag redFlag, blueFlag;

    /**
     * 初始化
     * @param config 配置文件
     */
    public Room(Config config) {
        this.level = config.getString("World");
        this.waitSpawn = config.getString("waitSpawn");
        this.redSpawn = config.getString("redSpawn");
        this.blueSpawn = config.getString("blueSpawn");
        this.setWaitTime = config.getInt("waitTime");
        this.setGameTime = config.getInt("gameTime");
        this.victoryScore = config.getInt("victoryScore", 5);
        switch (config.getInt("gameMode", 0)) {
            case 1:
                this.gameMode = GameMode.CTF;
                break;
            case 0:
            default:
                this.gameMode = GameMode.CLASSIC;
                break;
        }
        this.initialItems.put(BaseItem.ItemType.MELEE_WEAPON, new ArrayList<>(config.getStringList("initialItems.weapon.melee")));
        //TODO

        this.initTime();
        if (this.getLevel() == null) {
            Server.getInstance().loadLevel(this.level);
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
    protected void initTime() {
        super.initTime();
        this.redScore = 0;
        this.blueScore = 0;
    }

    @Override
    public void endGame() {
        this.endGame(true);
    }

    /**
     * 结束房间
     */
    public synchronized void endGame(boolean normal) {
        this.status = 0;
        if (normal) {
            if (this.players.size() > 0) {
                Iterator<Map.Entry<Player, Integer>> it = players.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Player, Integer> entry = it.next();
                    it.remove();
                    quitRoomOnline(entry.getKey());
                }
            }
            this.players.clear();
        }else {
            getLevel().getPlayers().values().forEach(
                    player -> player.kick(language.roomSafeKick));
        }
        this.playerHealth.clear();
        this.playerRespawnTime.clear();
        this.swordAttackCD.clear();
        initTime();
        this.haveRedFlag = null;
        this.haveBlueFlag = null;
        this.redFlagStand = null;
        this.blueFlagStand = null;
        this.redFlag = null;
        this.blueFlag = null;
        Tools.cleanEntity(getLevel(), true);
    }

    /**
     * 加入房间
     * @param player 玩家
     */
    @Override
    public void joinRoom(Player player) {
        if (this.status == 0) {
            this.initTask();
        }
        this.players.put(player, 0);
        this.playerHealth.put(player, 20F);
        SavePlayerInventory.save(player);
        Tools.rePlayerState(player, true);
        player.teleport(this.getWaitSpawn());
        player.getInventory().setItem(3, Tools.getItem(11));
        player.getInventory().setItem(5, Tools.getItem(12));
        player.getInventory().setItem(8, Tools.getItem(10));
        if (GunWar.getInstance().isHasTips()) {
            Tips.closeTipsShow(this.level, player);
        }
        player.sendMessage(this.language.joinRoom.replace("%name%", this.level));
        Server.getInstance().getScheduler().scheduleDelayedTask(GunWar.getInstance(), new Task() {
            @Override
            public void onRun(int i) {
                if (player.getLevel() != getLevel()) {
                    quitRoom(player, true);
                }
            }
        }, 20);
    }

    @Override
    public void quitRoomOnline(Player player) {
        if (GunWar.getInstance().isHasTips()) {
            Tips.removeTipsConfig(this.level, player);
        }
        GunWar.getInstance().getScoreboard().closeScoreboard(player);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);
        SavePlayerInventory.restore(player);
        player.sendMessage(this.language.quitRoom);
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    /**
     * @return 开局给与的装备
     */
    public HashMap<BaseItem.ItemType, ArrayList<String>> getInitialItems() {
        return this.initialItems;
    }

    /**
     * 获取玩家血量Map
     * @return 玩家血量Map
     */
    public HashMap<Player, Float> getPlayerHealth() {
        return this.playerHealth;
    }

    public float getPlayerHealth(Player player) {
        if (this.playerHealth.containsKey(player)) {
            return this.playerHealth.get(player);
        }
        return 0;
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

    /**
     * 增加玩家血量
     * @param player 玩家
     * @param health 血量
     */
    public void addHealth(Player player, float health) {
        float nowHealth = this.playerHealth.get(player) + health;
        if (nowHealth > 20) {
            this.playerHealth.put(player, 20F);
        }else {
            this.playerHealth.put(player, nowHealth);
        }
    }

    /**
     * 减少玩家血量
     * @param player 玩家
     * @param health 血量
     */
    public void lessHealth(Player player, Player damager, float health) {
        float nowHealth = this.playerHealth.get(player) - health;
        if (nowHealth <= 0) {
            if (damager != null) {
                this.playerHealth.put(player, 0F);
            }else {
                this.playerHealth.put(player, 1F);
            }
        }else {
            this.playerHealth.put(player, nowHealth);
        }
    }

    /**
     * 获取红队出生点
     * @return 出生点
     */
    public Position getRedSpawn() {
        String[] s = this.redSpawn.split(":");
        return new Position(Integer.parseInt(s[0]),
                Integer.parseInt(s[1]),
                Integer.parseInt(s[2]),
                this.getLevel());
    }

    /**
     * 获取蓝队出生点
     * @return 出生点
     */
    public Position getBlueSpawn() {
        String[] s = this.blueSpawn.split(":");
        return new Position(Integer.parseInt(s[0]),
                Integer.parseInt(s[1]),
                Integer.parseInt(s[2]),
                this.getLevel());
    }

}
