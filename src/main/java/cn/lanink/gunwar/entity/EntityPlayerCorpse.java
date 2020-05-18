package cn.lanink.gunwar.entity;

import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * 玩家尸体
 * @author 若水
 */
public class EntityPlayerCorpse extends EntityHuman {

    public EntityPlayerCorpse(FullChunk chunk, CompoundTag nbt, int team) {
        super(chunk, nbt);
        this.setNameTagVisible(false);
        this.setNameTagAlwaysVisible(false);
        this.inventory.setArmorContents(Tools.getArmors(team));
    }

}
