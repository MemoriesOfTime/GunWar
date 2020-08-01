package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.event.GunWarPlayerDamageEvent;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;

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
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damagePlayer = (Player) event.getDamager();
            if (damagePlayer == null) return;
            Room room = this.gunWar.getRooms().getOrDefault(damagePlayer.getLevel().getName(), null);
            if (room == null || !room.isPlaying(damagePlayer)) return;
            if (event.getEntity() instanceof EntityFlag && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                EntityFlag entityFlag = (EntityFlag) event.getEntity();
                int team = entityFlag.namedTag.getInt("GunWarTeam");
                if (team == 11 && room.getPlayerMode(damagePlayer) == 2) {
                    room.haveRedFlag = damagePlayer;
                }else if (team == 12 && room.getPlayerMode(damagePlayer) == 1) {
                    room.haveBlueFlag = damagePlayer;
                }
            }else if (event.getEntity() instanceof EntityFlagStand && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                EntityFlagStand entityFlagStand = (EntityFlagStand) event.getEntity();
                int team = entityFlagStand.namedTag.getInt("GunWarTeam");
                if (team == room.getPlayerMode(damagePlayer)) {
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
                if (room.getMode() == 2 && room.getPlayerMode(damagePlayer) != room.getPlayerMode(player)) {
                    if (event instanceof EntityDamageByChildEntityEvent) {
                        int id = ((EntityDamageByChildEntityEvent) event).getChild().getNetworkId();
                        if (id == 80) {
                            Server.getInstance().getPluginManager().callEvent(
                                    new GunWarPlayerDamageEvent(room, player, damagePlayer, 10F));
                            return;
                        } else if (id == 81) {
                            Server.getInstance().getPluginManager().callEvent(
                                    new GunWarPlayerDamageEvent(room, player, damagePlayer, 2F));
                            return;
                        }
                    }else {
                        int id = damagePlayer.getInventory().getItemInHand() == null ? 0 : damagePlayer.getInventory().getItemInHand().getId();
                        if (id == 272 && !room.swordAttackCD.contains(player)) {
                            room.swordAttackCD.add(player);
                            this.gunWar.getServer().getPluginManager().callEvent(
                                    new GunWarPlayerDamageEvent(room, player, damagePlayer, 2F));
                            this.gunWar.getServer().getScheduler().scheduleDelayedTask(this.gunWar, new Task() {
                                @Override
                                public void onRun(int i) {
                                    while (room.swordAttackCD.contains(player)) {
                                        room.swordAttackCD.remove(player);
                                    }
                                }
                            }, 20);
                            return;
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
            Room room = this.gunWar.getRooms().get(player.getLevel().getName());
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
