package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.item.ItemManage;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class ProjectileWeapon extends BaseWeapon {

    protected int range;

    public ProjectileWeapon(String name, Config config) {
        super(name, config);
        //this.range = range;
    }

    @Override
    public ItemManage.ItemType getItemType() {
        return ItemManage.ItemType.PROJECTILE_WEAPON;
    }

    /**
     * @return 有效半径
     */
    public int getRange() {
        return this.range;
    }

/*    public double getDamage(double distance) {
        return getDamage(this.getDamage(), this.getRange(), distance);
    }

    public static double getDamage(double damage, int range, double distance) {
        if (distance > range) {
            return 0;
        }else {
            return damage - (0.1 * distance);
        }
    }*/

}
