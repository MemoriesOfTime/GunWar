package cn.lanink.gunwar.utils.update;

import cn.lanink.gunwar.GunWar;

import java.io.File;

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

}
