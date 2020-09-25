package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.event.GunWarPlayerDamageEvent;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.item.weapon.MeleeWeapon;
import cn.lanink.gunwar.item.weapon.ProjectileWeapon;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;

/**
 * @author lt_name
 */
public class PlayerDamageListener implements Listener {

    private final GunWar gunWar;

    public PlayerDamageListener(GunWar gunWar) {
        this.gunWar = gunWar;
    }

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damagePlayer = (Player) event.getDamager();
            if (damagePlayer == null) return;
            Room room = this.gunWar.getRooms().getOrDefault(damagePlayer.getLevel().getFolderName(), null);
            if (room == null || !room.isPlaying(damagePlayer)) return;
            if (event.getEntity() instanceof EntityFlag && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                EntityFlag entityFlag = (EntityFlag) event.getEntity();
                int team = entityFlag.namedTag.getInt("GunWarTeam");
                if (team == 11 && room.getPlayers(damagePlayer) == 2) {
                    room.haveRedFlag = damagePlayer;
                }else if (team == 12 && room.getPlayers(damagePlayer) == 1) {
                    room.haveBlueFlag = damagePlayer;
                }
            }else if (event.getEntity() instanceof EntityFlagStand && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                EntityFlagStand entityFlagStand = (EntityFlagStand) event.getEntity();
                int team = entityFlagStand.namedTag.getInt("GunWarTeam");
                if (team == room.getPlayers(damagePlayer)) {
                    switch (team) {
                        case 1:
                            if (room.haveBlueFlag == damagePlayer) {
                                room.blueFlag.teleport(new Vector3(room.getBlueSpawn().getX(),
                                        room.getBlueSpawn().getY() + 0.3D,
                                        room.getBlueSpawn().getZ()));
                                room.redScore++;
                                room.haveBlueFlag = null;
                                Tools.addSound(room, Sound.RANDOM_LEVELUP);
                            }
                            break;
                        case 2:
                            if (room.haveRedFlag == damagePlayer) {
                                room.redFlag.teleport(new Vector3(room.getRedSpawn().getX(),
                                        room.getRedSpawn().getY() + 0.3D,
                                        room.getRedSpawn().getZ()));
                                room.blueScore++;
                                room.haveRedFlag = null;
                                Tools.addSound(room, Sound.RANDOM_LEVELUP);
                            }
                            break;
                    }
                }
            }else if ((event.getEntity() instanceof Player)) {
                Player player = (Player) event.getEntity();
                if (room.getStatus() == 2 && room.getPlayers(damagePlayer) != room.getPlayers(player)) {
                    if (event instanceof EntityDamageByChildEntityEvent) {
                        Entity entity = ((EntityDamageByChildEntityEvent) event).getChild();
                        switch (ItemManage.getItemType(entity)) {
                            case PROJECTILE_WEAPON:
                                ProjectileWeapon weapon = ItemManage.getProjectileWeapon(entity);
                                if (weapon.getRange() == 0) {
                                    GunWarPlayerDamageEvent ev = new GunWarPlayerDamageEvent(
                                            room, player, damagePlayer, (float) weapon.getMaxDamage());
                                    Server.getInstance().getPluginManager().callEvent(ev);
                                    if (!ev.isCancelled()) {
                                        if (room.lessHealth(player, damagePlayer, ev.getDamage()) < 1) {
                                            Tools.sendMessage(room, weapon.getKillMessage()
                                                    .replace("%damager%", damagePlayer.getName())
                                                    .replace("%player%", player.getName()));
                                        }
                                        return;
                                    }
                                }
                                break;
                            case GUN_WEAPON:
                                GunWeapon gunWeapon = ItemManage.getGunWeapon(entity);
                                GunWarPlayerDamageEvent ev = new GunWarPlayerDamageEvent(
                                        room, player, damagePlayer, (float) gunWeapon.getRandomDamage());
                                Server.getInstance().getPluginManager().callEvent(ev);
                                if (!ev.isCancelled()) {
                                    if (room.lessHealth(player, damagePlayer, ev.getDamage()) < 1) {
                                        Tools.sendMessage(room, gunWeapon.getKillMessage()
                                                .replace("%damager%", damagePlayer.getName())
                                                .replace("%player%", player.getName()));
                                    }
                                    return;
                                }
                                break;
                        }
                    }else {
                        Item item = damagePlayer.getInventory().getItemInHand();
                        if (ItemManage.getItemType(item) == ItemManage.ItemType.MELEE_WEAPON) {
                            MeleeWeapon weapon = ItemManage.getMeleeWeapon(item);
                            if (weapon != null && ItemManage.canAttack(damagePlayer, weapon)) {
                                GunWarPlayerDamageEvent ev = new GunWarPlayerDamageEvent(
                                        room, player, damagePlayer, (float) weapon.getRandomDamage());
                                Server.getInstance().getPluginManager().callEvent(ev);
                                if (ev.isCancelled()) {
                                    event.setCancelled(true);
                                }else {
                                    event.setKnockBack(weapon.getKnockBack());
                                    if (room.lessHealth(player, damagePlayer, ev.getDamage()) < 1) {
                                        Tools.sendMessage(room, weapon.getKillMessage()
                                                .replace("%damager%", damagePlayer.getName())
                                                .replace("%player%", player.getName()));
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    /**
     * 伤害事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Room room = this.gunWar.getRooms().get(player.getLevel().getFolderName());
            if (room == null || !room.isPlaying(player)) return;
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                    event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
                this.gunWar.getServer().getPluginManager().callEvent(
                        new GunWarPlayerDamageEvent(room, player, player, event.getDamage()));
            }
        }else if (event.getEntity() instanceof EntityPlayerCorpse ||
                event.getEntity() instanceof EntityFlagStand ||
                event.getEntity() instanceof EntityFlag) {
            event.setCancelled(true);
        }
    }


}
