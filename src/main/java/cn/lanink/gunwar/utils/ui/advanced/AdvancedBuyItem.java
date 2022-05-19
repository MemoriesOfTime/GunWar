package cn.lanink.gunwar.utils.ui.advanced;

import cn.lanink.gamecore.form.inventory.responsible.ResponseItem;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.supplier.items.SupplyItemConfig;
import cn.nukkit.Player;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.item.Item;
import org.jetbrains.annotations.NotNull;

public class AdvancedBuyItem extends ResponseItem {

    private final SupplyItemConfig itemConfig;

    public AdvancedBuyItem(@NotNull Item item, @NotNull SupplyItemConfig itemConfig) {
        super(item);
        this.itemConfig = itemConfig;
    }

    @Override
    public void callClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player) {
        for (BaseRoom room : GunWar.getInstance().getRooms().values()) {
            if (room.isPlaying(player)) {
                int nowIntegral = room.getPlayerIntegral(player);
                if (nowIntegral < this.itemConfig.getNeedIntegral()) {
                    player.sendTip("您没有足够的积分来购买！");
                    return;
                }
                room.getPlayerIntegralMap().put(player, nowIntegral - this.itemConfig.getNeedIntegral());
                player.getInventory().addItem(this.itemConfig.getItems());
                player.sendTip("购买成功！");
            }
        }
    }

}