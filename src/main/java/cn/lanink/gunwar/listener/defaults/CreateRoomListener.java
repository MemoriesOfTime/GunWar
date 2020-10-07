package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.ui.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class CreateRoomListener implements Listener {

    private final GunWar gunWar;

    public CreateRoomListener(GunWar gunWar) {
        this.gunWar = gunWar;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null) {
            return;
        }
        Block block = event.getBlock();
        if (block.getFloorX() == 0 && block.getFloorY() == 0 && block.getFloorZ() == 0) {
            return;
        }
        String pos = block.getFloorX() + ":" + (block.getFloorY() + 1) + ":" + block.getFloorZ();
        if (this.gunWar.createRoomSchedule.containsKey(player) && item.hasCompoundTag()) {
            Config config = this.gunWar.getRoomConfig(player.getLevel());
            switch (item.getNamedTag().getInt("GunWarItemType")) {
                case 110:
                    this.gunWar.createRoomSchedule.put(player, this.gunWar.createRoomSchedule.get(player) - 10);
                    break;
                case 111:
                    this.gunWar.createRoomSchedule.put(player, this.gunWar.createRoomSchedule.get(player) + 10);
                    break;
                case 113:
                    switch (this.gunWar.createRoomSchedule.get(player)) {
                        case 10:
                            config.set("waitSpawn", pos);
                            this.gunWar.createRoomSchedule.put(player, 20);
                            break;
                        case 20:
                            config.set("redSpawn", pos);
                            this.gunWar.createRoomSchedule.put(player, 30);
                            break;
                        case 30:
                            config.set("blueSpawn", pos);
                            this.gunWar.createRoomSchedule.put(player, 40);
                            GuiCreate.sendAdminTimeMenu(player);
                            break;
                        case 40:
                            GuiCreate.sendAdminTimeMenu(player);
                            break;
                        case 50:
                            GuiCreate.sendAdminPlayersMenu(player);
                            break;
                        case 60:
                            GuiCreate.sendAdminModeMenu(player);
                            break;
                    }
                    break;
            }
            event.setCancelled(true);
        }
    }

}
