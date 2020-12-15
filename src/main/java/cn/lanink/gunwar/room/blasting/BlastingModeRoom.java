package cn.lanink.gunwar.room.blasting;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

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
    public void timeTask() {
        super.timeTask();
        //显示爆破点
        CompletableFuture.runAsync(() -> {
            for (Vector3 vector3 : Tools.getRoundEdgePoint(this.getBlastingPointA(), 5)) {
                this.getLevel().addParticleEffect(vector3, ParticleEffect.REDSTONE_TORCH_DUST);
            }
            for (Vector3 vector3 : Tools.getRoundEdgePoint(this.getBlastingPointB(), 5)) {
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

}
