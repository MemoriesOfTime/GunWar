package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.item.ItemManage;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class GunWeapon extends BaseWeapon {

    public GunWeapon(String name, Config config) {
        super(name, config);
    }

    @Override
    public ItemManage.ItemType getItemType() {
        return null;
    }

    //TODO 弹匣 换弹时间


}
