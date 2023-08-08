package cn.lanink.gunwar.utils.update;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * @author LT_Name
 */
public class ConfigUpdateUtils {

    public static void updateConfig(GunWar gunWar) {
        updateLanguageFile(gunWar);
    }

    private static void updateLanguageFile(GunWar gunWar) {
        for (String langName : gunWar.getSupportList()) {
            File languageFile = new File(gunWar.getDataFolder() + "/Language/" + langName + ".yml");
            if (languageFile.exists()) {
                File newFile = new File(gunWar.getDataFolder() + "/Language/" + langName + "_customize.yml");
                if (newFile.exists()) {
                    newFile.delete();
                }
                languageFile.renameTo(new File(gunWar.getDataFolder() + "/Language/" + langName + "_customize.yml"));
            }
        }
    }

    // 需要在NsGB加载后检查，放到onEnable里
    public static void checkFapNsGB(GunWar gunWar) {
        try {
            Class.forName("cn.nsgamebase.NsGameBaseMain");

            Config config = gunWar.getConfig();

            /*
              fapWinIntegral:
                money: 1
                point: 0
                exp: 0
                maxMultiplier: 1
              fapLoseIntegral:
                money: 1
                point: 0
                exp: 0
                maxMultiplier: 1
            */

            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("money", 1);
            map.put("point", 0);
            map.put("exp", 0);
            map.put("maxMultiplier", 1);

            boolean needSave = false;
            if (!config.exists("fapWinIntegral")) {
                config.set("fapWinIntegral", map);
                needSave = true;
            }
            if (!config.exists("fapLoseIntegral")) {
                config.set("fapLoseIntegral", map);
                needSave = true;
            }

            if (needSave) {
                config.save();
            }
        } catch (Exception ignored) {

        }
    }

}
