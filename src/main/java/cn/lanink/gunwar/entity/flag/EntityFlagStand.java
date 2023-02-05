package cn.lanink.gunwar.entity.flag;

import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author lt_name
 */
public class EntityFlagStand extends EntityHuman {

    public EntityFlagStand(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
    }

    @Override
    public float getHeight() {
        return 0.5F;
    }

    @Override
    public float getLength() {
        return 1;
    }

    @Override
    public float getWidth() {
        return 1;
    }

}
