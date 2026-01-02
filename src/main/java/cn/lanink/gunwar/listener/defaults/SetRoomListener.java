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
import java.util.List;

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
                        case 45:
                            if (task.isAutoNext()) {
                                config.remove("waitTime");
                                config.remove("gameTime");
                                config.remove("victoryScore");
                            }
                            break;
                        case 50:
                            if (task.isAutoNext()) {
                                config.remove("supplyType");
                                config.remove("supplyEnableTime");
                            }
                            break;
                        case 530:
                            if (task.isAutoNext()) {
                                config.remove("actionAttackerInitialResource");
                                config.remove("actionResourceReward");
                                config.remove("enableCameraAnimation");
                                config.remove("enableOvertime");
                                config.remove("overtimeResource");
                                config.remove("overtimeTime");
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
                            task.setRoomSchedule(15);
                            break;
                        case 15:
                            GuiCreate.sendAdminModeMenu(player);
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
                        case 300:
                            List<String> randomSpawns = config.getStringList("randomSpawns");
                            randomSpawns.add(pos);
                            config.set("randomSpawns", randomSpawns);
                            player.sendMessage(this.gunWar.getLanguage().translateString("admin_setRoom_addRandomSpawnSuccessful"));
                            break;
                        case 400:
                            config.set("ConquestPointA", pos);
                            player.sendMessage(this.gunWar.getLanguage().translateString("admin_setRoom_setConquestPointSuccessful", "§eA"));
                            break;
                        case 410:
                            config.set("ConquestPointB", pos);
                            player.sendMessage(this.gunWar.getLanguage().translateString("admin_setRoom_setConquestPointSuccessful", "§eB"));
                            break;
                        case 420:
                            config.set("ConquestPointC", pos);
                            player.sendMessage(this.gunWar.getLanguage().translateString("admin_setRoom_setConquestPointSuccessful", "§eC"));
                            break;
                        case 500: //行动模式 添加区域A控制点
                            List<String> zoneAControlPoints = config.getStringList("actionZoneA_controlPoints");
                            zoneAControlPoints.add(pos);
                            config.set("actionZoneA_controlPoints", zoneAControlPoints);
                            player.sendMessage("§a成功添加 §e区域A §6控制点");
                            break;
                        case 501: //行动模式 设置区域A防守方重生点
                            config.set("actionZoneA_defenderSpawn", pos);
                            player.sendMessage("§a成功设置 §e区域A §9防守方重生点");
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        case 510: //行动模式 添加区域B控制点
                            List<String> zoneBControlPoints = config.getStringList("actionZoneB_controlPoints");
                            zoneBControlPoints.add(pos);
                            config.set("actionZoneB_controlPoints", zoneBControlPoints);
                            player.sendMessage("§a成功添加 §e区域B §6控制点");
                            break;
                        case 511: //行动模式 设置区域B防守方重生点
                            config.set("actionZoneB_defenderSpawn", pos);
                            player.sendMessage("§a成功设置 §e区域B §9防守方重生点");
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        case 520: //行动模式 添加区域C控制点
                            List<String> zoneCControlPoints = config.getStringList("actionZoneC_controlPoints");
                            zoneCControlPoints.add(pos);
                            config.set("actionZoneC_controlPoints", zoneCControlPoints);
                            player.sendMessage("§a成功添加 §e区域C §6控制点");
                            break;
                        case 521: //行动模式 设置区域C防守方重生点
                            config.set("actionZoneC_defenderSpawn", pos);
                            player.sendMessage("§a成功设置 §e区域C §9防守方重生点");
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        case 530: //行动模式 配置参数
                            GuiCreate.sendAdminActionConfigMenu(player);
                            break;
                        default:
                            break;
                    }
                    break;
                case 114: //删除已设置的点
                    String removePos = block.getFloorX() + ":" + (block.getFloorY() + 1) + ":" + block.getFloorZ();
                    switch (task.getSetRoomSchedule()) {
                        case 300:
                            List<String> randomSpawns = config.getStringList("randomSpawns");
                            randomSpawns.remove(removePos);
                            config.set("randomSpawns", randomSpawns);
                            player.sendMessage(this.gunWar.getLanguage().translateString("admin_setRoom_removeRandomSpawnSuccessful"));
                            break;
                        case 500: //行动模式 删除区域A控制点
                            List<String> zoneAControlPoints = config.getStringList("actionZoneA_controlPoints");
                            zoneAControlPoints.remove(removePos);
                            config.set("actionZoneA_controlPoints", zoneAControlPoints);
                            player.sendMessage("§a成功删除 §e区域A §6控制点");
                            break;
                        case 510: //行动模式 删除区域B控制点
                            List<String> zoneBControlPoints = config.getStringList("actionZoneB_controlPoints");
                            zoneBControlPoints.remove(removePos);
                            config.set("actionZoneB_controlPoints", zoneBControlPoints);
                            player.sendMessage("§a成功删除 §e区域B §6控制点");
                            break;
                        case 520: //行动模式 删除区域C控制点
                            List<String> zoneCControlPoints = config.getStringList("actionZoneC_controlPoints");
                            zoneCControlPoints.remove(removePos);
                            config.set("actionZoneC_controlPoints", zoneCControlPoints);
                            player.sendMessage("§a成功删除 §e区域C §6控制点");
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
