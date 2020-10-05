package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class GunWarPlayerDeathEvent extends GunWarRoomPlayerEvent  implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player damagePlayer;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public GunWarPlayerDeathEvent(BaseRoom room, Player player, Player damagePlayer) {
        this.room = room;
        this.player = player;
        this.damagePlayer = damagePlayer;
    }

    public Player getDamagePlayer() {
        return this.damagePlayer;
    }

}
