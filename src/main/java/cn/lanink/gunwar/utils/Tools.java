package cn.lanink.gunwar.utils;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemColorArmor;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.DyeColor;
import tip.messages.BossBarMessage;
import tip.messages.NameTagMessage;
import tip.messages.ScoreBoardMessage;
import tip.messages.TipMessage;
import tip.utils.Api;

import java.util.LinkedList;
import java.util.Random;


public class Tools {

    /**
     * 清理实体
     * @param level 世界
     */
    public static void cleanEntity(Level level) {
        for (Entity entity : level.getEntities()) {
            if (!(entity instanceof Player)) {
                entity.close();
            }
        }
    }

    /**
     * 给装备
     * @param player 玩家
     * @param team 所属队伍
     */
    public static void giveItem(Player player, int team) {
        Language language = GunWar.getInstance().getLanguage();
        Item grenade = Item.get(344, 0, 1);
        grenade.setNamedTag(new CompoundTag().putBoolean("isGunWarItem", true)
                .putInt("GunWarItemType", 4));
        grenade.setCustomName(language.itemGrenade);
        grenade.setLore(language.itemGrenadeLore.split("\n"));
        Item flashBang = Item.get(344, 0, 1);
        flashBang.setNamedTag(new CompoundTag().putBoolean("isGunWarItem", true)
                .putInt("GunWarItemType", 5));
        flashBang.setCustomName(language.itemFlashBang);
        flashBang.setLore(language.itemFlashBangLore.split("\n"));
        player.getInventory().setArmorContents(getArmors(team));
        player.getInventory().addItem(Item.get(272, 0, 1),
                Item.get(261, 0, 1),
                Item.get(262, 0, 5),
                Item.get(332, 0, 64),
                grenade, flashBang);
    }

    /**
     * 获取盔甲
     * @param team 队伍
     * @return 盔甲
     */
    public static Item[] getArmors(int team) {
        ItemColorArmor helmet = (ItemColorArmor) Item.get(298, 0, 1);
        ItemColorArmor chestPlate = (ItemColorArmor) Item.get(299, 0, 1);
        ItemColorArmor leggings = (ItemColorArmor) Item.get(300, 0, 1);
        ItemColorArmor boots = (ItemColorArmor) Item.get(301, 0, 1);
        BlockColor color;
        if (team == 1 || team == 11) {
            color = new BlockColor(255, 0, 0);
        }else {
            color = new BlockColor(0, 0, 255);
        }
        Item[] armor = new Item[4];
        armor[0] = helmet.setColor(color);
        armor[1] = chestPlate.setColor(color);
        armor[2] = leggings.setColor(color);
        armor[3] = boots.setColor(color);
        return armor;
    }

    /**
     * 获取物品
     * @return 物品
     */
    public static Item getItem(int type) {
        Language language = GunWar.getInstance().getLanguage();
        Item item = Item.get(0);
        switch (type) {
            case 4:
                item = Item.get(344, 0, 1);
                item.setNamedTag(new CompoundTag().putBoolean("isGunWarItem", true)
                        .putInt("GunWarItemType", 4));
                item.setCustomName(language.itemGrenade);
                item.setLore(language.itemGrenadeLore.split("\n"));
                return item;
            case 5:
                item = Item.get(344, 0, 1);
                item.setNamedTag(new CompoundTag().putBoolean("isGunWarItem", true)
                        .putInt("GunWarItemType", 5));
                item.setCustomName(language.itemFlashBang);
                item.setLore(language.itemFlashBangLore.split("\n"));
                return item;
            case 10:
                item = Item.get(324, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isGunWarItem", true)
                        .putInt("GunWarItemType", 10));
                item.setCustomName(language.itemQuitRoom);
                item.setLore(language.itemQuitRoomLore.split("\n"));
                return item;
            case 11:
                item = Item.get(241, 14, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isGunWarItem", true)
                        .putInt("GunWarItemType", 11));
                item.setCustomName(language.itemTeamSelectRed);
                return item;
            case 12:
                item = Item.get(241, 11, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isGunWarItem", true)
                        .putInt("GunWarItemType", 12));
                item.setCustomName(language.itemTeamSelectBlue);
                return item;
            default:
                return item;
        }
    }

    /**
     * 移除显示信息(Tips)
     * @param level 地图
     */
    public static void removePlayerShowMessage(String level, Player player) {
        Api.removePlayerShowMessage(player.getName(),
                new NameTagMessage(level, true, ""));
        Api.removePlayerShowMessage(player.getName(),
                new TipMessage(level, true, 0, ""));
        Api.removePlayerShowMessage(player.getName(),
                new ScoreBoardMessage(level, true, "", new LinkedList<>()));
        Api.removePlayerShowMessage(player.getName(),
                new BossBarMessage(level, false, 5, false, new LinkedList<>()));
    }

    /**
     * 重置玩家状态
     * @param player 玩家
     * @param joinRoom 是否为加入房间
     */
    public static void rePlayerState(Player player, boolean joinRoom) {
        player.setGamemode(0);
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setLevel(player.getFoodData().getMaxLevel());
        if (joinRoom) {
            player.setNameTagVisible(false);
            player.setNameTagAlwaysVisible(false);
            player.setAllowModifyWorld(false);
        }else {
            player.setNameTagVisible(true);
            player.setNameTagAlwaysVisible(true);
            player.setAllowModifyWorld(true);
        }
        player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, false));
    }

    /**
     * 播放声音
     * @param room 房间
     * @param sound 声音
     */
    public static void addSound(Room room, Sound sound) {
        room.getPlayers().keySet().forEach(player -> addSound(player, sound));
    }

    /**
     * 发包方式播放声音
     * @param player 玩家
     * @param sound 声音
     */
    public static void addSound(Player player, Sound sound) {
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound.getSound();
        packet.volume = 1.0F;
        packet.pitch = 1.0F;
        packet.x = player.getFloorX();
        packet.y = player.getFloorY();
        packet.z = player.getFloorZ();
        player.dataPacket(packet);
    }

    /**
     * 获取底部 Y
     * 调用前应判断非空
     * @param player 玩家
     * @return Y
     */
    public static double getFloorY(Player player) {
        for (int y = 0; y < 10; y++) {
            Level level = player.getLevel();
            Block block = level.getBlock(player.getFloorX(), player.getFloorY() - y, player.getFloorZ());
            if (block.getId() != 0) {
                if (block.getBoundingBox() != null) {
                    return block.getBoundingBox().getMaxY() + 0.2;
                }
                return block.getMinY() + 0.2;
            }
        }
        return player.getFloorY();
    }

    /**
     * 放烟花
     * GitHub：https://github.com/SmallasWater/LuckDraw/blob/master/src/main/java/smallaswater/luckdraw/utils/Tools.java
     * @param position 位置
     */
    public static void spawnFirework(Position position) {
        Level level = position.getLevel();
        ItemFirework item = new ItemFirework();
        CompoundTag tag = new CompoundTag();
        Random random = new Random();
        CompoundTag ex = new CompoundTag();
        ex.putByteArray("FireworkColor",new byte[]{
                (byte) DyeColor.values()[random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].getDyeData()
        });
        ex.putByteArray("FireworkFade",new byte[0]);
        ex.putBoolean("FireworkFlicker",random.nextBoolean());
        ex.putBoolean("FireworkTrail",random.nextBoolean());
        ex.putByte("FireworkType",ItemFirework.FireworkExplosion.ExplosionType.values()
                [random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].ordinal());
        tag.putCompound("Fireworks",(new CompoundTag("Fireworks")).putList(new ListTag<CompoundTag>("Explosions").add(ex)).putByte("Flight",1));
        item.setNamedTag(tag);
        CompoundTag nbt = new CompoundTag();
        nbt.putList(new ListTag<DoubleTag>("Pos")
                .add(new DoubleTag("",position.x+0.5D))
                .add(new DoubleTag("",position.y+0.5D))
                .add(new DoubleTag("",position.z+0.5D))
        );
        nbt.putList(new ListTag<DoubleTag>("Motion")
                .add(new DoubleTag("",0.0D))
                .add(new DoubleTag("",0.0D))
                .add(new DoubleTag("",0.0D))
        );
        nbt.putList(new ListTag<FloatTag>("Rotation")
                .add(new FloatTag("",0.0F))
                .add(new FloatTag("",0.0F))

        );
        nbt.putCompound("FireworkItem", NBTIO.putItemHelper(item));
        EntityFirework entity = new EntityFirework(level.getChunk((int)position.x >> 4, (int)position.z >> 4), nbt);
        entity.spawnToAll();
    }

}
