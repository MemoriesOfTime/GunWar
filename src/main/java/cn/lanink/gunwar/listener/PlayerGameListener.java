package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;

public class PlayerGameListener implements Listener {

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getCause() == null || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player player1 = (Player) event.getDamager();
            Player player2 = (Player) event.getEntity();
            if (player1 == null || player2 == null) {
                return;
            }
            Room room = GunWar.getInstance().getRooms().getOrDefault(player1.getLevel().getName(), null);
            if (room == null || !room.isPlaying(player1) || !room.isPlaying(player2)) {
                return;
            }
            if (room.getMode() == 2) {
                if (room.isPlaying(player1) && room.isPlaying(player2) &&
                        room.getPlayerMode(player1) != room.getPlayerMode(player2)) {
                    int id = player1.getInventory().getItemInHand() == null ? 0 : player1.getInventory().getItemInHand().getId();
                    if (id == 272) {
                        room.lessHealth(player2, 2F);
                        return;
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    /**
     * 实体受到另一个子实体伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onDamageByChild(EntityDamageByChildEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player1 = ((Player) event.getDamager()).getPlayer();
            Player player2 = ((Player) event.getEntity()).getPlayer();
            if (player1 == player2 || event.getChild() == null) {
                return;
            }
            Room room = GunWar.getInstance().getRooms().getOrDefault(player1.getLevel().getName(), null);
            if (room == null || !room.isPlaying(player1) || !room.isPlaying(player2)) {
                return;
            }
            if (room.getMode() == 2 && room.getPlayerMode(player1) != room.getPlayerMode(player2)) {
                int id = event.getChild().getNetworkId();
                if (id == 80) {
                    room.lessHealth(player2, 10F);
                    return;
                } else if (id == 81) {
                    room.lessHealth(player2, 1F);
                    return;
                }
            }
            event.setCancelled(true);
        }
    }

}
