package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.Room;
import cn.nukkit.event.Event;

public abstract class GunWarRoomEvent extends Event {

    protected Room room;

    public Room getRoom() {
        return this.room;
    }

}
