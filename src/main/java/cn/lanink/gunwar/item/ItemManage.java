package cn.lanink.gunwar.item;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.base.BaseItem;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.item.weapon.MeleeWeapon;
import cn.lanink.gunwar.item.weapon.ProjectileWeapon;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.HashMap;

/**
 * @author lt_name
 */
public class ItemManage {

    private final GunWar gunWar;
    private final String itemsFolder;
    private final String weaponFolder;
    private final String meleeWeaponFolder;
    private final String projectileWeaponFolder;
    private final String gunWeaponFolder;

    //武器
    private final HashMap<String, MeleeWeapon> meleeWeaponMap = new HashMap<>();
    private final HashMap<String, ProjectileWeapon> projectileWeaponMap = new HashMap<>();
    private final HashMap<String, GunWeapon> gunWeaponMap = new HashMap<>();

    public ItemManage(GunWar gunWar) {
        this.gunWar = gunWar;
        this.itemsFolder = this.gunWar.getDataFolder() + "/Items/";
        this.weaponFolder = this.itemsFolder + "Weapon/";
        this.meleeWeaponFolder = this.weaponFolder + "Melee";
        this.projectileWeaponFolder = this.weaponFolder + "Projectile";
        this.gunWeaponFolder = this.weaponFolder + "Gun";
        //TODO
        this.loadAllMeleeWeapon();


    }

    public String getMeleeWeaponFolder() {
        return this.meleeWeaponFolder;
    }

    public void loadAllMeleeWeapon() {
        if (!new File(this.meleeWeaponFolder).exists()) {
            this.gunWar.saveResource("Items/Weapon/Melee/demo.yml", false);
        }
        File[] files = new File(this.meleeWeaponFolder).listFiles();
        if (files != null) {
            for (File file : files) {
                String[] fileName = file.getName().split("\\.");
                if (fileName.length > 1 && "yml".equals(fileName[1])) {
                    Config config = new Config(file, Config.YAML);
                    this.meleeWeaponMap.put(fileName[0], new MeleeWeapon(fileName[0], config));
                }
            }
        }
    }

    public HashMap<String, MeleeWeapon> getMeleeWeaponMap() {
        return this.meleeWeaponMap;
    }

    public MeleeWeapon getMeleeWeapon(Item item) {
        if (item.hasCompoundTag()) {
            String name = item.getNamedTag().getCompound(BaseItem.GUN_WAR_ITEM_TYPE).getString(BaseItem.GUN_WAR_ITEM_NAME);
            if (!name.isEmpty()) {
                return this.meleeWeaponMap.get(name);
            }
        }
        return null;
    }

}
