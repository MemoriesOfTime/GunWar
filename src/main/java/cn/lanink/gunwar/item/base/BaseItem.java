package cn.lanink.gunwar.item.base;

import cn.lanink.gunwar.item.ItemManage;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;

import java.util.Objects;

/**
 * @author lt_name
 */
public abstract class BaseItem {

    public static final String GUN_WAR_ITEM_TAG = "GunWarItemTag";
    public static final String GUN_WAR_ITEM_NAME = "GunWarItemName";
    public static final String GUN_WAR_ITEM_TYPE = "GunWarItemType";

    private final String name;
    public final Item item;

    public BaseItem(String name, Config config) {
        this.name = name;
        String stringID = config.getString("id");
        String[] split = stringID.split(":");
        if (split.length > 1) {
            this.item = Item.get(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }else {
            this.item = Item.get(Integer.parseInt(stringID));
        }
        this.item.setCustomName(config.getString("showName", this.item.getName()));
        String lore = config.getString("lore");
        if (!"".equals(lore.trim())) {
            this.item.setLore(lore.split("\n"));
        }
        if (!this.item.hasCompoundTag()) {
            this.item.setNamedTag(new CompoundTag());
        }
        this.item.getNamedTag().putCompound(GUN_WAR_ITEM_TAG, new CompoundTag()
                .putString(GUN_WAR_ITEM_NAME, this.name)
                .putInt(GUN_WAR_ITEM_TYPE, this.getItemType().getIntType()));
    }

    public abstract ItemManage.ItemType getItemType();

    /**
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return 显示名称
     */
    public String getShowName() {
        return this.item.getCustomName();
    }

    /**
     * @return 物品
     */
    public Item getItem() {
        return this.item.clone();
    }

    public CompoundTag getGunWarItemTag() {
        return this.item.getNamedTag().getCompound(GUN_WAR_ITEM_TAG);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseItem)) return false;
        BaseItem baseItem = (BaseItem) o;
        return Objects.equals(name, baseItem.name) &&
                Objects.equals(this.getItemType(), baseItem.getItemType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.getItemType());
    }

}
