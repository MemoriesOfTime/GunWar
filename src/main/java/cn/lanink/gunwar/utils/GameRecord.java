package cn.lanink.gunwar.utils;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public class GameRecord {

    public static String KILLS = "kills";
    public static String DEATHS = "deaths";
    public static String VICTORY = "victory";
    public static String DEFEAT = "defeat";

    /**
     * 获取玩家记录集合
     * @param player 玩家
     * @return 记录集合
     */
    public static LinkedHashMap<String, Integer> getPlayerRecord(Player player) {
        Config config = GunWar.getInstance().getGameRecord();
        Map<String, Object> map = config.getAll();
        LinkedHashMap<String, Integer> record;
        if (map.containsKey(player.getName())) {
            record = (LinkedHashMap<String, Integer>) map.get(player.getName());
        }else {
            record = new LinkedHashMap<>();
        }
        return record;
    }

    /**
     * 添加击杀数
     * @param player 玩家
     */
    public static void addKills(Player player) {
        Config config = GunWar.getInstance().getGameRecord();
        LinkedHashMap<String, Integer> record = getPlayerRecord(player);
        int number = record.getOrDefault(KILLS, 0) + 1;
        record.put(KILLS, number);
        config.set(player.getName(), record);
        config.save();
    }

    /**
     * 获取击杀数
     * @param player 玩家
     * @return 击杀数
     */
    public static int getKills(Player player) {
        LinkedHashMap<String, Integer> record = getPlayerRecord(player);
        return record.getOrDefault(KILLS, 0);
    }

    /**
     * 添加死亡数
     * @param player 玩家
     */
    public static void addDeaths(Player player) {
        Config config = GunWar.getInstance().getGameRecord();
        LinkedHashMap<String, Integer> record = getPlayerRecord(player);
        int number = record.getOrDefault(DEATHS, 0) + 1;
        record.put(DEATHS, number);
        config.set(player.getName(), record);
        config.save();
    }

    /**
     * 获取死亡数
     * @param player 玩家
     * @return 死亡数
     */
    public static long getDeaths(Player player) {
        LinkedHashMap<String, Integer> record = getPlayerRecord(player);
        return record.getOrDefault(DEATHS, 0);
    }

    /**
     * 添加胜利次数
     * @param player 玩家
     */
    public static void addVictory(Player player) {
        Config config = GunWar.getInstance().getGameRecord();
        LinkedHashMap<String, Integer> record = getPlayerRecord(player);
        int number = record.getOrDefault(VICTORY, 0) + 1;
        record.put(VICTORY, number);
        config.set(player.getName(), record);
        config.save();
    }

    /**
     * 获取胜利次数
     * @param player 玩家
     * @return 胜利次数
     */
    public static long getVictory(Player player) {
        LinkedHashMap<String, Integer> record = getPlayerRecord(player);
        return record.getOrDefault(VICTORY, 0);
    }

    /**
     * 添加失败次数
     * @param player 玩家
     */
    public static void addDefeat(Player player) {
        Config config = GunWar.getInstance().getGameRecord();
        LinkedHashMap<String, Integer> record = getPlayerRecord(player);
        int number = record.getOrDefault(DEFEAT, 0) + 1;
        record.put(DEFEAT, number);
        config.set(player.getName(), record);
        config.save();
    }

    /**
     * 获取失败次数
     * @param player 玩家
     * @return 失败次数
     */
    public static long getDefeat(Player player) {
        LinkedHashMap<String, Integer> record = getPlayerRecord(player);
        return record.getOrDefault(DEFEAT, 0);
    }

}
