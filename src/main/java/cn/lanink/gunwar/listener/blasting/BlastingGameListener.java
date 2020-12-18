package cn.lanink.gunwar.listener.blasting;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityGunWarBombBlock;
import cn.lanink.gunwar.listener.base.BaseGameListener;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.tasks.game.blasting.DemolitionBombTask;
import cn.lanink.gunwar.tasks.game.blasting.PlantBombTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;

/**
 * @author lt_name
 */
public class BlastingGameListener extends BaseGameListener<BlastingModeRoom> {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null || block.getId() == BlockID.AIR) {
            return;
        }
        BlastingModeRoom room = this.getListenerRoom(player.getLevel());
        if (room == null) {
            return;
        }
        Item item = player.getInventory().getItemInHand();
        if (item.hasCompoundTag() && item.getNamedTag().getInt("GunWarItemType") == 201) {
            event.setCancelled(true);
            Vector3 placePoint = block.clone();
            placePoint.y += 1;
            if (player.getLevel().getBlock(placePoint).getId() != BlockID.AIR) {
                return;
            }
            if ((player.distance(room.getBlastingPointA()) < room.getBlastingPointRadius() &&
                    placePoint.distance(room.getBlastingPointA()) < room.getBlastingPointRadius()) ||
                    (player.distance(room.getBlastingPointB()) < room.getBlastingPointRadius() &&
                            placePoint.distance(room.getBlastingPointB()) < room.getBlastingPointRadius())) {
                if (PlantBombTask.PLANT_BOMB_PLAYERS.contains(player)) {
                    PlantBombTask.PLANT_BOMB_PLAYERS.remove(player);
                }else {
                    Server.getInstance().getScheduler().scheduleRepeatingTask(GunWar.getInstance(),
                            new PlantBombTask(room, player, placePoint,
                                    PlantBombTask.MAX_PLACEMENT_PROGRESS / (room.getPlantBombTime() * 1D)),
                            1, true);
                }
            }
        }
    }

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof EntityGunWarBombBlock &&
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            Player player = (Player) event.getDamager();
            if (player == null) return;
            BlastingModeRoom room = this.getListenerRoom(player.getLevel());
            if (room == null || !room.isPlaying(player)) {
                return;
            }
            if (room.getPlayers(player) == 2 && room.getDemolitionBombPlayer() == null) {
                room.setDemolitionBombPlayer(player);
                Server.getInstance().getScheduler().scheduleRepeatingTask(GunWar.getInstance(),
                        new DemolitionBombTask(room, player,
                                DemolitionBombTask.MAX_DEMOLITION_PROGRESS / (room.getDemolitionBombTime() * 1D)),
                        1, true);
            }
            event.setCancelled(true);
        }
    }


}
