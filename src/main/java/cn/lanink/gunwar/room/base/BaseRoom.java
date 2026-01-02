package cn.lanink.gunwar.room.base;

import cn.lanink.gamecore.GameCore;
import cn.lanink.gamecore.room.GameRoom;
import cn.lanink.gamecore.room.IRoom;
import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.utils.FileUtils;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gamecore.utils.PlayerDataUtils;
import cn.lanink.gamecore.utils.Tips;
import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.event.*;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.room.freeforall.FreeForAllModeRoom;
import cn.lanink.gunwar.tasks.VictoryTask;
import cn.lanink.gunwar.tasks.WaitTask;
import cn.lanink.gunwar.tasks.game.ScoreBoardTask;
import cn.lanink.gunwar.tasks.game.ShowHealthTask;
import cn.lanink.gunwar.tasks.game.TimeTask;
import cn.lanink.gunwar.utils.Tools;
import cn.lanink.gunwar.utils.gamerecord.GameRecord;
import cn.lanink.gunwar.utils.gamerecord.RecordType;
import cn.lanink.gunwar.utils.nsgb.GunWarDataGamePlayerPojoUtils;
import cn.lanink.teamsystem.TeamSystem;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
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
public abstract class BaseRoom extends RoomConfig implements GameRoom, IRoom, ITimeTask {

    protected final GunWar gunWar = GunWar.getInstance();
    protected final Language language = GunWar.getInstance().getLanguage();

    protected int status;

    public int waitTime;
    public int gameTime;

    protected final ConcurrentHashMap<Player, PlayerGameData> players = new ConcurrentHashMap<>();

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
            if (FileUtils.copyDir(Server.getInstance().getFilePath() + "/worlds/" + this.getLevelName(), backup)) {
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
     * @return 可以攻击队友
     */
    public boolean canDamageTeammates() {
        return false;
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
        if (this.getPlayerDataMap().isEmpty()) {
            this.endGame();
            return;
        }

        //启用独立血量时锁定玩家血量 （为了让各种食物道具发挥效果，这里不能满血）
        if (this.gunWar.isEnableAloneHealth()) {
            for (Player player : this.players.keySet()) {
                player.setHealth(player.getMaxHealth() - 1);
            }
        }

        //玩家无敌时间计算
        for (Map.Entry<Player, PlayerGameData> entry : this.players.entrySet()) {
            if (entry.getValue().getInvincibleTime() > 0) {
                entry.getValue().setInvincibleTime(entry.getValue().getInvincibleTime() - 1);
            }
            if (!this.canUseShop(entry.getKey())) {
                for (Item item : entry.getKey().getInventory().getContents().values()) {
                    CompoundTag namedTag = item.getNamedTag();
                    if (namedTag != null && namedTag.getBoolean(ItemManage.IS_GUN_WAR_ITEM_TAG) && namedTag.getInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG) == 13) {
                        entry.getKey().getInventory().remove(item);
                    }
                }
            }
        }

        if(!this.roundEnd) {
            this.checkGameTime();
            this.checkTeamPlayerCount();
        }
    }

    /**
     * 检查游戏时间
     */
    protected void checkGameTime() {
        if (this.gameTime <= 0) {
            this.roundEnd(Team.NULL);
            this.gameTime = this.getSetGameTime();
            return;
        }
        this.gameTime--;
    }

    /**
     * 检查队伍人数
     */
    protected void checkTeamPlayerCount() {
        int red = 0;
        int blue = 0;
        for (PlayerGameData gameData : this.getPlayerDataMap().values()) {
            switch (gameData.getTeam()) {
                case RED:
                case RED_DEATH:
                    red++;
                    break;
                case BLUE:
                case BLUE_DEATH:
                    blue++;
                    break;
                default:
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
     * 初始化Task （等待状态）
     */
    protected void initTask() {
        this.setStatus(ROOM_STATUS_WAIT);
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
        this.roundEnd = false;
    }

    public void startGame() {
        this.setStatus(ROOM_STATUS_GAME);
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomStartEvent(this));

        this.assignTeam();

        for (Player player : this.players.keySet()) {
            this.setPlayerIntegral(player, IntegralConfig.getIntegral(IntegralConfig.IntegralType.START_BASE_INTEGRAL));

            //清除玩家在房间等待阶段获取到的物品
            if (!this.isRoundEndCleanItem()) {
                player.getInventory().clearAll();
                player.getUIInventory().clearAll();
            }
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
        for (Map.Entry<Player, PlayerGameData> entry : this.getPlayerDataMap().entrySet()) {
            switch (entry.getValue().getTeam()) {
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
            if (!noTeam.isEmpty()) {
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
            this.getPlayerData(player).setTeam(Team.RED);
            player.sendTitle(this.language.translateString("teamNameRed"), "", 10, 30, 10);
            player.setNameTag("§c" + player.getName());
        }
        for (Player player : blueTeam) {
            this.getPlayerData(player).setTeam(Team.BLUE);
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

        if (!this.getPlayerDataMap().isEmpty()) {
            for (Player p1 : this.players.keySet()) {
                for (Player p2 : this.players.keySet()) {
                    p1.showPlayer(p2);
                    p2.showPlayer(p1);
                }
            }

            this.victoryCommand(victory);

            for (Player player : new HashSet<>(this.getPlayerDataMap().keySet())) {
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
        for (Entity entity : new ArrayList<>(Arrays.asList(this.getLevel().getEntities()))) {
            if (entity instanceof Player) { //nk bug?
                entity.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                entity.setPosition(Server.getInstance().getDefaultLevel().getSafeSpawn()); //防止传送失败
            }else if (entity != null && !entity.isClosed()) {
                entity.close();
            }
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

    /**
     * 游戏结束 结算命令
     *
     * @param victory 胜利队伍
     */
    protected void victoryCommand(int victory) {
        if (victory != 1 && victory != 2) {
            return;
        }
        LinkedHashMap<Player, PlayerGameData> victoryPlayers = new LinkedHashMap<>();
        LinkedHashMap<Player, PlayerGameData> defeatPlayers = new LinkedHashMap<>();
        for (Map.Entry<Player, PlayerGameData> entry : this.getPlayerDataMap().entrySet()) {
            if (victory == 1) {
                if (entry.getValue().getTeam() == Team.RED || entry.getValue().getTeam() == Team.RED_DEATH) {
                    victoryPlayers.put(entry.getKey(), entry.getValue());
                }else {
                    defeatPlayers.put(entry.getKey(), entry.getValue());
                }
            }else {
                if (entry.getValue().getTeam() == Team.BLUE || entry.getValue().getTeam() == Team.BLUE_DEATH) {
                    victoryPlayers.put(entry.getKey(), entry.getValue());
                }else {
                    defeatPlayers.put(entry.getKey(), entry.getValue());
                }
            }
        }
        Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, () -> {
            List<String> vCmds = GunWar.getInstance().getConfig().getStringList("胜利执行命令");
            List<String> dCmds = GunWar.getInstance().getConfig().getStringList("失败执行命令");
            if (!victoryPlayers.isEmpty()) {
                for (Player player : victoryPlayers.keySet()) {
                    Tools.executeCommands(player, vCmds);
                    if (this.gunWar.isHasNsGB()) {
                        GunWarDataGamePlayerPojoUtils.onWin(victoryPlayers.get(player));
                    }
                }
            }
            if (!defeatPlayers.isEmpty()) {
                for (Player player : defeatPlayers.keySet()) {
                    Tools.executeCommands(player, dCmds);
                    if (this.gunWar.isHasNsGB()) {
                        GunWarDataGamePlayerPojoUtils.onLose(defeatPlayers.get(player));
                    }
                }
            }
        }, 10);
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
        for (Player player : this.getPlayerDataMap().keySet()) {
            this.playerRespawn(player);
        }

        //名片显示给队友
        for (Player p1 : this.getPlayerDataMap().keySet()) {
            for (Player p2 : this.getPlayerDataMap().keySet()) {
                Tools.showNameTag(p1, p2, this.getPlayerTeam(p1) == this.getPlayerTeam(p2));
            }
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
            for (Map.Entry<Player, PlayerGameData> entry : this.getPlayerDataMap().entrySet()) {
                if (entry.getValue().getTeam() == Team.RED) {
                    red++;
                }else if (entry.getValue().getTeam() == Team.BLUE) {
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
        return this.canJoin(null);
    }

    public boolean canJoin(Player player) {
        if (player != null && this.gunWar.isHasTeamSystem()) {
            cn.lanink.teamsystem.team.Team team = TeamSystem.Companion.getTeamByPlayer(player);
            if (team != null && team.getPlayers().size() + this.getPlayerDataMap().size() > this.getMaxPlayers()) {
                return false;
            }
        }

        return (this.getStatus() == ROOM_STATUS_TASK_NEED_INITIALIZED || this.getStatus() == ROOM_STATUS_WAIT) &&
                this.getPlayerDataMap().size() < this.getMaxPlayers();
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
        this.players.put(player, new PlayerGameData(player));

        this.setPlayerIntegral(player, Integer.MAX_VALUE);

        File file = new File(GunWar.getInstance().getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
        PlayerDataUtils.PlayerData playerData = PlayerDataUtils.create(player);
        playerData.saveAll();
        playerData.saveToFile(file);

        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.getEnderChestInventory().clearAll();

        Tools.rePlayerState(player, true);
        player.teleport(this.getWaitSpawn());
        if (GunWar.getInstance().isHasTips()) {
            Tips.closeTipsShow(this.getLevelName(), player);
        }

        //防止死循环，队长加入成功后再添加成员
        if (this.gunWar.isHasTeamSystem()) {
            cn.lanink.teamsystem.team.Team team = TeamSystem.Companion.getTeamByPlayer(player);
            if (team != null && team.isTeamLeader(player)) {
                team.getPlayers().forEach(playerName -> {
                    Player p = Server.getInstance().getPlayer(playerName);
                    if (p != null && !this.getPlayerDataMap().containsKey(p)) {
                        this.joinRoom(p);
                    }
                });
            }
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
        Tools.rePlayerState(player, false);

        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.getEnderChestInventory().clearAll();

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

        if (this.gunWar.isHasTeamSystem()) {
            cn.lanink.teamsystem.team.Team team = TeamSystem.Companion.getTeamByPlayer(player);
            if (team != null && team.isTeamLeader(player)) {
                team.getPlayers().forEach(playerName -> {
                    Player p = Server.getInstance().getPlayer(playerName);
                    if (p != null && this.getPlayerDataMap().containsKey(p)) {
                        this.quitRoom(p);
                    }
                });
            }
        }

        player.sendMessage(this.language.translateString("quitRoom"));

        if (this.gunWar.getConfig().exists("QuitRoom.cmd")) {
            Tools.executeCommands(player, this.gunWar.getConfig().getStringList("QuitRoom.cmd"));
        }
    }

    public boolean canUseShop(Player player) {
        switch (this.getSupplyType()) {
            case ALL_ROUND:
                return true;
            case ONLY_ROUND_START:
                int startTime = this.getSetGameTime() - this.gameTime;
                if (startTime > this.getSupplyEnableTime() + 1) {
                    return false;
                }
                return true;
            case CLOSE:
            default:
                return false;
        }
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
    public ConcurrentHashMap<Player, PlayerGameData> getPlayerDataMap() {
        return this.players;
    }

    public PlayerGameData getPlayerData(@NotNull Player player) {
        return this.getPlayerDataMap().getOrDefault(player, new PlayerGameData(player));
    }

    /**
     * 获取玩家队伍
     * @param player 玩家
     * @return 玩家队伍
     */
    public Team getPlayerTeam(Player player) {
        Team team = this.getPlayerTeamAccurate(player);
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
        return this.getPlayerData(player).getTeam();
    }

    @Deprecated
    public Set<Player> getPlayerDataMap(Team team) {
        return this.getPlayers(team);
    }

    /**
     * 根据队伍获取玩家列表
     * @return 玩家列表
     */
    public Set<Player> getPlayers(Team team) {
        HashSet<Player> set = new HashSet<>();
        for (Map.Entry<Player, PlayerGameData> entry : this.getPlayerDataMap().entrySet()) {
            if (team == Team.NULL && entry.getValue().getTeam() == Team.NULL) {
                set.add(entry.getKey());
            }else if ((team == Team.RED || team == Team.RED_DEATH) && (entry.getValue().getTeam() == Team.RED || entry.getValue().getTeam() == Team.RED_DEATH)) {
                set.add(entry.getKey());
            }else if ((team == Team.BLUE || team == Team.BLUE_DEATH) && (entry.getValue().getTeam() == Team.BLUE || entry.getValue().getTeam() == Team.BLUE_DEATH)) {
                set.add(entry.getKey());
            }
        }
        return set;
    }

    public Set<Player> getPlayersAccurate(Team team) {
        HashSet<Player> set = new HashSet<>();
        this.getPlayerDataMap().entrySet().stream()
                .filter(
                        entry -> (team == Team.NULL && entry.getValue().getTeam() == Team.NULL) ||
                                (team == Team.RED && entry.getValue().getTeam() == Team.RED) ||
                                (team == Team.RED_DEATH && entry.getValue().getTeam() == Team.RED_DEATH) ||
                                (team == Team.BLUE && entry.getValue().getTeam() == Team.BLUE) ||
                                (team == Team.BLUE_DEATH && entry.getValue().getTeam() == Team.BLUE_DEATH)
                ).forEach(entry -> set.add(entry.getKey()));
        return set;
    }

    public float getPlayerHealth(@NotNull Player player) {
        if (this.players.containsKey(player)) {
            return this.players.get(player).getHealth();
        }
        return 0;
    }

    public int getPlayerInvincibleTime(@NotNull Player player) {
        if (this.players.containsKey(player)) {
            return this.players.get(player).getInvincibleTime();
        }
        return 0;
    }

    public int getPlayerIntegral(@NotNull Player player) {
        if (this.players.containsKey(player)) {
            return this.players.get(player).getIntegral();
        }
        return 0;
    }

    public void addPlayerIntegral(@NotNull Player player, int integral) {
        if (this.getStatus() == ROOM_STATUS_WAIT) { //等待状态不增加积分
            return;
        }
        PlayerGameData playerGameData = this.getPlayerData(player);
        playerGameData.setIntegral(playerGameData.getIntegral() + integral);
    }

    public void setPlayerIntegral(@NotNull Player player, int integral) {
        if (this.getStatus() == ROOM_STATUS_WAIT && integral < this.getPlayerIntegral(player)) { //等待状态不扣除积分
            return;
        }
        this.getPlayerData(player).setIntegral(integral);
    }

    /**
     * 增加玩家血量
     * @param player 玩家
     * @param health 血量
     * @return 增加后的血量
     */
    public synchronized float addHealth(Player player, float health) {
        PlayerGameData playerGameData = this.getPlayerData(player);
        playerGameData.setHealth(Math.min(this.gunWar.isEnableAloneHealth() ? 20F : player.getMaxHealth(), playerGameData.getHealth() + health));
        return playerGameData.getHealth();
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
        PlayerGameData playerGameData = this.getPlayerData(player);
        float newHealth = playerGameData.getHealth() - health;
        if (newHealth < 1) {
            health = playerGameData.getHealth();
            this.playerDeath(player, damager, killMessage);
        } else {
            playerGameData.setHealth(newHealth);
        }
        if (damager instanceof Player) {
            Player damagePlayer = (Player) damager;
            playerGameData.setLastDamagePlayer(damagePlayer);
            playerGameData.getDamager().put(damagePlayer, playerGameData.getDamager().getOrDefault(damagePlayer, 0F) + health);
        }
        return newHealth;
    }

    public void playerRespawn(Player player) {
        GunWarPlayerRespawnEvent ev = new GunWarPlayerRespawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }

        PlayerGameData playerGameData = this.getPlayerData(player);

        playerGameData.setInvincibleTime(3); //重生三秒无敌
        playerGameData.setSpawnTime(this.gameTime);

        //重置枪械物品状态
        for (GunWeapon gunWeapon : ItemManage.getGunWeaponMap().values()) {
            gunWeapon.resetStatus(player);
        }

        //清理尸体
        for (Entity entity : this.getLevel().getEntities()) {
            if (entity instanceof EntityPlayerCorpse) {
                if (entity.namedTag == null ||
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
            Tools.removeGunWarItem(inventory, Tools.getItem(10));
            Tools.removeGunWarItem(inventory, Tools.getItem(11));
            Tools.removeGunWarItem(inventory, Tools.getItem(12));
            Tools.removeGunWarItem(inventory, Tools.getItem(13));
            Tools.removeGunWarItem(inventory, Tools.getItem(201));
        }

        Tools.rePlayerState(player, true);
        Tools.showPlayer(this, player);
        playerGameData.setHealth(this.gunWar.isEnableAloneHealth() ? 20F : player.getMaxHealth());

        if (this.getSupplyType() != SupplyType.CLOSE) {
            player.getInventory().addItem(Tools.getItem(13)); //打开商店物品
        }

        //队伍设置 位置传送 给予初始物品
        switch (this.getPlayerTeamAccurate(player)) {
            case RED_DEATH:
                playerGameData.setTeam(Team.RED);
            case RED:
                player.teleport(this.getRedSpawn());
                Tools.giveItem(this, player, Team.RED, this.isRoundEndCleanItem());
                break;
            case BLUE_DEATH:
                playerGameData.setTeam(Team.BLUE);
            case BLUE:
                player.teleport(this.getBlueSpawn());
                Tools.giveItem(this, player, Team.BLUE, this.isRoundEndCleanItem());
                break;
        }

        //名片显示给队友
        this.getPlayers(this.getPlayerTeamAccurate(player)).forEach(p -> Tools.showNameTag(player, p, true));

        //复活音效
        Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, () -> {
            Tools.playSound(player, Sound.MOB_ENDERMEN_PORTAL);
            Tools.playSound(player, Sound.RANDOM_ORB);
        }, 10);
    }

    public final void playerDeath(Player player, Entity damager) {
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
            this.getPlayerDataMap().keySet().forEach(p ->
                    p.sendMessage(language.translateString("suicideMessage", player.getName())));
        }else {
            if (damager instanceof Player) {
                Player damagerPlayer = (Player) damager;
                if (this.getPlayerTeam(damagerPlayer) != this.getPlayerTeam(player) || this instanceof FreeForAllModeRoom) {
                    PlayerGameData playerGameData = this.getPlayerData(damagerPlayer);
                    playerGameData.setKillCount(playerGameData.getKillCount() + 1);
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
                this.getPlayerDataMap().keySet().forEach(p ->
                        p.sendMessage(language.translateString("killMessage", damager.getName(), player.getName())));
            }else {
                this.getPlayerDataMap().keySet().forEach(p -> p.sendMessage(killMessage));
            }
        }

        PlayerGameData playerGameData = this.getPlayerData(player);
        for (Player p : playerGameData.getDamager().keySet()) {
            if (p == damager) {
                this.getPlayerData(p).addKillCount();
            } else {
                this.getPlayerData(p).addAssistsKillCount();
            }
        }
        playerGameData.getDamager().clear();

        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.getLevel().addSound(player, Sound.GAME_PLAYER_DIE);
        player.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, true).update();
        player.setGamemode(Player.SPECTATOR);
        Tools.hidePlayer(this, player);
        if (this.getPlayerTeamAccurate(player) == Team.RED) {
            playerGameData.setTeam(Team.RED_DEATH);
        }else if (this.getPlayerTeamAccurate(player) == Team.BLUE) {
            playerGameData.setTeam(Team.BLUE_DEATH);
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
            this.gunWar.getGameRoomManager().unloadGameRoom(this.getLevelName());
        }
        if (GunWar.getInstance().isDisabled()) {
            this.privateRestoreWorld(levelFile, backup);
        } else {
            Server.getInstance().getScheduler().scheduleAsyncTask(this.gunWar, new AsyncTask() {
                @Override
                public void onRun() {
                    privateRestoreWorld(levelFile, backup);
                }
            });
        }
    }

    private void privateRestoreWorld(File levelFile, File backup) {
        if (FileUtils.deleteFile(levelFile) && FileUtils.copyDir(backup, levelFile)) {
            Server.getInstance().loadLevel(getLevelName());
            level = Server.getInstance().getLevelByName(getLevelName());
            setStatus(ROOM_STATUS_TASK_NEED_INITIALIZED);
            if (GunWar.debug) {
                gunWar.getLogger().info("§a房间：" + getLevelName() + " 地图还原完成！");
            }
        }else {
            gunWar.getLogger().error(language.translateString("roomLevelRestoreLevelFailure", getLevelName()));
            gunWar.getGameRoomManager().unloadGameRoom(getLevelName());
        }
    }

}
