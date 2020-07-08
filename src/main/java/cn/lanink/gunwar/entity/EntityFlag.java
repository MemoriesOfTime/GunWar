package cn.lanink.gunwar.entity;

import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityFlag extends EntityHuman {

    private int flag = 0;

    public EntityFlag(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.yaw += 10;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (flag < 10) {
            this.yaw++;
        }else {
            this.yaw--;
        }
        this.flag++;
        if (this.flag >= 20) {
            this.flag = 0;
        }
        return super.onUpdate(currentTick);
    }
}
