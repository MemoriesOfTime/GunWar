package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.Room;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class GunWarRoomAssignTeamEvent extends GunWarRoomEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public GunWarRoomAssignTeamEvent(Room room) {
        this.room = room;
    }

}
