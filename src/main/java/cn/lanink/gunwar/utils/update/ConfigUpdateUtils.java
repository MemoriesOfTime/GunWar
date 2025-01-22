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

            LinkedHashMap<String, Object> fapWinIntegral = new LinkedHashMap<>();
            fapWinIntegral.put("money", 10);
            fapWinIntegral.put("exp", 10);
            fapWinIntegral.put("maxMultiplier", 1);

            LinkedHashMap<String, Object> fapLoseIntegral = new LinkedHashMap<>();
            fapLoseIntegral.put("money", 5);
            fapLoseIntegral.put("exp", 5);
            fapLoseIntegral.put("maxMultiplier", 1);

            boolean needSave = false;
            if (!config.exists("fapWinIntegral")) {
                config.set("fapWinIntegral", fapWinIntegral);
                needSave = true;
            }
            if (!config.exists("fapLoseIntegral")) {
                config.set("fapLoseIntegral", fapLoseIntegral);
                needSave = true;
            }

            if (needSave) {
                config.save();
            }
        } catch (Exception ignored) {

        }
    }

}
