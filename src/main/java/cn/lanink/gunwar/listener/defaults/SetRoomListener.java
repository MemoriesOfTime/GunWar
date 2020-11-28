package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.gui.GuiCreate;
import cn.lanink.gunwar.tasks.adminroom.SetRoomTask;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class SetRoomListener implements Listener {

    private final GunWar gunWar;

    public SetRoomListener(GunWar gunWar) {
        this.gunWar = gunWar;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        if (player != null && this.gunWar.setRoomTask.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null) {
            return;
        }
        if (this.gunWar.setRoomTask.containsKey(player) && item.hasCompoundTag()) {
            Block block = event.getBlock();
            if (block.getFloorX() == 0 && block.getFloorY() == 0 && block.getFloorZ() == 0) {
                return;
            }
            Config config = this.gunWar.getRoomConfig(player.getLevel());
            SetRoomTask task = this.gunWar.setRoomTask.get(player);
            switch (item.getNamedTag().getInt("GunWarItemType")) {
                case 110: //上一步
                    switch (task.getSetRoomSchedule()) {
                        case 10:
                            break;
                        case 50:
                            if (task.isAutoNext()) {
                                config.remove("waitTime");
                                config.remove("gameTime");
                                config.remove("victoryScore");
                            }
                            task.setRoomSchedule(40);
                            break;
                        case 60:
                            if (task.isAutoNext()) {
                                config.remove("minPlayers");
                                config.remove("maxPlayers");
                            }
                            task.setRoomSchedule(50);
                            break;
                        default:
                            task.setRoomSchedule(task.getSetRoomSchedule() - 10);
                            break;
                    }
                    break;
                case 111: //下一步
                    task.setRoomSchedule(task.getSetRoomSchedule() + 10);
                    break;
                case 112: //保存设置
                    task.setRoomSchedule(70);
                    break;
                case 113: //设置
                    String pos = block.getFloorX() + ":" + (block.getFloorY() + 1) + ":" + block.getFloorZ();
                    switch (task.getSetRoomSchedule()) {
                        case 10:
                            config.set("waitSpawn", pos);
                            task.setRoomSchedule(20);
                            break;
                        case 20:
                            config.set("redSpawn", pos);
                            player.sendMessage(this.gunWar.getLanguage().adminSetRedSpawn);
                            task.setRoomSchedule(30);
                            break;
                        case 30:
                            config.set("blueSpawn", pos);
                            player.sendMessage(this.gunWar.getLanguage().adminSetBlueSpawn);
                            task.setRoomSchedule(40);
                            if (task.isAutoNext()) {
                                GuiCreate.sendAdminTimeMenu(player);
                            }
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
