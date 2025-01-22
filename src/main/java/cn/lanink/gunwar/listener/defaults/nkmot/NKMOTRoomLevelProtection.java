package cn.lanink.gunwar.listener.defaults.nkmot;

import cn.lanink.gunwar.listener.defaults.RoomLevelProtection;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.ItemFrameUseEvent;
import cn.nukkit.level.Level;

/**
 * @author LT_Name
 */
public class NKMOTRoomLevelProtection extends RoomLevelProtection {

    /**
     * 物品展示框操作事件
     * @param event 事件
     */
    @EventHandler
    public void onItemFrameUse(ItemFrameUseEvent event) {
        Level level = event.getItemFrame().getLevel();
        if (level != null && this.getListenerRooms().containsKey(level.getFolderName())) {
            event.setCancelled();
        }
    }

}
