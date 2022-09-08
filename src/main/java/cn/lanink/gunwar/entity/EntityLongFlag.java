package cn.lanink.gunwar.entity;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.utils.FlagSkinType;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Getter;

/**
 * @author LT_Name
 */
public class EntityLongFlag extends EntityHuman {

    private Team team;
    private EntityFlagHead entityFlagHead;
    @Getter
    private int flagHeight = 100;

    @Override
    public float getHeight() {
        return 2.5F;
    }

    @Override
    public float getLength() {
        return 0.3F;
    }

    @Override
    public float getWidth() {
        return 0.3F;
    }

    public EntityLongFlag(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
    }

    public EntityLongFlag(FullChunk chunk, CompoundTag nbt, Team team) {
        this(chunk, nbt);
        this.team = team;
        this.setSkin(GunWar.getInstance().getFlagSkin(FlagSkinType.LONG_FLAGPOLE));
        this.spawnFlagHead();
    }

    /**
     * 生成旗帜头实体
     */
    protected void spawnFlagHead() {
        Skin headSkin = GunWar.getInstance().getFlagSkin((this.team == Team.RED || this.team == Team.RED_DEATH) ? FlagSkinType.FLAG_HEAD_RED : FlagSkinType.FLAG_HEAD_BLUE);
        CompoundTag tag = getDefaultNBT(this);
        tag.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", headSkin.getSkinData().data)
                .putString("ModelId", headSkin.getSkinId()));
        this.entityFlagHead = new EntityFlagHead(this.chunk, tag);
        this.entityFlagHead.setSkin(headSkin);
        this.entityFlagHead.spawnToAll();
        this.setFlagHeight(this.getFlagHeight());
    }

    /**
     * 按比例设置旗帜头高度
     *
     * @param height 旗帜头高度(1-100)
     */
    public void setFlagHeight(int height) {
        this.flagHeight = Math.min(100, Math.max(0, height));
        this.entityFlagHead.setY(this.getY() + this.getHeight() * this.flagHeight/100f);
    }

    public static class EntityFlagHead extends EntityHuman {

        public EntityFlagHead(FullChunk chunk, CompoundTag nbt) {
            super(chunk, nbt);
        }

    }

}
