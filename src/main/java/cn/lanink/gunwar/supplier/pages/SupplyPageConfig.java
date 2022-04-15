package cn.lanink.gunwar.supplier.pages;

import cn.lanink.gamecore.form.inventory.advanced.AdvancedFakeChestInventory;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowSimple;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.supplier.SupplyConfig;
import cn.lanink.gunwar.supplier.items.SupplyItemConfig;
import cn.lanink.gunwar.utils.ui.advanced.AdvancedBuyItem;
import cn.lanink.gunwar.utils.ui.advanced.AdvancedPageLinkItem;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.*;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString
public class SupplyPageConfig {

    private final String fileName;
    private final Config config;
    private final String title;

    @ToString.Exclude
    private final SupplyConfig parent;

    // slotPos -> Item
    private final @Nullable ImmutableMap<Integer, LinkItem> linkItems;
    // slotPos -> Item
    private final @NotNull ImmutableMap<Integer, SupplyItemConfig> items;

    public SupplyPageConfig(@NotNull String fileName, @NotNull File fileConfig, @NotNull SupplyConfig parent) {
        this.fileName = fileName;
        this.parent = parent;
        this.config = new Config(fileConfig, Config.YAML);

        this.title = this.config.getString("title");
        Map<String, Map<String, String>> rawLinkItemData = (Map<String, Map<String, String>>) this.config.get("linkItems");

        ImmutableMap.Builder<Integer, LinkItem> linkItemBuilder = new ImmutableMap.Builder<>();
        ImmutableMap.Builder<Integer, SupplyItemConfig> itemBuilder = new ImmutableMap.Builder<>();

        if (rawLinkItemData != null) {
            rawLinkItemData.entrySet().stream()
                    .filter(stringMapEntry -> {
                        String idAndMeta = stringMapEntry.getKey();

                        if (!idAndMeta.matches("\\d{1,3}:\\d{1,4}")) {
                            return false;
                        }

                        Map<String, String> value = stringMapEntry.getValue();
                        List<String> authorizedKey = Arrays.asList("pos", "link", "afterClick");
                        if (value.size() != authorizedKey.size() && value.containsKey("afterClick")) {
                            return false;
                        }
                        for (Map.Entry<String, String> secondEntry : value.entrySet()) {
                            if (!authorizedKey.contains(secondEntry.getKey())) {
                                return false;
                            }
                        }
                        return value.get("pos").matches("\\d{1,2}");
                    }).forEach(stringMapEntry -> {
                        Map<String, String> value = stringMapEntry.getValue();
                        int slotPos = Integer.parseInt(value.get("pos"));
                        LinkItem linkItem = new LinkItem(
                                Item.fromString(stringMapEntry.getKey()),
                                slotPos,
                                value.get("link"),
                                value.containsKey("afterClick") ? Item.fromString(value.get("afterClick")) : null
                        );
                        linkItemBuilder.put(slotPos, linkItem);
                    });
            this.linkItems = linkItemBuilder.build();
        } else {
            this.linkItems = null;
        }

        ArrayList<Integer> list = new ArrayList<>();
        this.config.getStringList("items")
                .stream()
                .filter(item -> this.parent.getItemConfigMap().containsKey(item))
                .forEach(item -> {
                    final SupplyItemConfig supplyItemConfig = this.parent.getItemConfigMap().get(item);
                    int slotPos = supplyItemConfig.getSlotPos();
                    while (list.contains(slotPos)) {
                        slotPos++;
                    }
                    itemBuilder.put(slotPos, supplyItemConfig);
                    list.add(slotPos);
                });
        this.items = itemBuilder.build();
    }

    public AdvancedFakeChestInventory generateWindow() {
        return generateWindow(null);
    }

    public AdvancedFakeChestInventory generateWindow(AdvancedFakeChestInventory advancedInventory) {
        if (advancedInventory == null) {
            advancedInventory = new AdvancedFakeChestInventory(this.title);
        }
        advancedInventory.clearAll();
        AdvancedFakeChestInventory finalAdvancedInventory = advancedInventory;
        if (this.linkItems != null) {
            this.linkItems.forEach((slotPos, linkItem) -> {
                SupplyPageConfig supplyPageConfig = getParent().getPageConfigMap().get(linkItem.getPageFileName());
                finalAdvancedInventory.putItem(slotPos, new AdvancedPageLinkItem(linkItem.getItem().setCustomName(supplyPageConfig.getTitle()), supplyPageConfig));
            });
        }
        this.items.forEach((slotPos, item) -> finalAdvancedInventory.putItem(slotPos, new AdvancedBuyItem(item.getItem().setCustomName(item.getTitle() + "§r\n" + item.getSubTitle()), item)));

        return finalAdvancedInventory;
    }

    public AdvancedFormWindowSimple generateForm(AdvancedFormWindowSimple parent) {
        AdvancedFormWindowSimple advancedFormWindowSimple = new AdvancedFormWindowSimple(this.title);
        if (parent != null) {
            advancedFormWindowSimple.addButton("返回到主界面", player -> player.showFormWindow(parent));
        }
        this.items.forEach((slotPos, itemConfig) ->
                advancedFormWindowSimple.addButton(itemConfig.getTitle() + "§r\n" + itemConfig.getSubTitle(), (player) -> {
                    BaseRoom room = null;
                    for (BaseRoom r : GunWar.getInstance().getRooms().values()) {
                        if (r.isPlaying(player)) {
                            room = r;
                        }
                    }
                    if (room == null) {
                        return;
                    }

                    int nowIntegral = room.getPlayerIntegralMap().getOrDefault(player, 0);
                    if (nowIntegral < itemConfig.getNeedIntegral()) {
                        player.sendTip("您没有足够的积分来购买！");
                    } else {
                        room.getPlayerIntegralMap().put(player, nowIntegral - itemConfig.getNeedIntegral());
                        player.getInventory().addItem(itemConfig.getItems());
                        player.sendTip("购买成功！");
                    }
                }
        ));
        return advancedFormWindowSimple;
    }

}