package cn.lanink.gunwar.entity;

import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author lt_name
 */
public class EntityGunWarBombBlock extends EntityHuman {

    public EntityGunWarBombBlock(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setNameTag("");
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.setDataFlag(0, 5, true);
    }

    @Override
    public int getNetworkId() {
        return 64;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(20);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        this.deadTicks = 0;
        return super.onUpdate(currentTick);
    }

    @Override
    public float getLength() {
        return 1.05F;
    }

    @Override
    public float getWidth() {
        return 1.05F;
    }

    @Override
    public float getHeight() {
        return 1.05F;
    }

}
