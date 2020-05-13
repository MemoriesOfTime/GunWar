package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.SavePlayerInventory;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player != null && GunWar.getInstance().getRooms().containsKey(player.getLevel().getName())) {
            GunWar.getInstance().getServer().getScheduler().scheduleDelayedTask(new Task() {
                @Override
                public void onRun(int i) {
                    if (player.isOnline()) {
                        Tools.rePlayerState(player ,false);
                        SavePlayerInventory.restore(player);
                        player.teleport(GunWar.getInstance().getServer().getDefaultLevel().getSafeSpawn());
                    }
                }
            }, 120);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        for (Room room : GunWar.getInstance().getRooms().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player, false);
            }
        }
    }

    @EventHandler
    public void onPlayerTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromLevel = event.getFrom().getLevel() == null ? null : event.getFrom().getLevel().getName();
        String toLevel = event.getTo().getLevel()== null ? null : event.getTo().getLevel().getName();
        if (player == null || fromLevel == null || toLevel == null) return;
        if (!fromLevel.equals(toLevel)) {
            LinkedHashMap<String, Room> room =  GunWar.getInstance().getRooms();
            if (room.containsKey(fromLevel) && room.get(fromLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(GunWar.getInstance().getLanguage().tpQuitRoomLevel);
            }else if (!player.isOp() && room.containsKey(toLevel) &&
                    !room.get(toLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(GunWar.getInstance().getLanguage().tpJoinRoomLevel);
            }
        }
    }

}
