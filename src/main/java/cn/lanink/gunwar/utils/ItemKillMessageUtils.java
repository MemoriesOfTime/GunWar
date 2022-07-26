package cn.lanink.gunwar.utils;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LT_Name
 */
public class ItemKillMessageUtils {

    private static final HashMap<Item, String> ITEM_KILL_MESSAGE = new HashMap<>();

    public static void load() {
        GunWar.getInstance().saveResource("ItemKillMessage.yml", false);
        new Config(GunWar.getInstance().getDataFolder() + "/ItemKillMessage.yml", Config.YAML).getAll().forEach((key, value) -> {
            Item item = Item.fromString(key);
            if (item.getId() != Item.AIR) {
                ITEM_KILL_MESSAGE.put(item, value.toString());
            }
        });
    }

    public static String getKillMessage(Item item) {
        //TODO 优化
        //return ITEM_KILL_MESSAGE.get(item);
        for (Map.Entry<Item, String> entry : ITEM_KILL_MESSAGE.entrySet()) {
            if (entry.getKey().getId() == item.getId() && entry.getKey().getDamage() == item.getDamage()) {
                if (entry.getKey().getName().equals(item.getName())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

}
