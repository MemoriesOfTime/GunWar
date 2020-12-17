package cn.lanink.gunwar.listener.blasting;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.listener.base.BaseGameListener;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.tasks.game.blasting.PlantBombTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;

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
            if ((player.distance(room.getBlastingPointA()) < room.getBlastingPointRadius() &&
                    block.distance(room.getBlastingPointA()) < room.getBlastingPointRadius()) ||
                    (player.distance(room.getBlastingPointB()) < room.getBlastingPointRadius() &&
                            block.distance(room.getBlastingPointB()) < room.getBlastingPointRadius())) {
                if (PlantBombTask.PLANT_BOMB_PLAYERS.contains(player)) {
                    PlantBombTask.PLANT_BOMB_PLAYERS.remove(player);
                }else {
                    Server.getInstance().getScheduler().scheduleRepeatingTask(GunWar.getInstance(),
                            new PlantBombTask(room, player, block, PlantBombTask.maxPlacementProgress / room.getPlantBombTime()), 1, true);
                }
            }
        }

    }


}