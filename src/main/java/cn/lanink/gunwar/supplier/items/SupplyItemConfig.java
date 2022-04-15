package cn.lanink.gunwar.supplier.items;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

import java.io.File;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

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
    private final Item[] items;
    @Getter
    private final int slotPos;
    @Getter
    private final int needIntegral;

    public SupplyItemConfig(@NotNull String fileName, @NotNull File fileConfig) {
        this.fileName = fileName;
        this.config = new Config(fileConfig, Config.YAML);

        this.title = this.config.getString("title");
        this.subTitle = this.config.getString("subTitle");

        this.slotPos = this.config.getInt("pos");

        this.needIntegral = this.config.getInt("needIntegral", 500);
        if (this.needIntegral <= 0) {
            GunWar.getInstance().getLogger().warning("商店物品：" + this.fileName + " 需要积分为0！玩家可无限购买！");
        }

        this.items = this.config.getStringList("items").stream()
                .filter(rawStr -> rawStr.matches("\\d{1,5}:\\d{1,4}x\\d{1,3}"))
                .map(rawStr -> {
                    Item item = Item.fromString(rawStr.split("x")[0]);
                    item.setCount(Tools.toInt(rawStr.split("x")[1]));
                    return item;
                }).toArray(Item[]::new);
        if (this.items.length == 0) {
            throw new RuntimeException("商店物品：" + this.fileName + " 无法正确加载物品！请检查配置！");
        }
    }

    public Item getItem() {
        return this.items[0].clone();
    }

    public Item[] getItems() {
        return this.items.clone();
    }

}