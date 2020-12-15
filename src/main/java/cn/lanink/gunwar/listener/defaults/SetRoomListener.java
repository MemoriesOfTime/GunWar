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
            event.setCancelled(true);
            Config config = this.gunWar.getRoomConfig(player.getLevel());
            SetRoomTask task = this.gunWar.setRoomTask.get(player);
            switch (item.getNamedTag().getInt("GunWarItemType")) {
                case 110: //上一步
                    switch (task.getSetRoomSchedule()) {
                        case 10:
                            return;
                        case 50:
                            if (task.isAutoNext()) {
                                config.remove("waitTime");
                                config.remove("gameTime");
                                config.remove("victoryScore");
                            }
                            break;
                        case 60:
                            if (task.isAutoNext()) {
                                config.remove("minPlayers");
                                config.remove("maxPlayers");
                            }
                            break;
                    }
                    task.setRoomSchedule(task.getBackRoomSchedule());
                    break;
                case 111: //下一步
                    task.setRoomSchedule(task.getNextRoomSchedule());
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
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        case 30:
                            config.set("blueSpawn", pos);
                            player.sendMessage(this.gunWar.getLanguage().adminSetBlueSpawn);
                            task.setRoomSchedule(task.getNextRoomSchedule());
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
                        case 200:
                            config.set("blastingPointA", pos);
                            player.sendMessage("爆破点A已设置");
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        case 210:
                            config.set("blastingPointB", pos);
                            player.sendMessage("爆破点B已设置");
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                    }
                    break;
            }
        }
    }

}
