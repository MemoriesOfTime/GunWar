package cn.lanink.gunwar.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;

/**
 * @author lt_name
 */
public class EntityText extends Entity {

    @Override
    public int getNetworkId() {
        return 64;
    }

    public EntityText(Position position, String nameTag) {
        super(position.getChunk(), Entity.getDefaultNBT(position));
        this.setNameTagVisible(true);
        this.setNameTagAlwaysVisible(true);
        this.setNameTag(nameTag);
        this.setImmobile(true);
    }

}
