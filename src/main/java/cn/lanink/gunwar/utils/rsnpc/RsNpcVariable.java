package cn.lanink.gunwar.utils.rsnpc;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import com.smallaswater.npc.variable.BaseVariable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lt_name
 */
public class RsNpcVariable extends BaseVariable {

    @Override
    public String stringReplace(Player player, String s) {
        HashMap<String, Integer> map = new HashMap<>();
        int all = 0;
        for (BaseRoom room : GunWar.getInstance().getGameRoomManager().getGameRoomMap().values()) {
            map.put(room.getGameMode(),
                    map.getOrDefault(room.getGameMode(), 0) + room.getPlayers().size());
            all += room.getPlayers().size();
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            s = s.replace("{GunWarRoomPlayerNumber" + entry.getKey() + "}", entry.getValue() + "");
        }
        
        HashMap<String, Integer> map1 = new HashMap<>();
        for (BaseRoom room : GunWar.getInstance().getGameRoomManager().getGameRoomMap().values()) {
                map1.put(room.getLevelName(), map1.getOrDefault(room.getLevelName(), 0) + room.getPlayers().size());
        }
        for (Map.Entry<String, Integer> entry : map1.entrySet()) {
            s = s.replace("{GunWarLevelPlayerNumber" + entry.getKey() + "}", entry.getValue() + "");
        }
        
        return s.replace("{GunWarRoomPlayerNumberAll}", all + "");
    }

}
