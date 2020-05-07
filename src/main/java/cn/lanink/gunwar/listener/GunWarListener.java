package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarRoomAssignTeamEvent;
import cn.lanink.gunwar.event.GunWarRoomRoundEndEvent;
import cn.lanink.gunwar.event.GunWarRoomStartEvent;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;

public class GunWarListener implements Listener {

    /**
     * 房间开始事件
     * @param event 事件
     */
    @EventHandler
    public void onRoomStart(GunWarRoomStartEvent event) {
        Room room = event.getRoom();
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomAssignTeamEvent(room));




    }

    /**
     * 房间回合结束事件
     * @param event 事件
     */
    @EventHandler
    public void onRoundEnd(GunWarRoomRoundEndEvent event) {

    }

}
