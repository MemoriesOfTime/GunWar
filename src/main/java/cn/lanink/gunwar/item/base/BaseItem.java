package cn.lanink.gunwar.item.base;

import cn.lanink.gunwar.item.ItemManage;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public abstract class BaseItem {

    public static final String GUN_WAR_ITEM_TAG = "GunWarItemTag";
    public static final String GUN_WAR_ITEM_NAME = "GunWarItemName";
    public static final String GUN_WAR_ITEM_TYPE = "GunWarItemType";

    private final String name;
    public final Item item;
    private final boolean infiniteDurability;

    public BaseItem(String name, Config config) {
        this.name = name;
        this.item = Item.fromString(config.getString("id"));
        this.item.setCustomName(config.getString("showName", this.item.getName()));
        String lore = config.getString("lore");
        if (!lore.trim().isEmpty()) {
            this.item.setLore(lore.split("\n"));
        }
        if (!this.item.hasCompoundTag()) {
            this.item.setNamedTag(new CompoundTag());
        }
        this.infiniteDurability = config.getBoolean("infiniteDurability", false);
        if (this.infiniteDurability) {
            this.item.getNamedTag().putByte("Unbreakable", 1);
        }
        this.item.getNamedTag().putCompound(GUN_WAR_ITEM_TAG, new CompoundTag()
                .putString(GUN_WAR_ITEM_NAME, this.name)
                .putString(GUN_WAR_ITEM_TYPE, this.getItemType().getStringType()))
                .putBoolean("infiniteDurability", this.infiniteDurability);
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
     * @return 无限耐久
     */
    public boolean isInfiniteDurability() {
        return infiniteDurability;
    }

    /**
     * @return 物品
     */
    public Item getItem() {
        return this.item.clone();
    }

    public CompoundTag getCompoundTag() {
        return this.item.getNamedTag().getCompound(GUN_WAR_ITEM_TAG);
    }

    public void setCompoundTag(CompoundTag compoundTag) {
        CompoundTag tag = this.item.getNamedTag();
        tag.putCompound(GUN_WAR_ITEM_TAG, compoundTag);
        this.item.setCompoundTag(tag);
    }

}
