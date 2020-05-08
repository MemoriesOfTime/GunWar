package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.Room;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class GunWarPlayerCorpseSpawnEvent extends GunWarRoomPlayerEvent  implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public GunWarPlayerCorpseSpawnEvent(Room room, Player player) {
        this.room = room;
        this.player = player;
    }

}
