package cn.lanink.gunwar.utils;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Data;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author LT_Name
 */
public class ItemKillMessageUtils {

    private static final HashMap<ItemData, String> ITEM_KILL_MESSAGE = new HashMap<>();
    private static final Cache<Item, ItemData> ITEMDATA_CACHE = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.MINUTES).build();


    public static void load() {
        GunWar.getInstance().saveResource("ItemKillMessage.yml", false);
        new Config(GunWar.getInstance().getDataFolder() + "/ItemKillMessage.yml", Config.YAML).getAll().forEach((key, value) -> {
            try {
                Item item = Item.fromString(key);
                if (item.getId() != Item.AIR) {
                    ITEM_KILL_MESSAGE.put(ItemData.of(item), value.toString());
                }
            }catch (Exception e) {
                //nkx不支持字符串物品，大部分报错可以忽略
                if (GunWar.debug) {
                    GunWar.getInstance().getLogger().error("读取ItemKillMessage.yml错误：", e);
                }
            }
        });
    }

    public static String getKillMessage(Item item) {
        return ITEM_KILL_MESSAGE.get(ItemData.of(item));
    }

    @Data
    public static class ItemData {
        public int id;
        public int damage;
        public String name;

        private ItemData(Item item) {
            this.id = item.getId();
            this.damage = item.getDamage();
            this.name = item.getName();
        }

        public static ItemData of(Item item) {
            ItemData itemData = ITEMDATA_CACHE.getIfPresent(item);
            if (itemData == null) {
                itemData = new ItemData(item);
                ITEMDATA_CACHE.put(item, itemData);
            }
            return itemData;
        }
    }

}
