package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.gui.GuiCreate;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.tasks.adminroom.SetRoomTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

import java.util.HashSet;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class SetRoomListener implements Listener {

    private final GunWar gunWar;
    private final HashSet<Player> playerClickCooldown = new HashSet<>();

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

            //防止win10玩家重复触发
            if (this.playerClickCooldown.contains(player)) {
                return;
            }
            this.playerClickCooldown.add(player);
            Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, () -> this.playerClickCooldown.remove(player), 20);

            Config config = this.gunWar.getRoomConfig(player.getLevel());
            SetRoomTask task = this.gunWar.setRoomTask.get(player);
            switch (item.getNamedTag().getInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG)) {
                case 110: //上一步
                    //部分配置需要在返回上一步后移除，否则返回上一步后会再次跳过
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
                        default:
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
                            player.sendMessage(this.gunWar.getLanguage().translateString("adminSetRedSpawn"));
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        case 30:
                            config.set("blueSpawn", pos);
                            player.sendMessage(this.gunWar.getLanguage().translateString("adminSetBlueSpawn"));
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            if (task.isAutoNext()) {
                                GuiCreate.sendAdminTimeMenu(player);
                            }
                            break;
                        case 40:
                            GuiCreate.sendAdminTimeMenu(player);
                            break;
                        case 45:
                            GuiCreate.sendAdminShopMenu(player);
                            break;
                        case 50:
                            GuiCreate.sendAdminPlayersMenu(player);
                            break;
                        case 60:
                            GuiCreate.sendAdminModeMenu(player);
                            break;
                        case 200:
                            config.set("blastingPointA", pos);
                            player.sendMessage(this.gunWar.getLanguage().translateString("admin_setRoom_setBlastingPointSuccessful", "§cA"));
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        case 210:
                            config.set("blastingPointB", pos);
                            player.sendMessage(this.gunWar.getLanguage().translateString("admin_setRoom_setBlastingPointSuccessful", "§9B"));
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
