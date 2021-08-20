package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.event.HandlerList;

public class GunWarRoomStartEvent extends GunWarRoomEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public GunWarRoomStartEvent(BaseRoom room) {
        this.room = room;
    }

}
