package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.item.ItemManage;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class MeleeWeapon extends BaseWeapon {

    protected final int attackCooldown;
    protected final float knockBack;
    protected final boolean infiniteDurability;

    public MeleeWeapon(String name, Config config) {
        super(name, config);
        this.attackCooldown = config.getInt("attackCooldown");
        this.knockBack = (float) config.getDouble("knockBack", 0.3D);
        this.infiniteDurability = config.getBoolean("infiniteDurability");
        this.getGunWarItemTag()
                .putInt("attackCooldown", this.attackCooldown)
                .putFloat("knockBack", this.knockBack)
                .putBoolean("infiniteDurability", this.infiniteDurability);
    }

    @Override
    public ItemManage.ItemType getItemType() {
        return ItemManage.ItemType.MELEE_WEAPON;
    }

    /**
     * @return 攻击间隔
     */
    public int getAttackCooldown() {
        return this.attackCooldown;
    }

    /**
     * @return 击退
     */
    public float getKnockBack() {
        return this.knockBack;
    }

}
