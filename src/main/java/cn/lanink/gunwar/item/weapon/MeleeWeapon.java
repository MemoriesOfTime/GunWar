package cn.lanink.gunwar.item.weapon;

import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class MeleeWeapon extends BaseWeapon {

    private final int attackCooldown;

    public MeleeWeapon(String name, Config config) {
        super(name, config);
        this.attackCooldown = config.getInt("attackCooldown");
        this.getGunWarItemTag().putInt("attackCooldown", this.attackCooldown);
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

}
