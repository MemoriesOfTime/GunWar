package cn.lanink.gunwar.utils.gamerecord;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.*;

/**
 * @author lt_name
 */
public class GameRecord {

    public static LinkedHashMap<String, Integer> getRankingList(RecordType recordType) {
        Config config = GunWar.getInstance().getGameRecord();
        Map<String, Object> map = config.getAll();
        HashMap<String, Integer> list = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            list.put(entry.getKey(), getPlayerRecord(entry.getKey()).getOrDefault(recordType.getName(), 0));
        }
        return getRankingList(list);
    }

    public static HashMap<String, Integer> getPlayerRecord(Player player) {
        return getPlayerRecord(player.getName());
    }

    public static HashMap<String, Integer> getPlayerRecord(String player) {
        return GunWar.getInstance().getGameRecord().get(player, new HashMap<>());
    }

    public static void addPlayerRecord(Player player, RecordType recordType) {
        addPlayerRecord(player.getName(), recordType);
    }

    public static void addPlayerRecord(String player, RecordType recordType) {
        Config config = GunWar.getInstance().getGameRecord();
        HashMap<String, Integer> record = getPlayerRecord(player);
        record.put(recordType.getName(), getPlayerRecord(player, recordType) + 1);
        config.set(player, record);
        config.save();
    }

    public static int getPlayerRecord(Player player, RecordType recordType) {
        return getPlayerRecord(player.getName(), recordType);
    }

    public static int getPlayerRecord(String player, RecordType recordType) {
        return getPlayerRecord(player).getOrDefault(recordType.getName(), 0);
    }

    /**
     * 排行榜
     *
     * @param map 数据
     * @return 排行后的数据
     */
    public static LinkedHashMap<String, Integer> getRankingList(Map<String, Integer> map) {
        LinkedHashMap<String, Integer> map1 = new LinkedHashMap<>();
        List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue() - o1.getValue());
        for (Map.Entry<String, Integer> entry : list) {
            map1.put(entry.getKey(), entry.getValue());
        }
/*        map1 = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));*/
        return map1;
    }

}
