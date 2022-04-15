package cn.lanink.gunwar.utils.ui.advanced;

import cn.lanink.gamecore.form.inventory.advanced.AdvancedFakeChestInventory;
import cn.lanink.gamecore.form.inventory.responsible.ResponseItem;
import cn.lanink.gunwar.supplier.pages.SupplyPageConfig;
import cn.nukkit.Player;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.item.Item;
import org.jetbrains.annotations.NotNull;

public class AdvancedPageLinkItem extends ResponseItem {

    private final SupplyPageConfig pageConfig;

    public AdvancedPageLinkItem(@NotNull Item item, @NotNull SupplyPageConfig nextPageConfig) {
        super(item);
        this.pageConfig = nextPageConfig;
    }

    @Override
    public void callClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player) {
        if (!(clickEvent.getInventory() instanceof AdvancedFakeChestInventory)) {
            return;
        }
        AdvancedFakeChestInventory newWindow = this.pageConfig.generateWindow((AdvancedFakeChestInventory) clickEvent.getInventory());
        if (this.pageConfig.getLinkItems() != null && this.pageConfig.getLinkItems().get(clickEvent.getSlot()).getAfterClick() != null) {
            Item afterClick = this.pageConfig.getLinkItems().get(clickEvent.getSlot()).getAfterClick().setCustomName(getItem().getCustomName());
            newWindow.setItem(clickEvent.getSlot(), afterClick);
        }
    }

}