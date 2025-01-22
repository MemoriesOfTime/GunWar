package cn.lanink.gunwar.listener.defaults.nkpm1e;

import cn.lanink.gunwar.listener.defaults.RoomLevelProtection;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.ItemFrameDropItemEvent;
import cn.nukkit.level.Level;

/**
 * @author LT_Name
 */
public class PM1ERoomLevelProtection extends RoomLevelProtection {

    /**
     * 物品展示框丢出事件
     * @param event 事件
     */
    @EventHandler
    public void onFrameDropItem(ItemFrameDropItemEvent event) {
        Level level = event.getItemFrame() == null ? null : event.getItemFrame().getLevel();
        if (level != null && this.getListenerRooms().containsKey(level.getFolderName())) {
            event.setCancelled();
        }
    }

}
