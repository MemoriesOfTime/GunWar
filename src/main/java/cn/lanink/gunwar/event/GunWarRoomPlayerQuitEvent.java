package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author LT_Name
 */
public class GunWarRoomPlayerQuitEvent extends GunWarRoomPlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public GunWarRoomPlayerQuitEvent(@NotNull BaseRoom room, @NotNull Player player) {
        this.room = room;
        this.player = player;
    }

}
