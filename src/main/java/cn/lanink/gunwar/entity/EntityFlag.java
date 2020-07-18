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
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if(currentTick%2 == 0) {
            if (flag < 20) {
                this.yaw++;
            }else {
                this.yaw--;
            }
            this.flag++;
            if (this.flag >= 40) {
                this.flag = 0;
            }
        }
        return super.onUpdate(currentTick);
    }

    @Override
    public float getHeight() {
        return 1.6F;
    }

    @Override
    public float getLength() {
        return 0.3F;
    }

    @Override
    public float getWidth() {
        return 0.3F;
    }

}
