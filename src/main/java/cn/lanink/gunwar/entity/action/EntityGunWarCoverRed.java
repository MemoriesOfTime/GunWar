package cn.lanink.gunwar.entity.action;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * 红色的遮盖实体
 *
 * @author LT_Name
 */
public class EntityGunWarCoverRed extends Entity implements CustomEntity {

    public static final EntityDefinition ENTITY_DEFINITION = EntityDefinition.builder()
            .identifier("gunwar:gunwar_cover_red")
            .spawnEgg(false)
            .implementation(EntityGunWarCoverRed.class)
            .build();

    public EntityGunWarCoverRed(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setScale(2);
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return ENTITY_DEFINITION;
    }

    @Override
    public int getNetworkId() {
        return this.getEntityDefinition().getRuntimeId();
    }
}
