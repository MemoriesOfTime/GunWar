package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;

public class PlayerGameListener implements Listener {

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
            if (room == null) {
                return;
            }
            if (room.getPlayerMode(player1) != room.getPlayerMode(player2)) {
                int id = event.getChild().getNetworkId();
                if (id == 80) {
                    room.lessHealth(player2, 10F);
                }else if (id == 81){
                    room.lessHealth(player2, 1F);
                }
            }
            event.setDamage(0);
        }
    }

}
