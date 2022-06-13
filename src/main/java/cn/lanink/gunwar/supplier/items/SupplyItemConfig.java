package cn.lanink.gunwar.supplier.items;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.utils.exception.supply.SupplyConfigLoadException;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

@ToString
public class SupplyItemConfig {

    @Getter
    private final String fileName;
    @Getter
    private final Config config;

    @Getter
    private final String title;
    @Getter
    private final String subTitle;
    private final List<String> items;
    @Getter
    private final int slotPos;
    @Getter
    private final int needIntegral;

    public SupplyItemConfig(@NotNull String fileName, @NotNull File fileConfig) throws SupplyConfigLoadException {
        this.fileName = fileName;
        this.config = new Config(fileConfig, Config.YAML);

        this.title = this.config.getString("title");
        this.subTitle = this.config.getString("subTitle");

        this.slotPos = this.config.getInt("pos");

        this.needIntegral = this.config.getInt("needIntegral", 500);
        if (this.needIntegral <= 0) {
            GunWar.getInstance().getLogger().warning("商店物品：" + this.fileName + " 需要积分为0！玩家可无限购买！");
        }

        this.items = this.config.getStringList("items");
        Item[] itemArray = this.items.stream().map(ItemManage::of).toArray(Item[]::new); //全部读取一次，检查格式是否正确
        if (itemArray.length == 0) {
            throw new SupplyConfigLoadException("商店物品：" + this.fileName + " 无法正确加载物品！请检查配置！");
        }
    }

    public Item getItem() {
        return ItemManage.of(this.items.get(0));
    }

    public Item[] getItems() {
        return this.items.stream().map(ItemManage::of).toArray(Item[]::new);
    }

}