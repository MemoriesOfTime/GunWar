package cn.lanink.gunwar.entity.tower;

import cn.lanink.gamecore.GameCore;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author LT_Name
 */
public class EntityCrossbowTower extends EntityBaseTower {

    private static final TowerDefinition CROSSBOW_TOWER_DEFINITION;

    static {
        CROSSBOW_TOWER_DEFINITION = new TowerDefinition();
        CROSSBOW_TOWER_DEFINITION.setName("CrossbowTower");
        CROSSBOW_TOWER_DEFINITION.setMaxHealth(100);
        CROSSBOW_TOWER_DEFINITION.setAttackDamage(4);
        CROSSBOW_TOWER_DEFINITION.setAttackSpeed(30);
        CROSSBOW_TOWER_DEFINITION.setAttackMinRange(3);
        CROSSBOW_TOWER_DEFINITION.setAttackMaxRange(15);
    }

    public EntityCrossbowTower(BaseRoom room, Team team, FullChunk chunk, CompoundTag nbt) {
        super(room, team, chunk, nbt);
        this.setTowerDefinition(CROSSBOW_TOWER_DEFINITION);
        this.setSkin(GameCore.MODEL.getModel("GunWar:CrossbowTower"));
    }

}
