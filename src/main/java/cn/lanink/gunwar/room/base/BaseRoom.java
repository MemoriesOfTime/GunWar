package cn.lanink.gunwar.room.base;

import cn.lanink.gamecore.GameCore;
import cn.lanink.gamecore.room.IRoom;
import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.utils.FileUtil;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gamecore.utils.PlayerDataUtils;
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
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础/通用 房间类
 * @author lt_name
 */
public abstract class BaseRoom extends RoomConfig implements IRoom, ITimeTask {

    protected final GunWar gunWar = GunWar.getInstance();
    protected final Language language = GunWar.getInstance().getLanguage();

    protected int status;

    public int waitTime;
    public int gameTime;

    protected ConcurrentHashMap<Player, Team> players = new ConcurrentHashMap<>();
    protected final HashMap<Player, Float> playerHealth = new HashMap<>(); //玩家血量
    protected final HashMap<Player, Integer> playerInvincibleTime = new HashMap<>(); //玩家无敌时间
    protected final HashMap<Player, Integer> playerIntegralMap = new HashMap<>(); //玩家积分

    public int redScore; //队伍得分
    public int blueScore;

    @Getter
    private boolean roundEnd = false; //防止重复执行回合结束方法

    /**
     * 初始化
     * @param level 游戏世界
     * @param config 配置文件
     */
    @SuppressWarnings("unchecked")
    public BaseRoom(@NotNull Level level, @NotNull Config config) throws RoomLoadException {
        super(level, config);

        File backup = new File(this.gunWar.getWorldBackupPath() + this.getLevelName());
        if (!backup.exists()) {
            this.gunWar.getLogger().info(this.language.translateString("roomLevelBackup", this.getLevelName()));
            Server.getInstance().unloadLevel(this.getLevel(), true);
            if (FileUtil.copyDir(Server.getInstance().getFilePath() + "/worlds/" + this.getLevelName(), backup)) {
                Server.getInstance().loadLevel(this.getLevelName());
                this.level = Server.getInstance().getLevelByName(this.getLevelName());
            }else {
                throw new RoomLoadException("房间地图备份失败！ / The room world backup failed!");
            }
        }
        this.level.getGameRules().setGameRule(GameRule.NATURAL_REGENERATION, false); //防止游戏难度为0时自动回血

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

        //玩家无敌时间计算
        for (Map.Entry<Player, Integer> entry : this.playerInvincibleTime.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            }
        }

        if(!this.roundEnd) {
            if (this.gameTime <= 0) {
                this.roundEnd(Team.NULL);
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
        this.roundEnd = false;
    }

    public void startGame() {
        this.setStatus(ROOM_STATUS_GAME);
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomStartEvent(this));

        this.assignTeam();

        for (Player player : this.players.keySet()) {
            this.setPlayerIntegral(player, IntegralConfig.getIntegral(IntegralConfig.IntegralType.START_BASE_INTEGRAL));
        }

        this.roundStart();

        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar,
                new TimeTask(this.gunWar, this.getTimeTask()),
                20
        );
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar,
                new ScoreBoardTask(this.gunWar, this),
                18,
                true
        );
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar,
                new ShowHealthTask(this.gunWar, this),
                5,
                true
        );
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
        for (Map.Entry<Player, Team> entry : this.getPlayers().entrySet()) {
            switch (entry.getValue()) {
                case RED:
                    redTeam.add(entry.getKey());
                    break;
                case BLUE:
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
            this.getPlayers().put(player, Team.RED);
            player.sendTitle(this.language.translateString("teamNameRed"), "", 10, 30, 10);
            player.setNameTag("§c" + player.getName());
        }
        for (Player player : blueTeam) {
            this.getPlayers().put(player, Team.BLUE);
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

        if (!this.getPlayers().isEmpty()) {
            for (Player p1 : this.players.keySet()) {
                for (Player p2 : this.players.keySet()) {
                    p1.showPlayer(p2);
                    p2.showPlayer(p1);
                }
            }

            LinkedList<Player> victoryPlayers = new LinkedList<>();
            LinkedList<Player> defeatPlayers = new LinkedList<>();
            for (Map.Entry<Player, Team> entry : this.getPlayers().entrySet()) {
                if (victory == 1) {
                    if (entry.getValue() == Team.RED || entry.getValue() == Team.RED_DEATH) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                }else if (victory == 2) {
                    if (entry.getValue() == Team.BLUE || entry.getValue() == Team.BLUE_DEATH) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                }
            }
            Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, () -> {
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
            }, 10);

            for (Player player : new HashSet<>(this.getPlayers().keySet())) {
                this.quitRoom(player);
            }
        }
        for (Player player : this.getLevel().getPlayers().values()) {
            //如果被拦截，就尝试不触发事件传送走玩家
            player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn(), null);
        }
        //因为某些原因无法正常传送走玩家，就全部踹出服务器！
        for (Player player : this.getLevel().getPlayers().values()) {
            player.kick("Teleport error!");
        }

        this.initData();
        Tools.cleanEntity(getLevel(), true);
        this.setStatus(ROOM_STATUS_TASK_NEED_INITIALIZED);
        switch (oldStatus) {
            case IRoomStatus.ROOM_STATUS_GAME:
            case IRoomStatus.ROOM_STATUS_VICTORY:
                this.restoreWorld();
                break;
            default:
                break;
        }
    }

    protected final void setRoundEnd(boolean roundEnd) throws IllegalArgumentException {
        //正常情况下只有BaseRoundModeRoom类型的房间才需要设置此参数
        if (!(this instanceof BaseRoundModeRoom)) {
            throw new IllegalArgumentException();
        }
        this.roundEnd = roundEnd;
    }

    public void roundStart() {
        GunWarRoomRoundStartEvent ev = new GunWarRoomRoundStartEvent(this);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        Tools.cleanEntity(this.getLevel(), true);
        this.roundEnd = false;
        this.gameTime = this.getSetGameTime();
        for (Player player : this.getPlayers().keySet()) {
            this.playerRespawn(player);
        }
    }

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
        this.roundEnd = true;
        Team v = ev.getVictoryTeam();
        Tools.cleanEntity(this.getLevel(), true);

        //本回合胜利计算
        if (v == Team.NULL) { //平局
            int red = 0, blue = 0;
            for (Map.Entry<Player, Team> entry : this.getPlayers().entrySet()) {
                if (entry.getValue() == Team.RED) {
                    red++;
                }else if (entry.getValue() == Team.BLUE) {
                    blue++;
                }
            }
            if (red == blue) {
                this.redScore++;
                this.blueScore++;
                Tools.sendRoundVictoryTitle(this, Team.NULL);
                Tools.giveTeamIntegral(this, Team.RED, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_LOSE_SCORE));
                Tools.giveTeamIntegral(this, Team.BLUE, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_LOSE_SCORE));
            }else if (red > blue) {
                this.redScore++;
                Tools.sendRoundVictoryTitle(this, Team.RED);
                Tools.giveTeamIntegral(this, Team.RED, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_WIN_SCORE));
                Tools.giveTeamIntegral(this, Team.BLUE, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_LOSE_SCORE));
            }else {
                this.blueScore++;
                Tools.sendRoundVictoryTitle(this, Team.BLUE);
                Tools.giveTeamIntegral(this, Team.BLUE, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_WIN_SCORE));
                Tools.giveTeamIntegral(this, Team.RED, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_LOSE_SCORE));
            }
        }else if (v == Team.RED) { //红队胜利
            this.redScore++;
            Tools.sendRoundVictoryTitle(this, Team.RED);
            Tools.giveTeamIntegral(this, Team.RED, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_WIN_SCORE));
            Tools.giveTeamIntegral(this, Team.BLUE, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_LOSE_SCORE));
        }else { //蓝队胜利
            this.blueScore++;
            Tools.sendRoundVictoryTitle(this, Team.BLUE);
            Tools.giveTeamIntegral(this, Team.BLUE, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_WIN_SCORE));
            Tools.giveTeamIntegral(this, Team.RED, IntegralConfig.getIntegral(IntegralConfig.IntegralType.ROUND_LOSE_SCORE));
        }

        //房间胜利计算
        if (this.redScore >= this.victoryScore) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 1), 20);
            return;
        }else if (this.blueScore >= this.victoryScore) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.gunWar, new VictoryTask(this.gunWar, this, 2), 20);
            return;
        }

        //延迟3秒开始下一回合
        Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, this::roundStart, 60);
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
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomPlayerJoinEvent(this, player));

        if (this.status == 0) {
            this.initTask();
        }
        this.players.put(player, Team.NULL);
        this.playerHealth.put(player, 20F);
        this.setPlayerIntegral(player, Integer.MAX_VALUE);

        File file = new File(GunWar.getInstance().getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
        PlayerDataUtils.PlayerData playerData = PlayerDataUtils.create(player);
        playerData.saveAll();
        playerData.saveToFile(file);

        player.getInventory().clearAll();
        player.getOffhandInventory().clearAll();
        player.getEnderChestInventory().clearAll();

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
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomPlayerQuitEvent(this, player));

        this.players.remove(player);
        if (GunWar.getInstance().isHasTips()) {
            Tips.removeTipsConfig(this.getLevelName(), player);
        }
        GunWar.getInstance().getScoreboard().closeScoreboard(player);
        //player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);

        File file = new File(GunWar.getInstance().getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
        if (file.exists()) {
            PlayerDataUtils.PlayerData playerData = PlayerDataUtils.create(player, file);
            if (file.delete()) {
                playerData.restoreAll();
            }
        }

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
    public ConcurrentHashMap<Player, Team> getPlayers() {
        return this.players;
    }

    /**
     * 获取玩家队伍
     * @param player 玩家
     * @return 玩家队伍
     */
    public Team getPlayerTeam(Player player) {
        Team team = this.players.getOrDefault(player, Team.NULL);
        if (team == Team.RED_DEATH) {
            team = Team.RED;
        }else if (team == Team.BLUE_DEATH) {
            team = Team.BLUE;
        }
        return team;
    }

    /**
     * 获取玩家准确队伍
     * @param player 玩家
     * @return 玩家准确队伍
     */
    public Team getPlayerTeamAccurate(Player player) {
        return this.players.getOrDefault(player, Team.NULL);
    }

    /**
     * 根据队伍获取玩家列表
     * @return 玩家列表
     */
    public Set<Player> getPlayers(Team team) {
        HashSet<Player> set = new HashSet<>();
        for (Map.Entry<Player, Team> entry : this.getPlayers().entrySet()) {
            if (team == Team.NULL && entry.getValue() == Team.NULL) {
                set.add(entry.getKey());
            }else if ((team == Team.RED || team == Team.RED_DEATH) && (entry.getValue() == Team.RED || entry.getValue() == Team.RED_DEATH)) {
                set.add(entry.getKey());
            }else if ((team == Team.BLUE || team == Team.BLUE_DEATH) && (entry.getValue() == Team.BLUE || entry.getValue() == Team.BLUE_DEATH)) {
                set.add(entry.getKey());
            }
        }
        return set;
    }

    public Set<Player> getPlayersAccurate(Team team) {
        HashSet<Player> set = new HashSet<>();
        this.getPlayers().entrySet().stream()
                .filter(
                        entry -> (team == Team.NULL && entry.getValue() == Team.NULL) ||
                                (team == Team.RED && entry.getValue() == Team.RED) ||
                                (team == Team.RED_DEATH && entry.getValue() == Team.RED_DEATH) ||
                                (team == Team.BLUE && entry.getValue() == Team.BLUE) ||
                                (team == Team.BLUE_DEATH && entry.getValue() == Team.BLUE_DEATH)
                ).forEach(entry -> set.add(entry.getKey()));
        return set;
    }

    /**
     * 获取玩家血量Map
     * @return 玩家血量Map
     */
    public HashMap<Player, Float> getPlayerHealth() {
        return this.playerHealth;
    }

    public float getPlayerHealth(@NotNull Player player) {
        return this.playerHealth.getOrDefault(player, 0F);
    }

    public int getPlayerInvincibleTime(@NotNull Player player) {
        return this.playerInvincibleTime.getOrDefault(player, 0);
    }

    public int getPlayerIntegral(@NotNull Player player) {
        return this.playerIntegralMap.getOrDefault(player, 0);
    }

    public void addPlayerIntegral(@NotNull Player player, int integral) {
        if (this.getStatus() == ROOM_STATUS_WAIT) { //等待状态不增加积分
            return;
        }
        this.playerIntegralMap.put(player, this.playerIntegralMap.getOrDefault(player, 0) + integral);
    }

    public void setPlayerIntegral(@NotNull Player player, int integral) {
        if (this.getStatus() == ROOM_STATUS_WAIT) { //等待状态不扣除积分
            if (integral < this.getPlayerIntegral(player)) {
                return;
            }
        }
        this.playerIntegralMap.put(player, integral);
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

        this.playerInvincibleTime.put(player, 3); //重生三秒无敌

        //重置枪械
        for (GunWeapon gunWeapon : ItemManage.getGunWeaponMap().values()) {
            gunWeapon.stopReload(player);
            gunWeapon.getMagazineMap().remove(player);
        }

        //清理尸体
        for (Entity entity : this.getLevel().getEntities()) {
            if (entity instanceof EntityPlayerCorpse) {
                if (entity.namedTag != null &&
                        entity.namedTag.getString("playerName").equals(player.getName())) {
                    entity.close();
                }
            }
        }

        if (this.isRoundEndCleanItem() ||
                this.getPlayerTeamAccurate(player) == Team.RED_DEATH ||
                this.getPlayerTeamAccurate(player) == Team.BLUE_DEATH) {
            player.getInventory().clearAll();
            player.getUIInventory().clearAll();
        }else {
            //清除一些必须清除的特殊物品
            PlayerInventory inventory = player.getInventory();
            inventory.remove(Tools.getItem(10));
            inventory.remove(Tools.getItem(11));
            inventory.remove(Tools.getItem(12));
            inventory.remove(Tools.getItem(13));
            inventory.remove(Tools.getItem(201));
        }

        Tools.rePlayerState(player, true);
        Tools.showPlayer(this, player);
        this.getPlayerHealth().put(player, 20F);
        player.getInventory().addItem(Tools.getItem(13)); //打开商店物品
        switch (this.getPlayerTeamAccurate(player)) {
            case RED_DEATH:
                this.getPlayers().put(player, Team.RED);
            case RED:
                player.teleport(this.getRedSpawn());
                Tools.giveItem(this, player, Team.RED);
                break;
            case BLUE_DEATH:
                this.getPlayers().put(player, Team.BLUE);
            case BLUE:
                player.teleport(this.getBlueSpawn());
                Tools.giveItem(this, player, Team.BLUE);
        }
        //复活音效
        Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, () -> {
            Tools.playSound(player, Sound.MOB_ENDERMEN_PORTAL);
            Tools.playSound(player, Sound.RANDOM_ORB);
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
                Player damagerPlayer = (Player) damager;
                if (this.getPlayerTeam(damagerPlayer) != this.getPlayerTeam(player)) {
                    GameRecord.addPlayerRecord(damagerPlayer, RecordType.KILLS);
                    this.addPlayerIntegral(damagerPlayer, IntegralConfig.getIntegral(IntegralConfig.IntegralType.KILL_SCORE));
                }else {
                    this.addPlayerIntegral(damagerPlayer, IntegralConfig.getIntegral(IntegralConfig.IntegralType.KILL_TEAM_SCORE));
                }
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
        if (this.getPlayerTeamAccurate(player) == Team.RED) {
            this.getPlayers().put(player, Team.RED_DEATH);
        }else if (this.getPlayerTeamAccurate(player) == Team.BLUE) {
            this.getPlayers().put(player, Team.BLUE_DEATH);
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
        EntityPlayerCorpse entity = new EntityPlayerCorpse(player.getChunk(), nbt, this.getPlayerTeamAccurate(player));
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
            this.gunWar.getLogger().info("§a房间：" + this.getLevelName() + " 正在还原地图...");
        }
        Server.getInstance().unloadLevel(this.level, true);
        File levelFile = new File(Server.getInstance().getFilePath() + "/worlds/" + this.getLevelName());
        File backup = new File(this.gunWar.getWorldBackupPath() + this.getLevelName());
        if (!backup.exists()) {
            this.gunWar.getLogger().error(this.language.translateString("roomLevelBackupNotExist", this.getLevelName()));
            this.gunWar.unloadRoom(this.getLevelName());
        }
        Server.getInstance().getScheduler().scheduleAsyncTask(this.gunWar, new AsyncTask() {
            @Override
            public void onRun() {
                if (FileUtil.deleteFile(levelFile) && FileUtil.copyDir(backup, levelFile)) {
                    Server.getInstance().loadLevel(getLevelName());
                    level = Server.getInstance().getLevelByName(getLevelName());
                    setStatus(ROOM_STATUS_TASK_NEED_INITIALIZED);
                    if (GunWar.debug) {
                        gunWar.getLogger().info("§a房间：" + getLevelName() + " 地图还原完成！");
                    }
                }else {
                    gunWar.getLogger().error(language.translateString("roomLevelRestoreLevelFailure", getLevelName()));
                    gunWar.unloadRoom(getLevelName());
                }
            }
        });
    }

}
