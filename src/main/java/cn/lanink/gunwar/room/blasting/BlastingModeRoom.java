package cn.lanink.gunwar.room.blasting;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityGunWarBomb;
import cn.lanink.gunwar.entity.EntityGunWarBombBlock;
import cn.lanink.gunwar.event.GunWarSwapTeamEvent;
import cn.lanink.gunwar.room.base.BaseRoundModeRoom;
import cn.lanink.gunwar.room.base.PlayerGameData;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.DummyBossBar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lt_name
 */
public class BlastingModeRoom extends BaseRoundModeRoom {

    protected final String blastingPointA;
    protected final String blastingPointB;
    protected final int bombExplosionTime;

    protected EntityGunWarBomb entityGunWarBomb;
    private EntityGunWarBombBlock entityGunWarBombBlock;
    protected final ConcurrentHashMap<Player, DummyBossBar> bossBarMap = new ConcurrentHashMap<>();
    protected boolean bombWasFound = false;
    public Player demolitionBombPlayer;
    protected boolean changeTeam = false;

    /**
     * 初始化
     *
     * @param level  游戏世界
     * @param config 配置文件
     */
    public BlastingModeRoom(Level level, Config config) throws RoomLoadException {
        super(level, config);
        this.blastingPointA = config.getString("blastingPointA");
        this.blastingPointB = config.getString("blastingPointB");
        if ("".equals(this.blastingPointA.trim()) || "".equals(this.blastingPointB.trim())) {
            throw new RoomLoadException("§c房间：" + level.getFolderName() + " 配置不完整，加载失败！");
        }
        this.bombExplosionTime = config.getInt("bombExplosionTime", 50);
    }

    @Override
    public void saveConfig() {
        super.saveConfig();

        this.config.set("blastingPointA", this.blastingPointA);
        this.config.set("blastingPointB", this.blastingPointB);
        this.config.set("bombExplosionTime", this.bombExplosionTime);

        this.config.save();
    }

    @Override
    public List<String> getListeners() {
        List<String> list = super.getListeners();
        list.add("BlastingGameListener");
        return list;
    }

    @Override
    public void timeTask() {
        super.timeTask();

        if (!this.isRoundEnd()) {
            //Boss血条显示炸弹爆炸倒计时
            if (this.entityGunWarBomb != null && !this.entityGunWarBomb.isClosed() &&
                    this.entityGunWarBomb.getExplosionTime() > 0) {
                double discoveryDistance = this.getBlastingPointRadius() * 0.8 + 5;
                if (!this.bombWasFound) {
                    for (Map.Entry<Player, PlayerGameData> entry : this.getPlayerDataMap().entrySet()) {
                        if (entry.getValue().getTeam() == Team.BLUE) {
                            if (entry.getKey().distance(this.getBlastingPointA()) <= discoveryDistance ||
                                    entry.getKey().distance(this.getBlastingPointB()) <= discoveryDistance) {
                                this.bombWasFound = true;
                                String s;
                                if (this.entityGunWarBomb.distance(this.getBlastingPointA()) <= this.getBlastingPointRadius()) {
                                    s = this.language.translateString("game_blasting_bombFound", "§cA");
                                } else {
                                    s = this.language.translateString("game_blasting_bombFound", "§9B");
                                }
                                Tools.sendTitle(this, Team.BLUE, "", s);
                            }
                        }
                    }
                }
                String s = "";
                if (this.bombWasFound) {
                    if (this.entityGunWarBomb.distance(this.getBlastingPointA()) <= this.getBlastingPointRadius()) {
                        s += this.language.translateString("game_blasting_bombFound", "§cA");
                    }else {
                        s += this.language.translateString("game_blasting_bombFound", "§9B");
                    }
                }
                s += this.language.translateString("game_blasting_countdownToBombExplosion",
                        this.entityGunWarBomb.getExplosionTime());
                for (Map.Entry<Player, PlayerGameData> entry : this.getPlayerDataMap().entrySet()) {
                    Tools.createBossBar(entry.getKey(), this.bossBarMap);
                    DummyBossBar bossBar = this.bossBarMap.get(entry.getKey());
                    bossBar.setText(s);
                    bossBar.setLength(this.entityGunWarBomb.getExplosionTime() / 50F * 100);
                }
            } else if (!this.bossBarMap.isEmpty()) {
                for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
                    entry.getKey().removeBossBar(entry.getValue().getBossBarId());
                }
                this.bossBarMap.clear();
            }

            //检查玩家是否还持有炸弹
            Item item = Tools.getItem(201);
            for (Player player : this.getPlayersAccurate(Team.RED)) {
                boolean hasBomb = false;
                for (Item i : player.getInventory().getContents().values()) {
                    if (item.equals(i)) {
                        hasBomb = true;
                        player.setNameTag("§c" + player.getName() + " §r" + this.language.translateString("game_blasting_nameTag_carryBomb"));
                    }
                }

                if (!hasBomb) {
                    player.setNameTag("§c" + player.getName());
                }
            }
        }

        //显示爆破点
        Server.getInstance().getScheduler().scheduleAsyncTask(this.gunWar, new AsyncTask() {
            @Override
            public void onRun() {
                LinkedList<Vector3> list = Tools.getRoundEdgePoint(getBlastingPointA(), getBlastingPointRadius());
                list.addAll(Tools.getRoundEdgePoint(getBlastingPointB(), getBlastingPointRadius()));
                for (Vector3 vector3 : list) {
                    vector3.y += 0.1;
                    if (getLevel().getBlock(vector3).getId() == BlockID.AIR) {
                        for (int y = vector3.getFloorY(); y > y-5; y--) {
                            if (getLevel().getBlock(new Vector3(vector3.x, y, vector3.z)).getId() != BlockID.AIR) {
                                vector3.y = y + 1.1;
                                break;
                            }
                        }
                    }else {
                        for (int y = vector3.getFloorY(); y < y+5; y++) {
                            if (getLevel().getBlock(new Vector3(vector3.x, y, vector3.z)).getId() == BlockID.AIR) {
                                vector3.y = y + 0.1;
                                break;
                            }
                        }
                    }
                    getLevel().addParticleEffect(vector3, ParticleEffect.REDSTONE_TORCH_DUST);
                }
            }
        });
    }

    @Override
    protected void checkGameTime() {
        if (this.gameTime <= 0 && (this.entityGunWarBomb == null || this.entityGunWarBomb.isClosed())) {
            this.roundEnd(Team.BLUE);
            this.gameTime = this.getSetGameTime();
            return;
        }
        this.gameTime--;
        int red = 0;
        int blue = 0;
        for (PlayerGameData gameData : this.getPlayerDataMap().values()) {
            if (gameData.getTeam() == Team.RED) {
                red++;
            } else if (gameData.getTeam() == Team.BLUE) {
                blue++;
            }
        }
        if (red == 0) {
            if (this.entityGunWarBomb == null) {
                this.roundEnd(Team.BLUE);
                this.gameTime = this.getSetGameTime();
            }
        } else if (blue == 0) {
            this.roundEnd(Team.RED);
            this.gameTime = this.getSetGameTime();
        }
    }

    @Override
    protected void initData() {
        super.initData();
        this.entityGunWarBomb = null;
        this.entityGunWarBombBlock = null;
        if (this.bossBarMap != null && !this.bossBarMap.isEmpty()) {
            for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
                entry.getKey().removeBossBar(entry.getValue().getBossBarId());
            }
            this.bossBarMap.clear();
        }
        this.bombWasFound = false;
        this.demolitionBombPlayer = null;
        this.changeTeam = false;
    }

    @Override
    public void roundStart() {
        int delay = 0;
        //交换队伍
        if (!this.changeTeam && (this.redScore + this.blueScore) >= this.victoryScore * 0.6) {
            LinkedList<Player> oldRedTeam = new LinkedList<>();
            LinkedList<Player> oldBlueTeam = new LinkedList<>();
            for (Map.Entry<Player, PlayerGameData> entry : this.getPlayerDataMap().entrySet()) {
                switch (entry.getValue().getTeam()) {
                    case RED:
                    case RED_DEATH:
                        oldRedTeam.add(entry.getKey());
                        break;
                    case BLUE:
                    case BLUE_DEATH:
                        oldBlueTeam.add(entry.getKey());
                        break;
                    default:
                        this.quitRoom(entry.getKey());
                        break;
                }
            }
            GunWarSwapTeamEvent ev = new GunWarSwapTeamEvent(this, oldRedTeam, oldBlueTeam, this.redScore, this.blueScore);
            Server.getInstance().getPluginManager().callEvent(ev);
            if (!ev.isCancelled()) {
                this.changeTeam = true;
                delay = 60;
                Tools.sendTitle(this, this.language.translateString("game_blasting_changeTeam"));
                for (Player player : ev.getNewRedTeam()) {
                    this.getPlayerData(player).setTeam(Team.RED);
                    player.setNameTag("§c" + player.getName());
                }
                for (Player player : ev.getNewBlueTeam()) {
                    this.getPlayerData(player).setTeam(Team.BLUE);
                    player.setNameTag("§9" + player.getName());
                }
                this.redScore = ev.getNewRedScore();
                this.blueScore = ev.getNewBlueScore();
            }
        }
        Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, () -> {
            super.roundStart();
            //随机挑选一名红队成员给炸弹
            LinkedList<Player> list = new LinkedList<>();
            for (Map.Entry<Player, PlayerGameData> entry : this.getPlayerDataMap().entrySet()) {
                if (entry.getValue().getTeam() == Team.RED) {
                    list.add(entry.getKey());
                }
            }
            if (!list.isEmpty()) {
                Player player = list.get(GunWar.RANDOM.nextInt(list.size()));
                player.getInventory().addItem(Tools.getItem(201));
                player.sendTitle("", this.language.translateString("game_blasting_youCarryBomb"));
            }
        }, delay);
    }

    @Override
    public void roundEnd(Team victory) {
        if (this.entityGunWarBomb != null) {
            this.entityGunWarBomb.close();
        }
        if (this.entityGunWarBombBlock != null) {
            this.entityGunWarBombBlock.close();
        }
        super.roundEnd(victory);
        this.entityGunWarBomb = null;
        this.entityGunWarBombBlock = null;
        if (!this.bossBarMap.isEmpty()) {
            for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
                entry.getKey().removeBossBar(entry.getValue().getBossBarId());
            }
            this.bossBarMap.clear();
        }
        this.bombWasFound = false;
        this.demolitionBombPlayer = null;
    }

    @Override
    public void playerDeath(Player player, Entity damager, String killMessage) {
        Item bomb = Tools.getItem(201);
        for (Item item : player.getInventory().getContents().values()) {
            if (bomb.equals(item)) {
                this.getLevel().dropItem(player, bomb);
                break;
            }
        }
        super.playerDeath(player, damager, killMessage);
    }

    public void setEntityGunWarBomb(EntityGunWarBomb entityGunWarBomb) {
        this.entityGunWarBomb = entityGunWarBomb;
    }

    public EntityGunWarBomb getEntityGunWarBomb() {
        return this.entityGunWarBomb;
    }

    public void setEntityGunWarBombBlock(EntityGunWarBombBlock entityGunWarBombBlock) {
        this.entityGunWarBombBlock = entityGunWarBombBlock;
    }

    public EntityGunWarBombBlock getEntityGunWarBombBlock() {
        return this.entityGunWarBombBlock;
    }

    /**
     * 炸弹爆炸
     */
    public void bombExplosion() {
        this.setRoundIsEnd(true);
        Tools.sendTitle(this, this.language.translateString("game_blasting_bombHasExploded"));
        for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
            entry.getKey().removeBossBar(entry.getValue().getBossBarId());
        }
        Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, () -> this.roundEnd(Team.RED), 60);
    }

    /**
     * @return 爆破点A
     */
    public Position getBlastingPointA() {
        String[] s = this.blastingPointA.split(":");
        return new Position(Integer.parseInt(s[0]),
                Integer.parseInt(s[1]),
                Integer.parseInt(s[2]),
                this.getLevel());
    }

    /**
     * @return 爆破点B
     */
    public Position getBlastingPointB() {
        String[] s = this.blastingPointB.split(":");
        return new Position(Integer.parseInt(s[0]),
                Integer.parseInt(s[1]),
                Integer.parseInt(s[2]),
                this.getLevel());
    }

    /**
     * @return 爆破点范围半径
     */
    public double getBlastingPointRadius() {
        return 5;
    }

    /**
     * @return 安装炸弹用时 (tick)
     */
    public int getPlantBombTime() {
        return 5 * 20;
    }

    /**
     * @return 拆除炸弹那用时 (tick)
     */
    public int getDemolitionBombTime() {
        return 5 * 20;
    }

    /**
     * @return 炸弹爆炸时间 (秒)
     */
    public int getBombExplosionTime() {
        return this.bombExplosionTime;
    }

}
