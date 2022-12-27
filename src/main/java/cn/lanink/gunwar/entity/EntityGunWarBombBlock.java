package cn.lanink.gunwar.entity;

import cn.lanink.gamecore.utils.EntityUtils;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * 拆弹点击判断用 炸弹实体
 *
 * @author lt_name
 */
public class EntityGunWarBombBlock extends EntityHuman {

    public EntityGunWarBombBlock(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt.putCompound("Skin", new CompoundTag()));
        this.setNameTag("");
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.setDataFlag(
                EntityUtils.getEntityField("DATA_FLAGS", DATA_FLAGS),
                EntityUtils.getEntityField("DATA_FLAG_INVISIBLE", DATA_FLAG_INVISIBLE),
                true
        );
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
        return 1.1F;
    }

    @Override
    public float getWidth() {
        return 1.1F;
    }

    @Override
    public float getHeight() {
        return 1.1F;
    }

}
