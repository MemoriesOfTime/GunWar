package cn.lanink.gunwar.room.blasting;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityGunWarBomb;
import cn.lanink.gunwar.entity.EntityGunWarBombBlock;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.DummyBossBar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lt_name
 */
public class BlastingModeRoom extends BaseRoom {

    protected final String blastingPointA, blastingPointB;
    protected EntityGunWarBomb entityGunWarBomb;
    private EntityGunWarBombBlock entityGunWarBombBlock;
    protected final ConcurrentHashMap<Player, DummyBossBar> bossBarMap = new ConcurrentHashMap<>();
    protected boolean bombWasFound = false;
    protected Player demolitionBombPlayer;

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
        //Boss血条显示炸弹爆炸倒计时
        if (this.entityGunWarBomb != null && !this.entityGunWarBomb.isClosed() &&
                this.entityGunWarBomb.getExplosionTime() > 0) {
            double discoveryDistance = this.getBlastingPointRadius() * 0.8 + 5;
            for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
                if (entry.getValue() == 2) {
                    if (entry.getKey().distance(this.getBlastingPointA()) <= discoveryDistance ||
                            entry.getKey().distance(this.getBlastingPointB()) <= discoveryDistance) {
                        this.bombWasFound = true;
                    }
                }
            }
            String s = "";
            if (this.bombWasFound) {
                if (this.entityGunWarBomb.distance(this.getBlastingPointA()) <= this.getBlastingPointRadius()) {
                    s += "§cA";
                }else {
                    s += "§9B";
                }
                s += "§a点发现炸弹  ";
            }
            s += "§e炸弹爆炸倒计时：§l§c" + this.entityGunWarBomb.getExplosionTime();
            for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
                Tools.createBossBar(entry.getKey(), this.bossBarMap);
                DummyBossBar bossBar = this.bossBarMap.get(entry.getKey());
                bossBar.setText(s);
                bossBar.setLength(this.entityGunWarBomb.getExplosionTime() / 50F * 100);
            }
        }else if (!this.bossBarMap.isEmpty()) {
            for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
                entry.getKey().removeBossBar(entry.getValue().getBossBarId());
            }
            this.bossBarMap.clear();
        }
        //显示爆破点
        CompletableFuture.runAsync(() -> {
            LinkedList<Vector3> list = Tools.getRoundEdgePoint(this.getBlastingPointA(), this.getBlastingPointRadius());
            list.addAll(Tools.getRoundEdgePoint(this.getBlastingPointB(), this.getBlastingPointRadius()));
            for (Vector3 vector3 : list) {
                vector3.y += 0.1;
                if (this.getLevel().getBlock(vector3).getId() == BlockID.AIR) {
                    for (int y = vector3.getFloorY(); y > y-5; y--) {
                        if (this.getLevel().getBlock(new Vector3(vector3.x, y, vector3.z)).getId() != BlockID.AIR) {
                            vector3.y = y + 1.1;
                            break;
                        }
                    }
                }else {
                    for (int y = vector3.getFloorY(); y < y+5; y++) {
                        if (this.getLevel().getBlock(new Vector3(vector3.x, y, vector3.z)).getId() == BlockID.AIR) {
                            vector3.y = y + 0.1;
                            break;
                        }
                    }
                }
                this.getLevel().addParticleEffect(vector3, ParticleEffect.REDSTONE_TORCH_DUST);
            }
        });
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
    }

    @Override
    public void roundStart() {
        //TODO 判断回合 交换队伍
        super.roundStart();
        LinkedList<Player> list = new LinkedList<>();
        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
            if (entry.getValue() == 1) {
                list.add(entry.getKey());
            }
        }
        Player player = list.get(GunWar.RANDOM.nextInt(list.size()));
        player.getInventory().addItem(Tools.getItem(201));
        player.sendTitle("", "你携带着炸弹！");
    }

    @Override
    public void roundEnd(int victory) {
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

    public void setDemolitionBombPlayer(Player demolitionBombPlayer) {
        this.demolitionBombPlayer = demolitionBombPlayer;
    }

    public Player getDemolitionBombPlayer() {
        return this.demolitionBombPlayer;
    }

    /**
     * 炸弹爆炸
     */
    public void bombExplosion() {
        for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
            entry.getKey().removeBossBar(entry.getValue().getBossBarId());
        }
        this.roundEnd(1);
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

}
