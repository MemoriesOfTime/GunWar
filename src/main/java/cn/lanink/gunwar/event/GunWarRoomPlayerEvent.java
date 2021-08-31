package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.event.player.PlayerEvent;

/**
 * @author LT_Name
 */
public abstract class GunWarRoomPlayerEvent extends PlayerEvent {

    protected BaseRoom room;

    public BaseRoom getRoom() {
        return this.room;
    }

}
