package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.item.ItemManage;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class MeleeWeapon extends BaseWeapon {

    protected final float knockBack;

    public MeleeWeapon(String name, Config config) {
        super(name, config);
        this.knockBack = Math.abs((float) config.getDouble("knockBack", 0.3D));
        this.getCompoundTag().putFloat("knockBack", this.knockBack);
    }

    @Override
    public ItemManage.ItemType getItemType() {
        return ItemManage.ItemType.MELEE_WEAPON;
    }

    /**
     * @return 击退
     */
    public float getKnockBack() {
        return this.knockBack;
    }

}
