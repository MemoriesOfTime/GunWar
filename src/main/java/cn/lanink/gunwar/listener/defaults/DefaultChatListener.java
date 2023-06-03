package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class DefaultChatListener extends BaseGameListener<BaseRoom> {

    private final GunWar gunWar = GunWar.getInstance();

    /**
     * 玩家执行命令事件
     * @param event 事件
     */
    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player == null || message == null) {
            return;
        }
        String[] split = message.replace("/", "").split(" ");
        if (split.length == 0) {
            return;
        }
        message = split[0];
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (this.gunWar.getCmdUser().equalsIgnoreCase(message) ||
                this.gunWar.getCmdAdmin().equalsIgnoreCase(message)) {
            return;
        }
        for (String string : this.gunWar.getCmdWhitelist()) {
            if (string.equalsIgnoreCase(message)) {
                return;
            }
        }
        event.setMessage("");
        event.setCancelled(true);
        player.sendMessage(this.gunWar.getLanguage().translateString("useCmdInRoom"));
    }

    /**
     * 玩家聊天事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player == null || message == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player)) {
            for (BaseRoom r : this.gunWar.getGameRoomManager().getGameRoomMap().values()) {
                for (Player p : r.getPlayerDataMap().keySet()) {
                    event.getRecipients().remove(p);
                }
            }
            return;
        }
        if (message.startsWith("@") || room.getStatus() != IRoomStatus.ROOM_STATUS_GAME) {
            message = this.gunWar.getLanguage().translateString("playerAllChat", player.getName(), message);
            Tools.sendMessage(room, message);
        }else {
            message = this.gunWar.getLanguage().translateString("playerTeamChat", player.getName(), message);
            Team team = room.getPlayerTeamAccurate(player);
            for (Player target : room.getPlayerDataMap().keySet()) {
                Team targetTeam = room.getPlayerTeamAccurate(target);
                if (team == targetTeam) {
                    target.sendMessage(message);
                } else if (team == Team.RED || team == Team.RED_DEATH) {
                    if (targetTeam == Team.RED || targetTeam == Team.RED_DEATH) {
                        target.sendMessage(message);
                    }
                } else if (team == Team.BLUE || team == Team.BLUE_DEATH) {
                    if (targetTeam == Team.BLUE || targetTeam == Team.BLUE_DEATH) {
                        target.sendMessage(message);
                    }
                }
            }
        }
        event.setMessage("");
        event.setCancelled(true);
    }

}
