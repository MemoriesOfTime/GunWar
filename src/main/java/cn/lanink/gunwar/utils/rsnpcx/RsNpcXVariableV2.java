package cn.lanink.gunwar.utils.rsnpcx;

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
public class RsNpcXVariableV2 extends BaseVariableV2 {

    @Override
    public void onUpdate(Player player, RsNpcConfig rsNpcConfig) {
        HashMap<String, Integer> map = new HashMap<>();
        int all = 0;
        for (BaseRoom room : GunWar.getInstance().getRooms().values()) {
            map.put(room.getGameMode(),
                    map.getOrDefault(room.getGameMode(), 0) + room.getPlayers().size());
            all += room.getPlayers().size();
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            this.addVariable("{GunWarRoomPlayerNumber" + entry.getKey() + "}", entry.getValue().toString());
        }
        this.addVariable("{GunWarRoomPlayerNumberAll}", String.valueOf(all));
    }

}