package cn.lanink.gunwar.utils.nsgb;

import cn.nsgamebase.entity.pojo.AbstractDataGamePlayerPojo;
import cn.nukkit.Player;

/**
 * @author LT_Name
 */
public class GunWarDataGamePlayerPojo extends AbstractDataGamePlayerPojo {

    public static GunWarDataGamePlayerPojo getGamePlayerPojo(Player player) {
        GunWarDataGamePlayerPojo pojo = new GunWarDataGamePlayerPojo();

        pojo.registerKey("killCount", "击杀数");

        return pojo;
    }

}
