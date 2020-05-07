package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.Room;
import cn.nukkit.event.player.PlayerEvent;


public abstract class GunWarRoomPlayerEvent extends PlayerEvent {

    protected Room room;

    public Room getRoom() {
        return this.room;
    }

}
