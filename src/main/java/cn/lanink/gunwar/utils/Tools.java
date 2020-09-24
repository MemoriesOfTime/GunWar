package cn.lanink.gunwar.utils;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
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
import cn.nukkit.network.protocol.PlayerSkinPacket;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Tools {

    public static void sendMessage(Room room, String message) {
        for (Player player : room.getPlayers().keySet()) {
            player.sendMessage(message);
        }
    }

    public static double randomDouble(double min, double max) {
        return min + ((max - min) * GunWar.RANDOM.nextDouble());
    }

    /**
     * 执行命令
     * @param player 玩家
     * @param cmds 命令
     */
    public static void cmd(Player player, List<String> cmds) {
        if (player == null || cmds == null || cmds.size() < 1) {
            return;
        }
        for (String s : cmds) {
            String[] cmd = s.split("&");
            if ((cmd.length > 1) && (cmd[1].equals("con"))) {
                Server.getInstance().dispatchCommand(new ConsoleCommandSender(), cmd[0].replace("@p", player.getName()));
            } else {
                Server.getInstance().dispatchCommand(player, cmd[0].replace("@p", player.getName()));
            }
        }
    }

    /**
     * 清理实体
     * @param level 世界
     */
    public static void cleanEntity(Level level) {
        cleanEntity(level, false);
    }

    public static void cleanEntity(Level level, boolean all) {
        for (Entity entity : level.getEntities()) {
            if (!(entity instanceof Player)) {
                if (entity instanceof EntityPlayerCorpse ||
                        entity instanceof EntityFlag ||
                        entity instanceof EntityFlagStand) {
                    if (!all) {
                        continue;
                    }
                }
                entity.close();
            }
        }
    }


    @Deprecated
    public static void giveItem(Player player, int team) {
        player.getInventory().setArmorContents(getArmors(team));
        player.getInventory().addItem(Item.get(272, 0, 1),
                Item.get(261, 0, 1),
                Item.get(262, 0, 5),
                Item.get(332, 0, 64),
                getItem(4), getItem(5));
    }

    /**
     * 给装备
     * @param room 房间
     * @param player 玩家
     * @param team 所属队伍
     */
    public static void giveItem(Room room, Player player, int team) {
        player.getInventory().setArmorContents(getArmors(team));
        for (Map.Entry<ItemManage.ItemType, ArrayList<String>> entry : room.getInitialItems().entrySet()) {
            for (String value : entry.getValue()) {
                String[] strings = value.split(":");
                Item item = null;
                switch (entry.getKey()) {
                    case MELEE_WEAPON:
                        item = ItemManage.getMeleeWeaponMap().get(strings[0]).getItem();
                        break;
                    case PROJECTILE_WEAPON:
                        item = ItemManage.getProjectileWeaponMap().get(strings[0]).getItem();
                        break;
                    //TODO
                    case GUN_WEAPON:
                        break;
                }
                if (item != null) {
                    item.setCount(Integer.parseInt(strings[1]));
                    player.getInventory().addItem(item);
                    if (GunWar.debug) {
                        GunWar.getInstance().getLogger().info("[debug] 给玩家：" + player.getName() +
                                "物品：" + item.getCustomName() + "数量：" + strings[1]);
                    }
                }
            }
        }
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
     * 设置实体皮肤
     * @param human 实体
     * @param skin 皮肤
     */
    public static void setHumanSkin(EntityHuman human, Skin skin) {
        PlayerSkinPacket packet = new PlayerSkinPacket();
        packet.skin = skin;
        packet.newSkinName = skin.getSkinId();
        packet.oldSkinName = human.getSkin().getSkinId();
        packet.uuid = human.getUniqueId();
        human.setSkin(skin);
        human.getLevel().getPlayers().values().forEach(player -> player.dataPacket(packet));
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
            player.setNameTag(player.getName());
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
     * GitHub：https://github.com/PetteriM1/FireworkShow
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
