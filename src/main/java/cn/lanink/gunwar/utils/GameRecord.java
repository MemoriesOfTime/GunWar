package cn.lanink.gunwar.utils;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.*;

public class GameRecord {

    public static String KILLS = "kills";
    public static String DEATHS = "deaths";
    public static String VICTORY = "victory";
    public static String DEFEAT = "defeat";

    /**
     * 获取字符串格式类型
     * @param type 类型
     * @return 字符串格式类型
     */
    public static String getTypeByEnum(type type) {
        switch (type) {
            case KILLS:
                return "kills";
            case DEATHS:
                return "deaths";
            case VICTORY:
                return "victory";
            case DEFEAT:
                return "defeat";
        }
        return "";
    }

    /**
     * 获取玩家记录集合
     * @param player 玩家
     * @return 记录集合
     */
    public static HashMap<String, Integer> getPlayerRecord(Player player) {
        return getPlayerRecord(player.getName());
    }

    /**
     * 获取玩家记录集合
     * @param player 玩家
     * @return 记录集合
     */
    public static HashMap<String, Integer> getPlayerRecord(String player) {
        Config config = GunWar.getInstance().getGameRecord();
        Map<String, Object> map = config.getAll();
        HashMap<String, Integer> record;
        if (map.containsKey(player)) {
            record = (HashMap<String, Integer>) map.get(player);
        }else {
            record = new HashMap<>();
        }
        return record;
    }

    /**
     * 添加击杀数
     * @param player 玩家
     */
    public static void addKills(Player player) {
        Config config = GunWar.getInstance().getGameRecord();
        HashMap<String, Integer> record = getPlayerRecord(player);
        int number = record.getOrDefault(getTypeByEnum(type.KILLS), 0) + 1;
        record.put(getTypeByEnum(type.KILLS), number);
        config.set(player.getName(), record);
        config.save();
    }

    /**
     * 获取击杀数
     * @param player 玩家
     * @return 击杀数
     */
    public static int getKills(Player player) {
        HashMap<String, Integer> record = getPlayerRecord(player);
        return record.getOrDefault(getTypeByEnum(type.KILLS), 0);
    }

    /**
     * 添加死亡数
     * @param player 玩家
     */
    public static void addDeaths(Player player) {
        Config config = GunWar.getInstance().getGameRecord();
        HashMap<String, Integer> record = getPlayerRecord(player);
        int number = record.getOrDefault(getTypeByEnum(type.DEATHS), 0) + 1;
        record.put(getTypeByEnum(type.DEATHS), number);
        config.set(player.getName(), record);
        config.save();
    }

    /**
     * 获取死亡数
     * @param player 玩家
     * @return 死亡数
     */
    public static long getDeaths(Player player) {
        HashMap<String, Integer> record = getPlayerRecord(player);
        return record.getOrDefault(getTypeByEnum(type.DEATHS), 0);
    }

    /**
     * 添加胜利次数
     * @param player 玩家
     */
    public static void addVictory(Player player) {
        Config config = GunWar.getInstance().getGameRecord();
        HashMap<String, Integer> record = getPlayerRecord(player);
        int number = record.getOrDefault(getTypeByEnum(type.VICTORY), 0) + 1;
        record.put(getTypeByEnum(type.VICTORY), number);
        config.set(player.getName(), record);
        config.save();
    }

    /**
     * 获取胜利次数
     * @param player 玩家
     * @return 胜利次数
     */
    public static long getVictory(Player player) {
        HashMap<String, Integer> record = getPlayerRecord(player);
        return record.getOrDefault(getTypeByEnum(type.VICTORY), 0);
    }

    /**
     * 添加失败次数
     * @param player 玩家
     */
    public static void addDefeat(Player player) {
        Config config = GunWar.getInstance().getGameRecord();
        HashMap<String, Integer> record = getPlayerRecord(player);
        int number = record.getOrDefault(getTypeByEnum(type.DEFEAT), 0) + 1;
        record.put(getTypeByEnum(type.DEFEAT), number);
        config.set(player.getName(), record);
        config.save();
    }

    /**
     * 获取失败次数
     * @param player 玩家
     * @return 失败次数
     */
    public static long getDefeat(Player player) {
        HashMap<String, Integer> record = getPlayerRecord(player);
        return record.getOrDefault(getTypeByEnum(type.DEFEAT), 0);
    }

    /**
     * 排行榜
     * @param map 数据
     * @return 排行后的数据
     */
    public static LinkedHashMap<String,Integer> getRankingList(Map<String, Integer> map) {
        LinkedHashMap<String,Integer> map1 = new LinkedHashMap<>();
        Comparator<Map.Entry<String, Integer>> valCmp = (o1, o2) -> {
            return o2.getValue() - o1.getValue();
        };
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(valCmp);
        for(Map.Entry<String,Integer> entry : list){
            map1.put(entry.getKey(), entry.getValue());
        }
/*        map1 = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));*/
        return map1;
    }

    /**
     * 获取排行
     * @param type 排行榜类型
     * @return 击杀数排行
     */
    public static LinkedHashMap<String,Integer> getRankingList(type type) {
        Config config = GunWar.getInstance().getGameRecord();
        Map<String, Object> map = config.getAll();
        HashMap<String, Integer> list = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            list.put(entry.getKey(),  getPlayerRecord(entry.getKey()).getOrDefault(getTypeByEnum(type), 0));
        }
        return getRankingList(list);
    }

    public enum type {
        KILLS,
        DEATHS,
        VICTORY,
        DEFEAT
    }


}
