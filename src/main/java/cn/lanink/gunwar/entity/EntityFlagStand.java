package cn.lanink.gunwar.entity;

import cn.lanink.gunwar.GunWar;
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
        this.setSkin(GunWar.getInstance().getFlagSkin(0));
    }

    @Override
    public float getHeight() {
        return 0.5F;
    }

    @Override
    public float getWidth() {
        return 1;
    }

}
