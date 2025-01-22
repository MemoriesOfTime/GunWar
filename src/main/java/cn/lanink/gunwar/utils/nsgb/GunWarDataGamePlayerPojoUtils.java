package cn.lanink.gunwar.utils.nsgb;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.PlayerGameData;
import cn.nsgamebase.api.GbGameApi;
import cn.nukkit.utils.Config;

/**
 * @author LT_Name
 */
public class GunWarDataGamePlayerPojoUtils {

    public static GunWarDataGamePlayerPojo getGamePlayerPojo() {
        GunWarDataGamePlayerPojo pojo = new GunWarDataGamePlayerPojo();

        pojo.registerKey("killCount", "击杀数");

        return pojo;
    }

    public static void onWin(PlayerGameData playerData) {
        GunWarDataGamePlayerPojo pojo = getGamePlayerPojo();
        pojo.add("played");
        pojo.add("win");
        pojo.add("killCount", playerData.getKillCount());
        Config gunWarConfig = GunWar.getInstance().getConfig();
        int money = gunWarConfig.getInt("fapWinIntegral.money");
        int exp = gunWarConfig.getInt("fapWinIntegral.exp");
        int maxMultiplier = gunWarConfig.getInt("fapWinIntegral.maxMultiplier");
        GbGameApi.saveAndReward(playerData.getPlayer().getName(), "GunWar", pojo, money, exp, maxMultiplier);
    }

    public static void onLose(PlayerGameData playerData) {
        GunWarDataGamePlayerPojo pojo = getGamePlayerPojo();

        pojo.add("played");
        pojo.add("killCount", playerData.getKillCount());
        Config gunWarConfig = GunWar.getInstance().getConfig();
        int money = gunWarConfig.getInt("fapLoseIntegral.money");
        int exp = gunWarConfig.getInt("fapLoseIntegral.exp");
        int maxMultiplier = gunWarConfig.getInt("fapLoseIntegral.maxMultiplier");
        GbGameApi.saveAndReward(playerData.getPlayer().getName(), "GunWar", pojo, money, exp, maxMultiplier);
    }

}
