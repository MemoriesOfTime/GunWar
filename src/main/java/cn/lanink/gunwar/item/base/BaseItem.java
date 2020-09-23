package cn.lanink.gunwar.item.base;

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
                .putInt(GUN_WAR_ITEM_TYPE, ItemType.NULL.getIntType()));
    }

    public abstract ItemType getItemType();

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

    public static ItemType getItemType(Item item) {
        if (item.hasCompoundTag()) {
            CompoundTag tag = item.getNamedTag().getCompound(GUN_WAR_ITEM_TAG);
            int intType = tag.getInt(GUN_WAR_ITEM_TYPE);
            for (ItemType itemType : ItemType.values()) {
                if (itemType.getIntType() == intType) {
                    return itemType;
                }
            }
        }
        return ItemType.NULL;
    }

    public enum ItemType {
        NULL(0),
        MELEE_WEAPON(1),
        PROJECTILE_WEAPON(2),
        GUN_WEAPON(3);

        private final int intType;

        ItemType(int intType) {
            this.intType = intType;
        }

        public int getIntType() {
            return this.intType;
        }

    }

}
