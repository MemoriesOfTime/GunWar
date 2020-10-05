package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class GunWarPlayerDamageEvent extends GunWarRoomPlayerEvent  implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player damagePlayer;
    private float damage;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public GunWarPlayerDamageEvent(BaseRoom room, Player player, Player damagePlayer, float damage) {
        this.room = room;
        this.player = player;
        this.damagePlayer = damagePlayer;
        this.damage = damage;
    }

    public Player getDamagePlayer() {
        return this.damagePlayer;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return  this.damage;
    }

}
