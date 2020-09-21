package cn.lanink.gunwar.item.weapon;

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
    public ItemType getItemType() {
        return ItemType.MELEE_WEAPON;
    }

    /**
     * @return 攻击间隔
     */
    public int getAttackCooldown() {
        return this.attackCooldown;
    }

    public float getKnockBack() {
        return this.knockBack;
    }

}
