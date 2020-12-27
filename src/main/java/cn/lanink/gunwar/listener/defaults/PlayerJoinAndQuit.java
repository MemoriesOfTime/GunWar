package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gamecore.utils.SavePlayerInventory;
import cn.lanink.gamecore.utils.Tips;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.tasks.game.blasting.DemolitionBombTask;
import cn.lanink.gunwar.tasks.game.blasting.PlantBombTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.scheduler.Task;

import java.util.LinkedHashMap;

/**
 * 玩家进入/退出服务器 或传送到其他世界时，退出房间
 */
public class PlayerJoinAndQuit implements Listener {

    private final GunWar gunWar;

    public PlayerJoinAndQuit(GunWar gunWar) {
        this.gunWar = gunWar;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player != null && this.gunWar.getRooms().containsKey(player.getLevel().getFolderName())) {
            Server.getInstance().getScheduler().scheduleDelayedTask(this.gunWar, new Task() {
                @Override
                public void onRun(int i) {
                    if (player.isOnline()) {
                        Tools.rePlayerState(player ,false);
                        if (gunWar.isHasTips()) {
                            Tips.removeTipsConfig(player.getLevel().getName(), player);
                        }
                        SavePlayerInventory.restore(gunWar, player);
                        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                    }
                }
            }, 10);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (this.gunWar.setRoomTask.containsKey(player)) {
            this.gunWar.setRoomTask.get(player).cancel();
        }
        for (BaseRoom room : this.gunWar.getRooms().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player);
            }
        }
        ItemManage.getPlayerAttackTime().remove(player);
        for (GunWeapon weapon : ItemManage.getGunWeaponMap().values()) {
            weapon.getMagazineMap().remove(player);
        }
        PlantBombTask.PLANT_BOMB_PLAYERS.remove(player);
        DemolitionBombTask.DEMOLITION_BOMB_PLAYERS.remove(player);
    }

    @EventHandler
    public void onPlayerTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromLevel = event.getFrom().getLevel() == null ? null : event.getFrom().getLevel().getName();
        String toLevel = event.getTo().getLevel()== null ? null : event.getTo().getLevel().getName();
        if (player == null || fromLevel == null || toLevel == null) return;
        if (!fromLevel.equals(toLevel)) {
            LinkedHashMap<String, BaseRoom> room =  this.gunWar.getRooms();
            if (room.containsKey(fromLevel) && room.get(fromLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(this.gunWar.getLanguage().tpQuitRoomLevel);
            }else if (!player.isOp() && room.containsKey(toLevel) &&
                    !room.get(toLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(this.gunWar.getLanguage().tpJoinRoomLevel);
            }
        }
    }

}
