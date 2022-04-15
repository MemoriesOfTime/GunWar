 package cn.lanink.gunwar.utils.ui.advanced;
 
 import cn.lanink.gamecore.form.inventory.advanced.AdvancedFakeChestInventory;
 import cn.lanink.gamecore.form.inventory.responsible.ResponseItem;
 import cn.lanink.gunwar.supplier.pages.LinkItem;
 import cn.lanink.gunwar.supplier.pages.SupplyPageConfig;
 import cn.nukkit.Player;
 import cn.nukkit.event.inventory.InventoryClickEvent;
 import cn.nukkit.item.Item;
 import org.jetbrains.annotations.NotNull;
 
 
 
 public class AdvancedPageLinkItem
   extends ResponseItem
 {
   private final SupplyPageConfig pageConfig;
   
   public AdvancedPageLinkItem(@NotNull Item item, @NotNull SupplyPageConfig nextPageConfig) {
/* 20 */     super(item);
/* 21 */     this.pageConfig = nextPageConfig;
   }
 
   
   public void callClick(InventoryClickEvent clickEvent, Player player) {
/* 26 */     if (!(clickEvent.getInventory() instanceof AdvancedFakeChestInventory)) {
       return;
     }
     
/* 30 */     AdvancedFakeChestInventory newWindow = this.pageConfig.generateWindow((AdvancedFakeChestInventory)clickEvent.getInventory());
     
/* 32 */     if (this.pageConfig.getLinkItems() != null && (
/* 33 */       (LinkItem)this.pageConfig.getLinkItems().get(Integer.valueOf(clickEvent.getSlot()))).getAfterClick() != null) {
/* 34 */       Item afterClick = ((LinkItem)this.pageConfig.getLinkItems().get(Integer.valueOf(clickEvent.getSlot()))).getAfterClick().setCustomName(getItem().getCustomName());
/* 35 */       newWindow.setItem(clickEvent.getSlot(), afterClick);
     } 
   }
 }


/* Location:              D:\BaiduNetdiskDownload\GunWar-1.4.3.2-FAP.jar!\cn\lanink\gunwa\\util\\ui\advanced\AdvancedPageLinkItem.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */