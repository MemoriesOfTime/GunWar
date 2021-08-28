package cn.lanink.gunwar.room.base;

import cn.lanink.gamecore.GameCore;
import cn.lanink.gamecore.room.IRoom;
import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.utils.FileUtil;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gamecore.utils.SavePlayerInventory;
import cn.lanink.gamecore.utils.Tips;
import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.event.*;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.tasks.VictoryTask;
import cn.lanink.gunwar.tasks.WaitTask;
import cn.lanink.gunwar.tasks.game.ScoreBoardTask;
import cn.lanink.gunwar.tasks.game.ShowHealthTask;
import cn.lanink.gunwar.tasks.game.TimeTask;
import cn.lanink.gunwar.utils.Tools;
import cn.lanink.gunwar.utils.gamerecord.GameRecord;
import cn.lanink.gunwar.utils.gamerecord.RecordType;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础/通用 房间类
 * @author lt_name
 */
public abstract class BaseRoom implements IRoom, ITimeTask {

    protected final GunWar gunWar = GunWar.getInstance();
    protected final Language language = GunWar.getInstance().getLanguage();
    private String gameMode = null;
    protected int status;
    private Level level;
    private final String levelName;
    protected int minPlayers, maxPlayers;
    protected final String waitSpawn;
    protected final String redSpawn, blueSpawn;
    protected int setWaitTime, setGameTime;
    public int waitTime, gameTime;
    protected ArrayList<String> initialItems = new ArrayList<>();
    protected ConcurrentHashMap<Player, Integer> players = new ConcurrentHashMap<>(); //0未分配 1 11红队 2 12蓝队
    protected final HashMap<Player, Float> playerHealth = new HashMap<>(); //玩家血量
    public int redScore, blueScore; //队伍得分
    public final int victoryScore; //胜利需要分数
    protected boolean roundIsEnd = false; //防止重复执行回合结束方法

    /**
     * 初始化
     * @param level 游戏世界
     * @param config 配置文件
     */
    public BaseRoom(Level level, Config config) throws RoomLoadException {
        this.level = level;
        this.levelName = level.getFolderName();
        this.minPlayers = config.getInt("minPlayers", 2);
        if (this.minPlayers < 2) {
            this.minPlayers = 2;
        }
        this.maxPlayers = config.getInt("maxPlayers", 10);
        if (this.maxPlayers < this.minPlayers) {
            this.maxPlayers = this.minPlayers;
        }
        this.waitSpawn = config.getString("waitSpawn");
        this.redSpawn = config.getString("redSpawn");
        this.blueSpawn = config.getString("blueSpawn");
        this.setWaitTime = config.getInt("waitTime");
        this.setGameTime = config.getInt("gameTime");
        this.victoryScore = config.getInt("victoryScore", 5);
        File backup = new File(this.gunWar.getWorldBackupPath() + this.levelName);
        if (!backup.exists()) {
            this.gunWar.getLogger().info(this.language.translateString("roomLevelBackup", this.levelName));
            Server.getInstance().unloadLevel(this.level, true);
            if (FileUtil.copyDir(Server.getInstance().getFilePath() + "/worlds/" + this.levelName, backup)) {
                Server.getInstance().loadLevel(this.levelName);
                this.level = Server.getInstance().getLevelByName(this.levelName);
            }else {
                throw new RoomLoadException("房间地图备份失败！ / The room world backup failed!");
            }
        }
        this.initialItems.addAll(config.getStringList("initialItems"));
        if (this.initialItems.isEmpty()) {
            ArrayList<String> defaultItems = new ArrayList<>(
                    Arrays.asList(
                            "373:28&1@item",
                            "322&1@item",
                            "DemoMelee&1@weapon_melee",
                            "DemoGrenade&1@weapon_projectile",
                            "DemoFlashbang&1@weapon_projectile",
                            "DemoGun&1@weapon_gun"));
            config.set("initialItems", defaultItems);
            config.save(true);
            this.initialItems.addAll(defaultItems);
        }
        this.initData();
        for (String name : this.getListeners()) {
            try {
                this.gunWar.getGameListeners().get(name).addListenerRoom(this);
            }catch (Exception e) {
                throw new RoomLoadException("Listener enable error!");
            }
        }
        this.setStatus(ROOM_STATUS_TASK_NEED_INITIALIZED);
    }

    public final void setGameMode(String gameMode) {
        if (this.gameMode == null) {
            this.gameMode = gameMode;
        }
    }

    public final String getGameMode() {
        return gameMode;
    }

    /**
     * 设置房间状态
     * @param status 状态
     */
    @Override
    public final void setStatus(int status) {
        this.status = status;
    }

    @Override
    public final int getStatus() {
        return this.status;
    }

    /**
     * @return 房间最少人数
     */
    public int getMinPlayers() {
        return this.minPlayers;
    }

    /**
     * @return 房间最多人数
     */
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    /**
     * @return 使用的监听器
     */
    public List<String> getListeners() {
        return new ArrayList<>(Arrays.asList(
                "RoomLevelProtection",
                "DefaultChatListener",
                "DefaultGameListener",
                "DefaultDamageListener"));
    }

    public ITimeTask getTimeTask() {
        return this;
    }

    @Override
    public void timeTask() {
        if (this.getPlayers().isEmpty()) {
            this.endGame();
            return;
        }

        if (this.gunWar.isEnableAloneHealth()) {
            for (Player player : this.players.keySet()) {
                player.setHealth(player.getMaxHealth() - 1);
            }
        }

        if(!this.roundIsEnd) {
            if (this.gameTime <= 0) {
                Server.getInstance().getScheduler().scheduleTask(this.gunWar, () -> this.roundEnd(0));
                this.gameTime = this.getSetGameTime();
                return;
            }
            this.gameTime--;
        }
    }

    /**
     * 初始化Task （等待状态）
     */
    protected void initTask() {
        this.setStatus(1);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                GunWar.getInstance(), new WaitTask(GunWar.getInstance(), this), 20);
    }

    /**
     * 初始化房间数据
     */
    protected void initData() {
        this.waitTime = this.setWaitTime;
        this.gameTime = this.setGameTime;
        this.redScore = 0;
        this.blueScore = 0;
        this.players.clear();
        this.playerHealth.clear();
        this.roundIsEnd = false;
    }

    public void startGame() {
        this.setStatus(ROOM_STATUS_GAME);
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomStartEvent(this));
        this.assignTeam();
        this.roundStart();
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar, new TimeTask(this.gunWar, this.getTimeTask()), 20, true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar, new ScoreBoardTask(this.gunWar, this), 18, true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar, new ShowHealthTask(this.gunWar, this), 5, true);
    }

    public void assignTeam() {
        GunWarRoomAssignTeamEvent ev = new GunWarRoomAssignTeamEvent(this);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        LinkedList<Player> redTeam = new LinkedList<>();
        LinkedList<Player> blueTeam = new LinkedList<>();
        LinkedList<Player> noTeam = new LinkedList<>();
        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
            switch (entry.getValue()) {
                case 1:
                    redTeam.add(entry.getKey());
                    break;
                case 2:
                    blueTeam.add(entry.getKey());
                    break;
                default:
                    noTeam.add(entry.getKey());
                    break;
            }
        }
        //队伍平衡
        Player cache;
        while (true) {
            if (noTeam.size() > 0) {
                Collections.shuffle(noTeam, GunWar.RANDOM);
                for (Player player : noTeam) {
                    if (redTeam.size() > blueTeam.size()) {
                        blueTeam.add(player);
                    }else {
                        redTeam.add(player);
                    }
                }
                noTeam.clear();
            }
            if (redTeam.size() != blueTeam.size()) {
                if (Math.abs(redTeam.size() - blueTeam.size()) == 1) {
                    break;
                }
                if (redTeam.size() > blueTeam.size()) {
                    cache = redTeam.get(GunWar.RANDOM.nextInt(redTeam.size()));
                    redTeam.remove(cache);
                    blueTeam.add(cache);
                }else {
                    cache = blueTeam.get(GunWar.RANDOM.nextInt(blueTeam.size()));
                    blueTeam.remove(cache);
                    redTeam.add(cache);
                }
            }else {
                break;
            }
        }
        for (Player player : redTeam) {
            this.getPlayers().put(player, 1);
            player.sendTitle(this.language.translateString("teamNameRed"), "", 10, 30, 10);
            player.setNameTag("§c" + player.getName());
        }
        for (Player player : blueTeam) {
            this.getPlayers().put(player, 2);
            player.sendTitle(this.language.translateString("teamNameBlue"), "", 10, 30, 10);
            player.setNameTag("§9" + player.getName());
        }
    }

    public void endGame() {
        this.endGame(0);
    }

    /**
     * 结束房间
     */
    public void endGame(int victory) {
        int oldStatus = this.getStatus();
        this.setStatus(ROOM_STATUS_LEVEL_NOT_LOADED);
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomEndEvent(this, victory));
        LinkedList<Player> victoryPlayers = new LinkedList<>();
        LinkedList<Player> defeatPlayers = new LinkedList<>();
        if (this.getPlayers().size() > 0) {
            for (Player p1 : this.players.keySet()) {
                for (Player p2 : this.players.keySet()) {
                    p1.showPlayer(p2);
                    p2.showPlayer(p1);
                }
            }
            for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
                if (victory == 1) {
                    if (entry.getValue() == 1 || entry.getValue() == 11) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                }else if (victory == 2) {
                    if (entry.getValue() == 2 || entry.getValue() == 12) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                }
            }
            for (Player player : new HashSet<>(this.getPlayers().keySet())) {
                this.quitRoom(player);
            }
        }
        this.initData();
        Tools.cleanEntity(getLevel(), true);
        List<String> vCmds = GunWar.getInstance().getConfig().getStringList("胜利执行命令");
        List<String> dCmds = GunWar.getInstance().getConfig().getStringList("失败执行命令");
        if (victoryPlayers.size() > 0 && vCmds.size() > 0) {
            for (Player player : victoryPlayers) {
                Tools.cmd(player, vCmds);
            }
        }
        if (defeatPlayers.size() > 0 && dCmds.size() > 0) {
            for (Player player : defeatPlayers) {
                Tools.cmd(player, dCmds);
            }
        }
        this.setStatus(ROOM_STATUS_TASK_NEED_INITIALIZED);
        switch (oldStatus) {
            case IRoomStatus.ROOM_STATUS_GAME:
            case IRoomStatus.ROOM_STATUS_VICTORY:
                this.restoreWorld();
                break;
        }
    }

    public void roundStart() {
        GunWarRoomRoundStartEvent ev = new GunWarRoomRoundStartEvent(this);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        this.roundIsEnd = false;
        this.gameTime = this.getSetGameTime();
        for (Player player : this.getPlayers().keySet()) {
            this.playerRespawn(player);
        }
    }

    public void roundEnd(int victory) {
        GunWarRoomRoundEndEvent ev = new GunWarRoomRoundEndEvent(this, victory);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        this.roundIsEnd = true;
        int v = ev.getVictory();
        Tools.cleanEntity(this.getLevel(), true);
        //本回合胜利计算
        if (v == 0) {
            int red = 0, blue = 0;
            for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
                if (entry.getValue() == 1) {
                    red++;
                }else if (entry.getValue() == 2) {
                    blue++;
                }
            }
            if (red == blue) {
                this.redScore++;
                this.blueScore++;
                Tools.sendRoundVictoryTitle(this, 0);
            }else if (red > blue) {
                this.redScore++;
                Tools.sendRoundVictoryTitle(this, 1);
            }else {
                this.blueScore++;
                Tools.sendRoundVictoryTitle(this, 2);
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

    public boolean canJoin() {
        return (this.getStatus() == ROOM_STATUS_TASK_NEED_INITIALIZED || this.getStatus() == ROOM_STATUS_WAIT) &&
                this.getPlayers().size() < this.getMaxPlayers();
    }

    public void joinRoom(Player player) {
        this.joinRoom(player, false);
    }

    /**
     * 加入房间
     * @param player 玩家
     */
    public void joinRoom(Player player, boolean spectator) {
        if (this.status == 0) {
            this.initTask();
        }
        this.players.put(player, 0);
        this.playerHealth.put(player, 20F);
        SavePlayerInventory.save(GunWar.getInstance(), player);
        Tools.rePlayerState(player, true);
        player.teleport(this.getWaitSpawn());
        if (GunWar.getInstance().isHasTips()) {
            Tips.closeTipsShow(this.getLevelName(), player);
        }
        player.sendMessage(this.language.translateString("joinRoom", this.getLevelName()));
        Server.getInstance().getScheduler().scheduleDelayedTask(GunWar.getInstance(), () -> {
            if (player.getLevel() != getLevel()) {
                quitRoom(player);
            }
        }, 10);
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
        for (Player p : this.players.keySet()) {
            p.showPlayer(player);
            player.showPlayer(p);
        }
        player.sendMessage(this.language.translateString("quitRoom"));
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
        if (this.gunWar.isEnableAloneHealth()) {
            float newHealth = this.playerHealth.get(player) + health;
            if (newHealth > 20) {
                this.playerHealth.put(player, 20F);
            } else {
                this.playerHealth.put(player, newHealth);
            }
            return this.playerHealth.get(player);
        }else {
            float newHealth = Math.min(player.getHealth() + health, player.getMaxHealth());
            player.setHealth(newHealth);
            return newHealth;
        }
    }

    public synchronized float lessHealth(Player player, Entity damager, float health) {
        return this.lessHealth(player, damager, health, "");
    }

    /**
     * 减少玩家血量
     * @param player 玩家
     * @param health 血量
     */
    public synchronized float lessHealth(Player player, Entity damager, float health, String killMessage) {
        if (this.gunWar.isEnableAloneHealth()) {
            float newHealth = this.playerHealth.get(player) - health;
            if (newHealth < 1) {
                this.playerHealth.put(player, 0F);
                this.playerDeath(player, damager, killMessage);
            } else {
                this.playerHealth.put(player, newHealth);
            }
            return this.playerHealth.get(player);
        }else {
            float newHealth = player.getHealth() - health;
            if (newHealth < 1) {
                this.playerDeath(player, damager, killMessage);
            }else {
                player.setHealth(newHealth);
            }
            return newHealth;
        }
    }

    /**
     * @return 开局给予的装备
     */
    public ArrayList<String> getInitialItems() {
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
    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
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

    public void playerRespawn(Player player) {
        GunWarPlayerRespawnEvent ev = new GunWarPlayerRespawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        for (GunWeapon gunWeapon : ItemManage.getGunWeaponMap().values()) {
            gunWeapon.stopReload(player);
            gunWeapon.getMagazineMap().remove(player);
        }
        for (Entity entity : this.getLevel().getEntities()) {
            if (entity instanceof EntityPlayerCorpse) {
                if (entity.namedTag != null &&
                        entity.namedTag.getString("playerName").equals(player.getName())) {
                    entity.close();
                }
            }
        }
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        Tools.rePlayerState(player, true);
        Tools.showPlayer(this, player);
        this.getPlayerHealth().put(player, 20F);
        switch (this.getPlayers(player)) {
            case 11:
                this.getPlayers().put(player, 1);
            case 1:
                player.teleport(this.getRedSpawn());
                Tools.giveItem(this, player, 1);
                break;
            case 12:
                this.getPlayers().put(player, 2);
            case 2:
                player.teleport(this.getBlueSpawn());
                Tools.giveItem(this, player, 2);
        }
        Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, new Task() {
            @Override
            public void onRun(int i) {
                Tools.playSound(player, Sound.MOB_ENDERMEN_PORTAL);
                Tools.playSound(player, Sound.RANDOM_ORB);
            }
        }, 10);
    }

    public void playerDeath(Player player, Entity damager) {
        this.playerDeath(player, damager, "");
    }

    public void playerDeath(Player player, Entity damager, String killMessage) {
        GunWarPlayerDeathEvent ev = new GunWarPlayerDeathEvent(this, player, damager);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        GameRecord.addPlayerRecord(player, RecordType.DEATHS);
        if (player == damager) {
            this.getPlayers().keySet().forEach(p ->
                    p.sendMessage(language.translateString("suicideMessage", player.getName())));
        }else {
            if (damager instanceof Player) {
                GameRecord.addPlayerRecord((Player) damager, RecordType.KILLS);
            }
            player.sendTitle(language.translateString("titleDeathTitle"),
                    language.translateString("titleDeathSubtitle", damager.getName()),
                    10, 30, 10);
            if (killMessage == null || "".equals(killMessage.trim())) {
                this.getPlayers().keySet().forEach(p ->
                        p.sendMessage(language.translateString("killMessage", damager.getName(), player.getName())));
            }else {
                this.getPlayers().keySet().forEach(p -> p.sendMessage(killMessage));
            }
        }
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.getLevel().addSound(player, Sound.GAME_PLAYER_DIE);
        player.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, true).update();
        player.setGamemode(Player.VIEW);
        Tools.hidePlayer(this, player);
        if (this.getPlayers(player) == 1) {
            this.getPlayers().put(player, 11);
        }else if (this.getPlayers(player) == 2) {
            this.getPlayers().put(player, 12);
        }
        this.corpseSpawn(player);
    }

    public void corpseSpawn(Player player) {
        GunWarPlayerCorpseSpawnEvent ev = new GunWarPlayerCorpseSpawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        CompoundTag nbt = EntityPlayerCorpse.getDefaultNBT(player);
        Skin skin = player.getSkin();
        switch(skin.getSkinData().data.length) {
            case 8192:
            case 16384:
            case 32768:
            case 65536:
                break;
            default:
                skin = GameCore.DEFAULT_SKIN;
        }
        skin.setTrusted(true);
        nbt.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        nbt.putFloat("Scale", -1.0F);
        nbt.putString("playerName", player.getName());
        EntityPlayerCorpse entity = new EntityPlayerCorpse(player.getChunk(), nbt, this.getPlayers(player));
        entity.setSkin(skin);
        entity.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        entity.setGliding(true);
        entity.setRotation(player.getYaw(), 0);
        entity.spawnToAll();
    }

    /**
     * 还原房间地图
     */
    protected void restoreWorld() {
        if (!this.gunWar.isRestoreWorld()) {
            return;
        }
        this.setStatus(ROOM_STATUS_LEVEL_NOT_LOADED);
        if (GunWar.debug) {
            this.gunWar.getLogger().info("§a房间：" + this.levelName + " 正在还原地图...");
        }
        Server.getInstance().unloadLevel(this.level, true);
        File levelFile = new File(Server.getInstance().getFilePath() + "/worlds/" + this.levelName);
        File backup = new File(this.gunWar.getWorldBackupPath() + this.levelName);
        if (!backup.exists()) {
            this.gunWar.getLogger().error(this.language.translateString("roomLevelBackupNotExist", this.levelName));
            this.gunWar.unloadRoom(this.levelName);
        }
        CompletableFuture.runAsync(() -> {
            if (FileUtil.deleteFile(levelFile) && FileUtil.copyDir(backup, levelFile)) {
                Server.getInstance().loadLevel(this.levelName);
                this.level = Server.getInstance().getLevelByName(this.levelName);
                this.setStatus(ROOM_STATUS_TASK_NEED_INITIALIZED);
                if (GunWar.debug) {
                    this.gunWar.getLogger().info("§a房间：" + this.levelName + " 地图还原完成！");
                }
            }else {
                this.gunWar.getLogger().error(this.language.translateString("roomLevelRestoreLevelFailure", this.levelName));
                this.gunWar.unloadRoom(this.levelName);
            }
        });
    }

}
