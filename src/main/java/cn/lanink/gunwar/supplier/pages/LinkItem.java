package cn.lanink.gunwar.supplier.pages;

import cn.lanink.gamecore.api.Info;
import cn.nukkit.item.Item;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ToString
public class LinkItem {

    private final int slotPos;
    private final Item item;

    @Info("从 SupplyConfig 中的 pageConfigMap 获取 page数据")
    @Getter
    private final String pageFileName;
    private final Item afterClick;

    public int getSlotPos() {
        return this.slotPos;
    }

    public LinkItem(@NotNull Item item, int slotPos, @NotNull String pageFileName) {
        this(item, slotPos, pageFileName, null);
    }

    public LinkItem(@NotNull Item item, int slotPos, @NotNull String pageFileName, @Nullable Item afterClick) {
        this.item = item;
        this.slotPos = slotPos;
        this.pageFileName = pageFileName;
        this.afterClick = afterClick;
    }

    public Item getItem() {
        return this.item.clone();
    }

    public Item getAfterClick() {
        if (this.afterClick != null) {
            return this.afterClick.clone();
        }
        return null;
    }

}
