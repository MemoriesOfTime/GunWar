package cn.lanink.gunwar.room.blasting;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BlastingModeRoom extends BaseRoom {

    protected final String blastingPointA, blastingPointB;

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
    public void playerRespawn(Player player) {
        super.playerRespawn(player);
        if (GunWar.debug && "ltname".equals(player.getName())) {
            player.getInventory().addItem(Tools.getItem(201));
        }
    }

    @Override
    public void timeTask() {
        super.timeTask();
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
     * @return 炸弹安装用时 (tick)
     */
    public double getPlantBombTime() {
        return 5 * 20;
    }

}
