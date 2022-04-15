 package cn.lanink.gunwar.utils.ui.advanced;
 
 import cn.lanink.gamecore.form.inventory.responsible.ResponseItem;
 import cn.lanink.gunwar.GunWar;
 import cn.lanink.gunwar.room.base.BaseRoom;
 import cn.lanink.gunwar.supplier.items.SupplyItemConfig;
 import cn.nukkit.Player;
 import cn.nukkit.event.inventory.InventoryClickEvent;
 import cn.nukkit.item.Item;
 import org.jetbrains.annotations.NotNull;
 
 
 
 
 public class AdvancedBuyItem
   extends ResponseItem
 {
   private final SupplyItemConfig itemConfig;
   
   public AdvancedBuyItem(@NotNull Item item, @NotNull SupplyItemConfig itemConfig) {
/* 21 */     super(item);
/* 22 */     this.itemConfig = itemConfig;
   }
 
   
   public void callClick(InventoryClickEvent clickEvent, Player player) {
/* 27 */     for (BaseRoom room : GunWar.getInstance().getRooms().values()) {
/* 28 */       if (room.isPlaying(player)) {
/* 29 */         int nowIntegral = ((Integer)room.getPlayerIntegralMap().getOrDefault(player, Integer.valueOf(0))).intValue();
/* 30 */         if (nowIntegral < this.itemConfig.getNeedIntegral()) {
/* 31 */           player.sendTip("您没有足够的积分来购买！");
           return;
         } 
/* 34 */         room.getPlayerIntegralMap().put(player, Integer.valueOf(nowIntegral - this.itemConfig.getNeedIntegral()));
         
/* 36 */         player.getInventory().addItem(this.itemConfig.getItems());
/* 37 */         player.sendTip("购买成功！");
       } 
     } 
   }
 }


/* Location:              D:\BaiduNetdiskDownload\GunWar-1.4.3.2-FAP.jar!\cn\lanink\gunwa\\util\\ui\advanced\AdvancedBuyItem.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */