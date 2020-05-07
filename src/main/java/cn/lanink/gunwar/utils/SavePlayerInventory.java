package cn.lanink.gunwar.utils;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author 若水
 */
public class SavePlayerInventory {

    /**
     * 保存玩家背包
     * @param player 玩家
     * @param restore 是否为还原
     */
    public static void savePlayerInventory(Player player, boolean restore) {
        File file = new File(GunWar.getInstance().getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
        if (restore) {
            if (file.exists()) {
                Config config = new Config(file, 1);
                if (file.delete()) {
                    player.getInventory().clearAll();
                    PutInventory(player, config.get("Inventory", null));
                }
            }
        }else {
            Config config = new Config(file, 1);
            config.set("Inventory", InventoryToJson(player));
            config.save();
            player.getInventory().clearAll();
        }
    }

    public static LinkedHashMap<String, Object> InventoryToJson(@NotNull Player player) {
        LinkedHashMap<String, Object> Inventory = new LinkedHashMap<>();
        for (int i = 0; i < player.getInventory().getSize() + 4; i++) {
            LinkedList<String> list = new LinkedList<>();
            Item item = player.getInventory().getItem(i);
            list.add(item.getId() + ":" + item.getDamage());
            list.add(item.getCount() + "");
            String tag = item.hasCompoundTag() ? bytesToHexString(item.getCompoundTag()) : "not";
            list.add(tag);
            Inventory.put(i + "", list);
        }
        return Inventory;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0)
            return null;
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2)
                stringBuilder.append(0);
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static void PutInventory(Player player, Map inventory) {
        if (inventory == null || inventory.isEmpty()) {
            return;
        }
        for (int i = 0; i < player.getInventory().getSize() + 4; i++) {
            List list = (List)inventory.get(i + "");
            Item item = Item.fromString((String) list.get(0));
            item.setCount(Integer.parseInt((String) list.get(1)));
            if (!String.valueOf(list.get(2)).equals("not")) {
                CompoundTag tag = Item.parseCompoundTag(hexStringToBytes((String) list.get(2)));
                item.setNamedTag(tag);
            }
            if (player.getInventory().getSize() + 4 < i) {
                player.getInventory().addItem(item.clone());
            } else {
                player.getInventory().setItem(i, item.clone());
            }
        }
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals(""))
            return null;
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

}
