package cn.lanink.gunwar.tasks.adminroom;

import cn.lanink.gamecore.utils.Tips;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityText;
import cn.lanink.gunwar.gui.GuiCreate;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author lt_name
 */
public class SetRoomTask extends PluginTask<GunWar> {

    private int setRoomSchedule = 10;
    private int backRoomSchedule = 10;
    private int nextRoomSchedule = 20;
    private boolean autoNext = false;

    private final Player player;
    private final Level level;
    private final Map<Integer, Item> playerInventory;
    private final Item offHandItem;
    private final int beforeGameMode;

    private EntityText waitSpawnText;
    private EntityText redSpawnText;
    private EntityText blueSpawnText;
    private List<EntityText> randomSpawnTextList = new ArrayList<>();

    private int particleEffectTick = 0;

    public SetRoomTask(GunWar owner, Player player, Level level) {
        super(owner);
        this.player = player;
        this.level = level;
        this.playerInventory = player.getInventory().getContents();
        this.offHandItem = player.getOffhandInventory().getItem(0);
        this.beforeGameMode = player.getGamemode();

        player.getInventory().clearAll();
        player.getUIInventory().clearAll();

        player.setGamemode(Player.CREATIVE);

        if (owner.isHasTips()) {
            Tips.closeTipsShow(player.getLevel().getFolderName(), player);
        }
    }

    @Override
    public void onRun(int i) {
        if (!this.player.isOnline() ||
                this.player.getLevel() != this.level ||
                !this.owner.setRoomTask.containsKey(this.player)) {
            this.cancel();
            return;
        }
        Item item;
        if (this.setRoomSchedule > 10) {
            item = Item.get(340);
            item.setNamedTag(new CompoundTag()
                    .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 110));
            item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_back"));
            this.player.getInventory().setItem(0, item);
        }else {
            this.player.getInventory().clear(0);
        }
        boolean canNext = false;
        Config config = this.owner.getRoomConfig(player.getLevel());
        switch (this.setRoomSchedule) {
            case 10: //设置等待出生点
                this.nextRoomSchedule = 20;
                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setWaitSpawn"));
                item = Item.get(138);
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setWaitSpawn"));
                this.player.getInventory().setItem(4, item);
                if (!"".equals(config.getString("waitSpawn").trim())) {
                    canNext = true;
                }
                break;
            case 20: //设置红队出生点
                this.backRoomSchedule = 10;
                this.nextRoomSchedule = 30;
                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setTeamSpawn",
                        this.owner.getLanguage().translateString("teamNameRed")));
                item = Item.get(241, 14);
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setTeamSpawn",
                        this.owner.getLanguage().translateString("teamNameRed")));
                this.player.getInventory().setItem(4, item);
                if (!"".equals(config.getString("redSpawn").trim())) {
                    canNext = true;
                }
                break;
            case 30: //设置蓝队出生点
                this.backRoomSchedule = 20;
                this.nextRoomSchedule = 40;
                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setTeamSpawn",
                        this.owner.getLanguage().translateString("teamNameBlue")));
                item = Item.get(241, 11);
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setTeamSpawn",
                        this.owner.getLanguage().translateString("teamNameBlue")));
                this.player.getInventory().setItem(4, item);
                if (!"".equals(config.getString("blueSpawn").trim())) {
                    canNext = true;
                }
                break;
            case 40: //设置更多参数
                this.backRoomSchedule = 30;
                this.nextRoomSchedule = 45;
                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setMoreParameters"));
                item = Item.get(347, 11);
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setMoreParameters"));
                this.player.getInventory().setItem(4, item);
                if (config.getInt("waitTime") > 0 &&
                        config.getInt("gameTime") > 0 &&
                        config.getInt("victoryScore") > 0) {
                    if (autoNext) {
                        this.setRoomSchedule(this.nextRoomSchedule);
                        GuiCreate.sendAdminShopMenu(player);
                    }else {
                        canNext = true;
                    }
                }
                break;
            case 45: //设置商店
                this.backRoomSchedule = 40;
                this.nextRoomSchedule = 50;
                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setShop"));
                item = Item.get(347, 11);
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setShop"));
                this.player.getInventory().setItem(4, item);

                String supplyType = config.getString("supplyType");
                if ("CLOSE".equalsIgnoreCase(supplyType)  ||
                        "ALL_ROUND".equalsIgnoreCase(supplyType) ||
                        config.getInt("supplyEnableTime") > 0) {
                    if (autoNext) {
                        this.setRoomSchedule(this.nextRoomSchedule);
                        GuiCreate.sendAdminPlayersMenu(player);
                    }else {
                        canNext = true;
                    }
                }
                break;
            case 50: //设置房间人数
                this.backRoomSchedule = 45;
                this.nextRoomSchedule = 60;
                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setRoomPlayers"));
                item = Item.get(347, 11);
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setRoomPlayers"));
                this.player.getInventory().setItem(4, item);
                if (config.getInt("minPlayers") > 0 &&
                        config.getInt("maxPlayers") > 0) {
                    if (autoNext) {
                        this.setRoomSchedule(this.nextRoomSchedule);
                        GuiCreate.sendAdminModeMenu(player);
                    }else {
                        canNext = true;
                    }
                }
                break;
            case 60: //设置游戏模式
                this.backRoomSchedule = 50;
                this.nextRoomSchedule = 70;
                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setGameMode"));
                item = Item.get(347, 11);
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setGameMode"));
                this.player.getInventory().setItem(4, item);
                String setMode = config.getString("gameMode", "").trim();
                if (!"".equals(setMode)) {
                    switch (setMode) {
                        case "blasting":
                            this.nextRoomSchedule = 200;
                            break;
                        case "ffa":
                            this.nextRoomSchedule = 300;
                            break;
                        case "conquest":
                            this.nextRoomSchedule = 400;
                            break;
                    }
                    if (autoNext) {
                        this.setRoomSchedule(this.nextRoomSchedule);
                    }else {
                        canNext = true;
                    }
                }
                break;
            case 70: //保存设置
                this.player.sendMessage(this.owner.getLanguage().translateString("admin_setRoom_setSuccessful"));
                config.save(true);
                this.closeEntity();
                this.owner.getGameRoomManager().loadGameRoom(this.level.getFolderName());
                this.cancel();
                return;
            case 200: //爆破模式 设置爆破点A
                this.backRoomSchedule = 60;
                this.nextRoomSchedule = 210;
                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setBlastingPoint", "§cA"));
                item = Item.get(46);
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setBlastingPoint", "§cA"));
                this.player.getInventory().setItem(4, item);
                if (!"".equals(config.getString("blastingPointA").trim())) {
                    canNext = true;
                }
                break;
            case 210: //设置爆破点B
                this.backRoomSchedule = 200;
                this.nextRoomSchedule = 70;
                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setBlastingPoint", "§9B"));
                item = Item.get(46);
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setBlastingPoint", "§9B"));
                this.player.getInventory().setItem(4, item);
                if (!"".equals(config.getString("blastingPointB").trim())) {
                    canNext = true;
                }
                break;
            case 300: //个人战模式 设置随机出生点
                this.backRoomSchedule = 60;
                this.nextRoomSchedule = 70;

                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setRandomSpawn"));

                this.player.getInventory().setItem(4, Item.get(0));

                item = Item.get(138);
                item.setNamedTag(new CompoundTag().putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_addRandomSpawn"));
                this.player.getInventory().setItem(3, item);

                item = Item.get(241, 14);//红色玻璃
                item.setNamedTag(new CompoundTag().putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 114));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_removeRandomSpawn"));
                this.player.getInventory().setItem(5, item);

                if (!config.getStringList("randomSpawns").isEmpty()) {
                    canNext = true;
                }
                break;
            case 400: //征服模式
                break;
        }
        //判断给 下一步/保存 物品
        if (canNext) {
            item = Item.get(340);
            if (this.nextRoomSchedule == 70) {
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 112));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_save"));
            }else {
                item.setNamedTag(new CompoundTag()
                        .putInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG, 111));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_next"));
            }
            this.player.getInventory().setItem(8, item);
        }else {
            this.player.getInventory().clear(8);
        }
        //显示已设置的点
        this.particleEffectTick++;
        if (this.particleEffectTick >= 2) {
            this.particleEffectTick = 0;
        }
        try{
            String[] s = config.getString("waitSpawn").split(":");
            Position pos = new Position(
                    Integer.parseInt(s[0]) + 0.5,
                    Integer.parseInt(s[1]),
                    Integer.parseInt(s[2]) + 0.5,
                    this.level);
            if (this.waitSpawnText == null || this.waitSpawnText.isClosed()) {
                this.waitSpawnText = new EntityText(pos, "§aWait §eSpawn");
                this.waitSpawnText.spawnToAll();
            }
            this.waitSpawnText.teleport(pos);
            this.particleEffect(pos);
        } catch (Exception ignored) {
        }
        try{
            String[] s = config.getString("redSpawn").split(":");
            Position pos = new Position(
                    Integer.parseInt(s[0]) + 0.5,
                    Integer.parseInt(s[1]),
                    Integer.parseInt(s[2]) + 0.5,
                    this.level);
            if (this.redSpawnText == null || this.redSpawnText.isClosed()) {
                this.redSpawnText = new EntityText(pos, "§cRed §eSpawn");
                this.redSpawnText.spawnToAll();
            }
            this.redSpawnText.teleport(pos);
            this.particleEffect(pos);
        } catch (Exception ignored) {
        }
        try{
            String[] s = config.getString("blueSpawn").split(":");
            Position pos = new Position(
                    Integer.parseInt(s[0]) + 0.5,
                    Integer.parseInt(s[1]),
                    Integer.parseInt(s[2]) + 0.5,
                    this.level);
            if (this.blueSpawnText == null || this.blueSpawnText.isClosed()) {
                this.blueSpawnText = new EntityText(pos, "§9Blue §eSpawn");
                this.blueSpawnText.spawnToAll();
            }
            this.blueSpawnText.teleport(pos);
            this.particleEffect(pos);
        } catch (Exception ignored) {
        }
        try{
            List<String> randomSpawns = config.getStringList("randomSpawns");
            Iterator<EntityText> iterator = this.randomSpawnTextList.iterator();
            while (iterator.hasNext()) {
                EntityText entityText = iterator.next();
                if (!randomSpawns.contains(entityText.namedTag.getString("stringPos"))) {
                    entityText.close();
                }
                if (entityText.isClosed()) {
                    iterator.remove();
                }
            }
            for (String string : randomSpawns) {
                this.particleEffect(Position.fromObject(Tools.stringToVector3(string).add(0.5, 0, 0.5), this.level));
                boolean isExist = false;
                for (EntityText entityText : this.randomSpawnTextList) {
                    if (entityText.namedTag.getString("stringPos").equals(string)) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    Position pos = Position.fromObject(Tools.stringToVector3(string).add(0.5, 0, 0.5), this.level);
                    EntityText entityText = new EntityText(pos, "§aRandom §eSpawn");
                    entityText.namedTag.putString("stringPos", string);
                    entityText.spawnToAll();
                    this.randomSpawnTextList.add(entityText);
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public int getSetRoomSchedule() {
        return this.setRoomSchedule;
    }

    public void setRoomSchedule(int setRoomSchedule) {
        this.setRoomSchedule = setRoomSchedule;
    }

    public int getBackRoomSchedule() {
        return this.backRoomSchedule;
    }

    public int getNextRoomSchedule() {
        return this.nextRoomSchedule;
    }

    public boolean isAutoNext() {
        return this.autoNext;
    }

    public void setAutoNext(boolean autoNext) {
        this.autoNext = autoNext;
    }

    private void closeEntity() {
        if (this.waitSpawnText != null) {
            this.waitSpawnText.close();
        }
        if (this.redSpawnText != null) {
            this.redSpawnText.close();
        }
        if (this.blueSpawnText != null) {
            this.blueSpawnText.close();
        }
    }

    private void particleEffect(Vector3 center) {
        if (this.particleEffectTick != 0) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                center.y += 0.2;
                Vector3 v = center.clone();
                v.x += 0.8;
                double x = v.x - center.x;
                double z = v.z - center.z;
                for (int i = 0; i < 360; i += 10) {
                    this.level.addParticleEffect(
                            new Vector3(
                                    x * Math.cos(i) - z * Math.sin(i) + center.x,
                                    center.y + (i * 0.0055),
                                    x * Math.sin(i) + z * Math.cos(i) + center.z),
                            ParticleEffect.REDSTONE_TORCH_DUST);
                    Thread.sleep(15);
                }
            } catch (Exception ignored) {

            }
        });
    }

    @Override
    public void onCancel() {
        this.closeEntity();
        if (this.setRoomSchedule != 70) {
            this.player.sendMessage(this.owner.getLanguage().translateString("admin_setRoom_cancel"));
        }
        if (this.player != null) {
            if (this.owner.isHasTips()) {
                Tips.removeTipsConfig(player.getLevel().getFolderName(), player);
            }
            this.player.setGamemode(this.beforeGameMode);
            if (this.player.getInventory() != null) {
                this.player.getInventory().clearAll();
                this.player.getUIInventory().clearAll();
                this.player.getInventory().setContents(this.playerInventory);
                this.player.getOffhandInventory().setItem(0, this.offHandItem);
            }
        }
        this.owner.setRoomTask.remove(this.player);
    }

}
