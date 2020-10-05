package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarPlayerDamageEvent;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.item.weapon.MeleeWeapon;
import cn.lanink.gunwar.item.weapon.ProjectileWeapon;
import cn.lanink.gunwar.listener.base.BaseGameListener;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;

/**
 * @author lt_name
 */
public class DefaultDamageListener extends BaseGameListener {

    private final GunWar gunWar = GunWar.getInstance();

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damagePlayer = (Player) event.getDamager();
            if (damagePlayer == null) return;
            BaseRoom room = this.getListenerRoom(damagePlayer.getLevel());
            if (room == null || !room.isPlaying(damagePlayer)) {
                return;
            }
            if (room.getStatus() == IRoomStatus.ROOM_STATUS_GAME &&
                    room.getPlayers(damagePlayer) != room.getPlayers(player)) {
                if (event instanceof EntityDamageByChildEntityEvent) {
                    Entity entity = ((EntityDamageByChildEntityEvent) event).getChild();
                    switch (ItemManage.getItemType(entity)) {
                        case WEAPON_PROJECTILE:
                            ProjectileWeapon weapon = ItemManage.getProjectileWeapon(entity);
                            if (weapon.getRange() == 0) {
                                GunWarPlayerDamageEvent ev = new GunWarPlayerDamageEvent(
                                        room, player, damagePlayer, (float) weapon.getMaxDamage());
                                Server.getInstance().getPluginManager().callEvent(ev);
                                if (!ev.isCancelled()) {
                                    for (Effect effect : weapon.getEffects()) {
                                        player.addEffect(effect);
                                    }
                                    room.lessHealth(player, damagePlayer, ev.getDamage(), weapon.getKillMessage()
                                            .replace("%damager%", damagePlayer.getName())
                                            .replace("%player%", player.getName()));
                                    return;
                                }
                            }
                            break;
                        case WEAPON_GUN:
                            GunWeapon gunWeapon = ItemManage.getGunWeapon(entity);
                            GunWarPlayerDamageEvent ev = new GunWarPlayerDamageEvent(
                                    room, player, damagePlayer, (float) gunWeapon.getRandomDamage());
                            Server.getInstance().getPluginManager().callEvent(ev);
                            if (!ev.isCancelled()) {
                                for (Effect effect : gunWeapon.getEffects()) {
                                    player.addEffect(effect);
                                }
                                room.lessHealth(player, damagePlayer, ev.getDamage(), gunWeapon.getKillMessage()
                                        .replace("%damager%", damagePlayer.getName())
                                        .replace("%player%", player.getName()));
                                return;
                            }
                            break;
                    }
                }else {
                    Item item = damagePlayer.getInventory().getItemInHand();
                    if (ItemManage.getItemType(item) == ItemManage.ItemType.WEAPON_MELEE) {
                        MeleeWeapon weapon = ItemManage.getMeleeWeapon(item);
                        if (weapon != null && ItemManage.canAttack(damagePlayer, weapon)) {
                            GunWarPlayerDamageEvent ev = new GunWarPlayerDamageEvent(
                                    room, player, damagePlayer, (float) weapon.getRandomDamage());
                            Server.getInstance().getPluginManager().callEvent(ev);
                            if (ev.isCancelled()) {
                                event.setCancelled(true);
                            }else {
                                event.setKnockBack(weapon.getKnockBack());
                                for (Effect effect : weapon.getEffects()) {
                                    player.addEffect(effect);
                                }
                                room.lessHealth(player, damagePlayer, ev.getDamage(), weapon.getKillMessage()
                                        .replace("%damager%", damagePlayer.getName())
                                        .replace("%player%", player.getName()));
                            }
                            return;
                        }
                    }
                }
            }
            event.setCancelled(true);
        }
    }


}
