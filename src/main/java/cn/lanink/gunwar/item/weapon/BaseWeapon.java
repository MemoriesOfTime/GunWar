package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.item.base.BaseItem;
import cn.lanink.gunwar.utils.Tools;
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
        this.minDamage = Math.abs(Double.parseDouble(stringDamage[0]));
        if (stringDamage.length > 1) {
            this.maxDamage = Math.abs(Double.parseDouble(stringDamage[1]));
        }else {
            this.maxDamage = minDamage;
        }
        this.killMessage = config.getString("killMessage");
        this.getCompoundTag()
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

    public double getRandomDamage() {
        return Tools.randomDouble(this.getMinDamage(), this.getMaxDamage());
    }

    public String getKillMessage() {
        return this.killMessage;
    }

}
