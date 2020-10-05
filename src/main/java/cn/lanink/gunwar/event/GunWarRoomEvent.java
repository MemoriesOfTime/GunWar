package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.event.Event;

public abstract class GunWarRoomEvent extends Event {

    protected BaseRoom room;

    public BaseRoom getRoom() {
        return this.room;
    }

}
