package cn.lanink.gunwar.item;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.base.BaseItem;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.item.weapon.MeleeWeapon;
import cn.lanink.gunwar.item.weapon.ProjectileWeapon;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final HashMap<String, MeleeWeapon> meleeWeaponMap = new HashMap<>();
    private static final HashMap<String, ProjectileWeapon> projectileWeaponMap = new HashMap<>();
    private static final HashMap<String, GunWeapon> gunWeaponMap = new HashMap<>();

    private static final ConcurrentHashMap<Player, Long> playerAttackTime = new ConcurrentHashMap<>();

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
                    meleeWeaponMap.put(fileName[0], new MeleeWeapon(fileName[0], config));
                }
            }
        }
    }

    public static ConcurrentHashMap<Player, Long> getPlayerAttackTime() {
        return playerAttackTime;
    }

    public HashMap<String, MeleeWeapon> getMeleeWeaponMap() {
        return meleeWeaponMap;
    }

    public static MeleeWeapon getMeleeWeapon(Item item) {
        if (item.hasCompoundTag()) {
            String name = item.getNamedTag().getCompound(BaseItem.GUN_WAR_ITEM_TAG).getString(BaseItem.GUN_WAR_ITEM_NAME);
            if (!name.isEmpty()) {
                return meleeWeaponMap.get(name);
            }
        }
        return null;
    }

    public static boolean canAttack(Player player, MeleeWeapon weapon) {
        long nowTime = System.currentTimeMillis();
        long lastTime = playerAttackTime.getOrDefault(player, 0L);
        if (GunWar.debug) {
            GunWar.getInstance().getLogger().info("[debug] nowTime:" + nowTime + " lastTime:" + lastTime +
                    " differ(tick):" + (nowTime - lastTime) / 50 + " attackCooldown(tick):" + weapon.getAttackCooldown());
        }
        if ((nowTime - lastTime) / 50 >= weapon.getAttackCooldown()) {
            playerAttackTime.put(player, nowTime);
            return true;
        }
        return false;
    }

    public static ItemType getItemType(Item item) {
        if (item.hasCompoundTag()) {
            CompoundTag tag = item.getNamedTag().getCompound(BaseItem.GUN_WAR_ITEM_TAG);
            int intType = tag.getInt(BaseItem.GUN_WAR_ITEM_TYPE);
            for (ItemType itemType : ItemType.values()) {
                if (itemType.getIntType() == intType) {
                    return itemType;
                }
            }
        }
        return ItemType.NULL;
    }

    public enum ItemType {
        NULL(0),
        MELEE_WEAPON(1),
        PROJECTILE_WEAPON(2),
        GUN_WEAPON(3);

        private final int intType;

        ItemType(int intType) {
            this.intType = intType;
        }

        public int getIntType() {
            return this.intType;
        }

    }

}
