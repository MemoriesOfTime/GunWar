package cn.lanink.gunwar.room.base;

import cn.lanink.gamecore.room.IRoom;
import cn.lanink.gamecore.utils.FileUtil;
import cn.lanink.gamecore.utils.SavePlayerInventory;
import cn.lanink.gamecore.utils.Tips;
import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarPlayerDeathEvent;
import cn.lanink.gunwar.event.GunWarRoomStartEvent;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础/通用 房间类
 * @author lt_name
 */
public abstract class BaseRoom implements IRoom {

    protected final GunWar gunWar = GunWar.getInstance();
    protected final Language language = GunWar.getInstance().getLanguage();
    protected int setWaitTime, setGameTime;
    public int waitTime, gameTime;
    protected int status;
    private Level level;
    private final String levelName;
    protected final String waitSpawn;
    protected final String redSpawn, blueSpawn;
    protected HashMap<ItemManage.ItemType, ArrayList<String>> initialItems = new HashMap<>();
    protected ConcurrentHashMap<Player, Integer> players = new ConcurrentHashMap<>(); //0未分配 1 11红队 2 12蓝队
    protected final HashMap<Player, Float> playerHealth = new HashMap<>(); //玩家血量
    public int redScore, blueScore; //队伍得分
    public final int victoryScore; //胜利需要分数

    public BaseRoom(Level level, Config config) throws RoomLoadException {
        this.level = level;
        this.levelName = level.getFolderName();
        this.waitSpawn = config.getString("waitSpawn");
        this.redSpawn = config.getString("redSpawn");
        this.blueSpawn = config.getString("blueSpawn");
        this.setWaitTime = config.getInt("waitTime");
        this.setGameTime = config.getInt("gameTime");
        this.victoryScore = config.getInt("victoryScore", 5);
        File backup = new File(this.gunWar.getWorldBackupPath() + this.levelName);
        if (!backup.exists()) {
            this.gunWar.getLogger().info("§a房间：%name% 未检测到地图备份，正在备份地图中...");
            Server.getInstance().unloadLevel(this.level);
            if (FileUtil.copyDir(Server.getInstance().getFilePath() + "/worlds/" + this.levelName, backup)) {
                Server.getInstance().loadLevel(this.levelName);
                this.level = Server.getInstance().getLevelByName(this.levelName);
            }else {
                throw new RoomLoadException("房间地图备份失败！ / The room world backup failed!");
            }
        }
        ArrayList<String> defaultItems = new ArrayList<>(Collections.singletonList("demo:1"));
        this.initialItems.put(ItemManage.ItemType.MELEE_WEAPON,
                new ArrayList<>(config.getStringList("initialItems.weapon.melee")));
        if (this.initialItems.get(ItemManage.ItemType.MELEE_WEAPON).isEmpty()) {
            config.set("initialItems.weapon.melee", new ArrayList<>(defaultItems));
        }
        this.initialItems.put(ItemManage.ItemType.PROJECTILE_WEAPON,
                new ArrayList<>(config.getStringList("initialItems.weapon.projectile")));
        if (this.initialItems.get(ItemManage.ItemType.PROJECTILE_WEAPON).isEmpty()) {
            config.set("initialItems.weapon.projectile", new ArrayList<>(defaultItems));
        }
        this.initialItems.put(ItemManage.ItemType.GUN_WEAPON,
                new ArrayList<>(config.getStringList("initialItems.weapon.gun")));
        if (this.initialItems.get(ItemManage.ItemType.GUN_WEAPON).isEmpty()) {
            config.set("initialItems.weapon.gun", new ArrayList<>(defaultItems));
        }
        config.save();
    }

    /**
     * 初始化task
     */
    protected abstract void initTask();

    /**
     * 初始化倒计时时间
     */
    protected void initData() {
        this.waitTime = this.setWaitTime;
        this.gameTime = this.setGameTime;
        this.redScore = 0;
        this.blueScore = 0;
    }

    public int getStatus() {
        return this.status;
    }

    /**
     * 设置房间状态
     * @param status 状态
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public void startGame() {
        //TODO
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomStartEvent((Room) this));
    }

    public void endGame() {
        this.endGame(0);
    }

    /**
     * 结束房间
     */
    public abstract void endGame(int victory);

    public void joinRoom(Player player) {
        this.joinRoom(player, false);
    }

    /**
     * 加入房间
     * @param player 玩家
     */
    public abstract void joinRoom(Player player, boolean spectator);

    public void quitRoom(Player player, boolean online) {
        this.quitRoom(player);
    }

    /**
     * 退出房间
     * @param player 玩家
     */
    public void quitRoom(Player player) {
        this.players.remove(player);
        if (GunWar.getInstance().isHasTips()) {
            Tips.removeTipsConfig(this.levelName, player);
        }
        GunWar.getInstance().getScoreboard().closeScoreboard(player);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);
        SavePlayerInventory.restore(GunWar.getInstance(), player);
        player.sendMessage(this.language.quitRoom);
    }

    /**
     * 获取玩家是否在房间内
     * @param player 玩家
     * @return 是否在房间
     */
    public boolean isPlaying(Player player) {
        return this.players.containsKey(player);
    }

    /**
     * 获取玩家列表
     * @return 玩家列表
     */
    public ConcurrentHashMap<Player, Integer> getPlayers() {
        return this.players;
    }

    /**
     * 获取玩家身份
     * @param player 玩家
     * @return 玩家身份
     */
    public int getPlayers(Player player) {
        return this.players.getOrDefault(player, 0);
    }

    /**
     * 获取玩家血量Map
     * @return 玩家血量Map
     */
    public HashMap<Player, Float> getPlayerHealth() {
        return this.playerHealth;
    }

    public float getPlayerHealth(Player player) {
        return this.playerHealth.getOrDefault(player, 0F);
    }

    /**
     * 增加玩家血量
     * @param player 玩家
     * @param health 血量
     */
    public synchronized float addHealth(Player player, float health) {
        float nowHealth = this.playerHealth.get(player) + health;
        if (nowHealth > 20) {
            this.playerHealth.put(player, 20F);
        }else {
            this.playerHealth.put(player, nowHealth);
        }
        return this.playerHealth.get(player);
    }

    /**
     * 减少玩家血量
     * @param player 玩家
     * @param health 血量
     */
    public synchronized float lessHealth(Player player, Player damager, float health) {
        float nowHealth = this.playerHealth.get(player) - health;
        if (nowHealth < 1) {
            this.playerHealth.put(player, 0F);
            Server.getInstance().getPluginManager().callEvent(new GunWarPlayerDeathEvent((Room) this, player, damager));
        }else {
            this.playerHealth.put(player, nowHealth);
        }
        return this.playerHealth.get(player);
    }

    /**
     * @return 开局给予的装备
     */
    public HashMap<ItemManage.ItemType, ArrayList<String>> getInitialItems() {
        return this.initialItems;
    }

    /**
     * 获取设置的等待时间
     * @return 等待时间
     */
    public int getSetWaitTime() {
        return this.setWaitTime;
    }

    /**
     * 获取设置的游戏时间
     * @return 游戏时间
     */
    public int getSetGameTime() {
        return this.setGameTime;
    }

    /**
     * 获取世界
     * @return 世界
     */
    public Level getLevel() {
        return this.level;
    }

    public String getLevelName() {
        return this.levelName;
    }

    /**
     * 获取等待出生点
     * @return 出生点
     */
    public Position getWaitSpawn() {
        String[] s = this.waitSpawn.split(":");
        return new Position(Integer.parseInt(s[0]),
                Integer.parseInt(s[1]),
                Integer.parseInt(s[2]),
                this.getLevel());
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
