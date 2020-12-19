package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class GunWarPlayerDeathEvent extends GunWarRoomPlayerEvent  implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Entity damager;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public GunWarPlayerDeathEvent(BaseRoom room, Player player, Entity damager) {
        this.room = room;
        this.player = player;
        this.damager = damager;
    }

    public Entity getDamagePlayer() {
        return this.damager;
    }

}
