package cn.lanink.gunwar.utils.rsnpc;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.variable.BaseVariableV2;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LT_Name
 */
public class RsNpcVariableV2 extends BaseVariableV2 {

    @Override
    public void onUpdate(Player player, RsNpcConfig rsNpcConfig) {
        HashMap<String, Integer> map = new HashMap<>();
        int all = 0;
        for (BaseRoom room : GunWar.getInstance().getGameRoomManager().getGameRoomMap().values()) {
            map.put(room.getGameMode(),
                    map.getOrDefault(room.getGameMode(), 0) + room.getPlayers().size());
            all += room.getPlayers().size();
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            this.addVariable("{GunWarRoomPlayerNumber" + entry.getKey() + "}", entry.getValue().toString());
        }

        HashMap<String, Integer> map1 = new HashMap<>();
        for (BaseRoom room : GunWar.getInstance().getGameRoomManager().getGameRoomMap().values()) {
            map1.put(room.getLevelName(), map1.getOrDefault(room.getLevelName(), 0) + room.getPlayers().size());
        }
        for (Map.Entry<String, Integer> entry : map1.entrySet()) {
            this.addVariable("{GunWarLevelPlayerNumber" + entry.getKey() + "}", entry.getValue().toString());
        }

        this.addVariable("{GunWarRoomPlayerNumberAll}", String.valueOf(all));
    }

}
