package cn.lanink.gunwar.entity.flag;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.utils.FlagSkinType;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * @author LT_Name
 */
public class EntityLongFlag extends EntityHuman {

    @Getter
    private Team team;

    private EntityFlagStand entityFlagStand;
    private EntityFlagHead entityFlagHead;

    @Getter
    private int flagHeight = 100;

    @Setter
    @Getter
    private int keepTime = 0;

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

    @Deprecated
    public EntityLongFlag(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public EntityLongFlag(FullChunk chunk, CompoundTag nbt, Team team) {
        super(chunk, nbt);
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.team = team;
        this.setSkin(GunWar.getInstance().getFlagSkin(FlagSkinType.LONG_FLAGPOLE));
        this.spawnFlagStand();
        this.setY(this.getY() + 0.4);
        this.spawnFlagHead();
    }

    /**
     * 生成旗帜底座
     */
    protected void spawnFlagStand() {
        Skin skin = GunWar.getInstance().getFlagSkin(getFlagStandSkinType());
        CompoundTag tag = EntityFlagStand.getDefaultNBT(this);
        tag.putFloat("Scale", 1.0F);
        tag.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        this.entityFlagStand = new EntityFlagStand(this.chunk, tag);
        this.entityFlagStand.setSkin(skin);
        this.entityFlagStand.spawnToAll();
    }

    /**
     * 生成旗帜头实体
     */
    protected void spawnFlagHead() {
        Skin headSkin = GunWar.getInstance().getFlagSkin(getFlagHeadSkinType());
        CompoundTag tag = getDefaultNBT(this);
        tag.putFloat("Scale", 1.0F);
        tag.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", headSkin.getSkinData().data)
                .putString("ModelId", headSkin.getSkinId()));
        this.entityFlagHead = new EntityFlagHead(this.chunk, tag);
        this.entityFlagHead.setSkin(headSkin);
        this.entityFlagHead.spawnToAll();
        this.setFlagHeight(this.getFlagHeight());
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (currentTick%20 == 0) {
            this.keepTime++;
        }
        return super.onUpdate(currentTick);
    }

    public void setTeam(Team team) {
        this.keepTime = 0;
        this.team = team;
        if (this.entityFlagStand != null) {
            Tools.setHumanSkin(this.entityFlagStand, GunWar.getInstance().getFlagSkin(getFlagStandSkinType()));
        }
        if (this.entityFlagHead != null) {
            Tools.setHumanSkin(this.entityFlagHead, GunWar.getInstance().getFlagSkin(getFlagHeadSkinType()));
        }
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

    public void addTeamPoints(Team team, int points) {
        if (team == this.getTeam()) {
            if (this.getFlagHeight() < 100) {
                this.setFlagHeight(this.getFlagHeight() + points);
            }
        }else {
            if (this.getFlagHeight() > 0) {
                this.setFlagHeight(this.getFlagHeight() - points);
            }else {
                this.setTeam(team);
            }
        }
    }

    @Override
    public void close() {
        super.close();
        if (this.entityFlagStand != null) {
            this.entityFlagStand.close();
        }
        if (this.entityFlagHead != null) {
            this.entityFlagHead.close();
        }
    }

    @NotNull
    private FlagSkinType getFlagStandSkinType() {
        FlagSkinType skinType;
        switch (this.team) {
            case RED:
            case RED_DEATH:
                skinType = FlagSkinType.FLAG_STAND_RED;
                break;
            case BLUE:
            case BLUE_DEATH:
                skinType = FlagSkinType.FLAG_STAND_BLUE;
                break;
            case NULL:
            default:
                skinType = FlagSkinType.FLAG_STAND_WHITE;
                break;
        }
        return skinType;
    }

    @NotNull
    private FlagSkinType getFlagHeadSkinType() {
        FlagSkinType skinType;
        switch (this.team) {
            case RED:
            case RED_DEATH:
                skinType = FlagSkinType.FLAG_HEAD_RED;
                break;
            case BLUE:
            case BLUE_DEATH:
                skinType = FlagSkinType.FLAG_HEAD_BLUE;
                break;
            case NULL:
            default:
                skinType = FlagSkinType.FLAG_HEAD_WHITE;
                break;
        }
        return skinType;
    }

    public static class EntityFlagHead extends EntityHuman {

        public EntityFlagHead(FullChunk chunk, CompoundTag nbt) {
            super(chunk, nbt);
        }

        @Override
        public float getHeight() {
            return 0.01F;
        }

        @Override
        public float getLength() {
            return 0.01F;
        }

        @Override
        public float getWidth() {
            return 0.01F;
        }

    }

}
