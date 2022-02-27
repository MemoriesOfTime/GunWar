package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarPlayerDamageEvent;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.item.weapon.MeleeWeapon;
import cn.lanink.gunwar.item.weapon.ProjectileWeapon;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.potion.Effect;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class DefaultDamageListener extends BaseGameListener<BaseRoom> {

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damagePlayer = (Player) event.getDamager();
            BaseRoom room = this.getListenerRoom(damagePlayer.getLevel());
            if (room == null) {
                return;
            }else if (!room.isPlaying(player) || !room.isPlaying(damagePlayer)) {
                event.setCancelled(true);
                return;
            }
            if (room.getStatus() == IRoomStatus.ROOM_STATUS_GAME &&
                    room.getPlayers(damagePlayer) != room.getPlayers(player)) {
                if (event instanceof EntityDamageByChildEntityEvent) {
                    Entity entity = ((EntityDamageByChildEntityEvent) event).getChild();
                    switch (ItemManage.getItemType(entity)) {
                        case WEAPON_PROJECTILE:
                            ProjectileWeapon weapon = ItemManage.getProjectileWeapon(entity);
                            //有效半径大于0的不在这里判断
                            if (weapon.getRange() <= 0) {
                                GunWarPlayerDamageEvent ev = new GunWarPlayerDamageEvent(
                                        room, player, damagePlayer, (float) weapon.getMaxDamage());
                                Server.getInstance().getPluginManager().callEvent(ev);
                                if (!ev.isCancelled()) {
                                    for (Effect effect : weapon.getEffects()) {
                                        player.addEffect(effect);
                                    }
                                    for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                                        event.setDamage(0, modifier);
                                    }
                                    room.lessHealth(player,
                                            damagePlayer,
                                            ev.getDamage(),
                                            weapon.getKillMessage()
                                                    .replace("%damager%", damagePlayer.getName())
                                                    .replace("%player%", player.getName())
                                    );
                                }else {
                                    event.setCancelled(true);
                                }
                                return;
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
                                for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                                    event.setDamage(0, modifier);
                                }
                                room.lessHealth(player,
                                        damagePlayer,
                                        ev.getDamage(),
                                        gunWeapon.getKillMessage()
                                                .replace("%damager%", damagePlayer.getName())
                                                .replace("%player%", player.getName())
                                );
                            }else {
                                event.setCancelled(true);
                            }
                            return;
                        default:
                            break;
                    }
                }else {
                    Item item = damagePlayer.getInventory().getItemInHand();
                    if (ItemManage.getItemType(item) == ItemManage.ItemType.WEAPON_MELEE) {
                        MeleeWeapon weapon = ItemManage.getMeleeWeapon(item);
                        if (weapon != null) {
                            if (!ItemManage.canAttack(damagePlayer, weapon)) {
                                event.setCancelled(true);
                                return;
                            }
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
                                for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                                    event.setDamage(0, modifier);
                                }
                                room.lessHealth(player, damagePlayer, ev.getDamage(), weapon.getKillMessage()
                                        .replace("%damager%", damagePlayer.getName())
                                        .replace("%player%", player.getName()));
                            }
                            return;
                        }
                    }
                }

                if (GunWar.getInstance().isEnableOtherWeaponDamage()) {
                    room.lessHealth(player, damagePlayer, event.getFinalDamage());
                    return;
                }
            }

            event.setCancelled(true);
        }
    }


}
