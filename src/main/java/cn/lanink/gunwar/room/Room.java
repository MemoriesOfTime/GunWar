package cn.lanink.gunwar.room;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.tasks.WaitTask;
import cn.lanink.gunwar.utils.SavePlayerInventory;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import tip.messages.BossBarMessage;
import tip.messages.NameTagMessage;
import tip.utils.Api;

import java.util.*;

/**
 * 房间
 */
public class Room extends BaseRoom {

    private final String redSpawn, blueSpawn;
    private LinkedHashMap<Player, Float> playerHealth = new LinkedHashMap<>(); //玩家血量
    public int redRound, blueRound; //队伍胜利次数
    public LinkedList<Player> swordAttackCD = new LinkedList<>();

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
    @Override
    protected void initTask() {
        this.setMode(1);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                GunWar.getInstance(), new WaitTask(GunWar.getInstance(), this), 20, true);
    }

    /**
     * 初始化部分参数
     */
    @Override
    protected void initTime() {
        super.initTime();
        this.redRound = 0;
        this.blueRound = 0;
    }

    @Override
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
    @Override
    public void joinRoom(Player player) {
        if (this.mode == 0) {
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
        NameTagMessage nameTagMessage = new NameTagMessage(this.level, true, "");
        Api.setPlayerShowMessage(player.getName(), nameTagMessage);
        BossBarMessage bossBarMessage = new BossBarMessage(this.level, false, 5, false, new LinkedList<>());
        Api.setPlayerShowMessage(player.getName(), bossBarMessage);
        player.sendMessage(this.language.joinRoom.replace("%name%", this.level));
    }

    @Override
    public void quitRoomOnline(Player player) {
        Tools.removePlayerShowMessage(this.level, player);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);
        SavePlayerInventory.restore(player);
        player.sendMessage(this.language.quitRoom);
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
