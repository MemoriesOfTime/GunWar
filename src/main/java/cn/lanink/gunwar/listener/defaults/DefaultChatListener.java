package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.listener.base.BaseGameListener;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * @author lt_name
 */
public class DefaultChatListener extends BaseGameListener {

    private final GunWar gunWar = GunWar.getInstance();

    /**
     * 玩家执行命令事件
     * @param event 事件
     */
    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getMessage() == null) return;
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (event.getMessage().startsWith(this.gunWar.getCmdUser(), 1) ||
                event.getMessage().startsWith(this.gunWar.getCmdAdmin(), 1)) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(this.gunWar.getLanguage().useCmdInRoom);
    }

    /**
     * 玩家聊天事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player == null || message == null) return;
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player) || room.getStatus() != 2) {
            return;
        }
        message = this.gunWar.getLanguage().playerTeamChat.replace("%player%", player.getName())
                .replace("%message%", message);
        int team = room.getPlayers(player);
        for (Player p : room.getPlayers().keySet()) {
            if (room.getPlayers(p) == team ||
                    (room.getPlayers(p) - 10 == team) ||
                    (room.getPlayers(p) == team - 10)) {
                p.sendMessage(message);
            }
        }
        event.setMessage("");
        event.setCancelled(true);
    }

}
