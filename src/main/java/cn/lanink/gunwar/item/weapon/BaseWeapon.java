package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.item.base.BaseItem;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public abstract class BaseWeapon extends BaseItem {

    protected final double minDamage;
    protected final double maxDamage;
    protected final String killMessage;

    public BaseWeapon(String name, Config config) {
        super(name, config);
        String[] stringDamage = config.getString("damage").split("-");
        this.minDamage = Double.parseDouble(stringDamage[0]);
        this.maxDamage = Double.parseDouble(stringDamage[1]);
        this.killMessage = config.getString("killMessage");
        this.getGunWarItemTag()
                .putDouble("minDamage", this.minDamage)
                .putDouble("maxDamage", this.maxDamage)
                .putString("killMessage", this.killMessage);
    }

    public double getMinDamage() {
        return this.minDamage;
    }

    public double getMaxDamage() {
        return this.maxDamage;
    }

}
