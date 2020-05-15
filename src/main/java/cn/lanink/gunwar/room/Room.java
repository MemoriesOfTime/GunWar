package cn.lanink.gunwar.room;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.tasks.TipsTask;
import cn.lanink.gunwar.tasks.WaitTask;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.SavePlayerInventory;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import tip.messages.BossBarMessage;
import tip.messages.NameTagMessage;
import tip.utils.Api;

import java.util.*;

/**
 * 房间
 */
public class Room {

    private final Language language = GunWar.getInstance().getLanguage();
    private int mode; //0未初始化 1等待 2游戏 3胜利结算 4等待下一回合
    private final String level, waitSpawn, redSpawn, blueSpawn;
    private final int setWaitTime, setGameTime;
    public int waitTime, gameTime;
    private LinkedHashMap<Player, Integer> players = new LinkedHashMap<>(); //0未分配 1 11红队 2 12蓝队
    private LinkedHashMap<Player, Float> playerHealth = new LinkedHashMap<>(); //玩家血量
    public int redRound, blueRound; //队伍胜利次数
    public int victory;
    public ArrayList<String> task = new ArrayList<>();

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
        this.initTime();
        if (this.getLevel() == null) {
            Server.getInstance().loadLevel(this.level);
        }
        this.mode = 0;
    }

    /**
     * 初始化Task
     */
    private void initTask() {
        this.setMode(1);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                GunWar.getInstance(), new WaitTask(GunWar.getInstance(), this), 20, true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                GunWar.getInstance(), new TipsTask(GunWar.getInstance(), this), 10);
    }

    /**
     * 初始化部分参数
     */
    private void initTime() {
        this.waitTime = this.setWaitTime;
        this.gameTime = this.setGameTime;
        this.redRound = 0;
        this.blueRound = 0;
        this.victory = 0;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }

    public void endGame() {
        this.endGame(true);
    }

    /**
     * 结束房间
     */
    public void endGame(boolean normal) {
        this.mode = 0;
        if (normal) {
            if (this.players.size() > 0) {
                this.players.keySet().forEach(this::quitRoomOnline);
            }
            this.players.clear();
        }else {
            this.getLevel().getPlayers().values().forEach(
                    player -> player.kick(this.language.roomSafeKick));
        }
        this.playerHealth.clear();
        this.initTime();
        this.task = new ArrayList<>();
        Tools.cleanEntity(this.getLevel());
    }

    /**
     * 加入房间
     * @param player 玩家
     */
    public void joinRoom(Player player) {
        if (this.mode == 0) {
            this.initTask();
        }
        this.players.put(player, 0);
        this.playerHealth.put(player, 20F);
        SavePlayerInventory.save(player);
        Tools.rePlayerState(player, true);
        player.teleport(this.getWaitSpawn());
        Item item = Item.get(324, 0, 1);
        item.setNamedTag(new CompoundTag()
                .putBoolean("isGunWarItem", true)
                .putInt("GunWarType", 10));
        item.setCustomName("§c退出房间");
        item.setLore("手持点击,即可退出房间");
        player.getInventory().setItem(8, item);
        NameTagMessage nameTagMessage = new NameTagMessage(this.level, true, "");
        Api.setPlayerShowMessage(player.getName(), nameTagMessage);
        BossBarMessage bossBarMessage = new BossBarMessage(this.level, false, 5, false, new LinkedList<>());
        Api.setPlayerShowMessage(player.getName(), bossBarMessage);
        player.sendMessage(this.language.joinRoom.replace("%name%", this.level));
    }

    /**
     * 退出房间
     * @param player 玩家
     */
    public void quitRoom(Player player, boolean online) {
        if (this.isPlaying(player)) {
            this.players.remove(player);
        }
        if (online) {
            this.quitRoomOnline(player);
        }
    }

    private void quitRoomOnline(Player player) {
        Tools.removePlayerShowMessage(this.level, player);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);
        SavePlayerInventory.restore(player);
        player.sendMessage(this.language.quitRoom);
    }

    public boolean isPlaying(Player player) {
        return this.players.containsKey(player);
    }

    /**
     * 获取玩家列表
     * @return 玩家列表
     */
    public LinkedHashMap<Player, Integer> getPlayers() {
        return this.players;
    }

    /**
     * 获取玩家血量Map
     * @return 玩家血量Map
     */
    public LinkedHashMap<Player, Float> getPlayerHealth() {
        return playerHealth;
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
    public void lessHealth(Player player, Player damage, float health) {
        float nowHealth = this.playerHealth.get(player) - health;
        if (nowHealth <= 0) {
            this.playerHealth.put(player, 0F);
        }else {
            this.playerHealth.put(player, nowHealth);
        }
    }

    /**
     * 获取玩家队伍
     * @param player 玩家
     * @return 所属队伍
     */
    public int getPlayerMode(Player player) {
        if (this.isPlaying(player)) {
            return this.players.get(player);
        }
        return 0;
    }

    public int getWaitTime() {
        return this.setWaitTime;
    }

    public int getGameTime() {
        return this.setGameTime;
    }

    /**
     * 获取世界
     * @return 世界
     */
    public Level getLevel() {
        return Server.getInstance().getLevelByName(this.level);
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
