package cn.lanink.gunwar.tasks.adminroom;

import cn.lanink.gamecore.utils.FileUtil;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityText;
import cn.lanink.gunwar.ui.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author lt_name
 */
public class SetRoomTask extends PluginTask<GunWar> {

    private final Player player;
    private final Level level;
    private final Map<Integer, Item> playerInventory;
    private final Item offHandItem;

    private Entity waitSpawnText;
    private Entity redSpawnText;
    private Entity blueSpawnText;

    private int particleEffectTick = 0;

    public SetRoomTask(GunWar owner, Player player, Level level) {
        super(owner);
        this.player = player;
        this.level = level;
        this.playerInventory = player.getInventory().getContents();
        this.offHandItem = player.getOffhandInventory().getItem(0);
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
    }

    @Override
    public void onRun(int i) {
        if (!this.player.isOnline() ||
                this.player.getLevel() != this.level ||
                !this.owner.setRoomSchedule.containsKey(this.player)) {
            this.cancel();
            return;
        }
        int createRoomSchedule = this.owner.setRoomSchedule.getOrDefault(player, 0);
        Item item;
        if (createRoomSchedule > 10) {
            item = Item.get(340);
            item.setNamedTag(new CompoundTag()
                    .putInt("GunWarItemType", 110));
            item.setCustomName(this.owner.getLanguage().admin_createRoom_back);
            player.getInventory().setItem(0, item);
        }else {
            player.getInventory().clear(0);
        }
        Config config = this.owner.getRoomConfig(player.getLevel());
        switch (createRoomSchedule) {
            case 10: //设置等待出生点
                player.sendTip(this.owner.getLanguage().admin_createRoom_setWaitSpawn);
                item = Item.get(138);
                item.setNamedTag(new CompoundTag()
                        .putInt("GunWarItemType", 113));
                item.setCustomName(this.owner.getLanguage().admin_createRoom_setWaitSpawn);
                player.getInventory().setItem(4, item);
                break;
            case 20: //设置红队出生点
                player.sendTip(this.owner.getLanguage().admin_createRoom_setTeamSpawn
                        .replace("%team%", this.owner.getLanguage().teamNameRed));
                item = Item.get(241, 14);
                item.setNamedTag(new CompoundTag()
                        .putInt("GunWarItemType", 113));
                item.setCustomName(this.owner.getLanguage().admin_createRoom_setTeamSpawn
                        .replace("%team%", this.owner.getLanguage().teamNameRed));
                player.getInventory().setItem(4, item);
                break;
            case 30: //设置蓝队出生点
                player.sendTip(this.owner.getLanguage().admin_createRoom_setTeamSpawn
                        .replace("%team%", this.owner.getLanguage().teamNameBlue));
                item = Item.get(241, 11);
                item.setNamedTag(new CompoundTag()
                        .putInt("GunWarItemType", 113));
                item.setCustomName(this.owner.getLanguage().admin_createRoom_setTeamSpawn
                        .replace("%team%", this.owner.getLanguage().teamNameBlue));
                player.getInventory().setItem(4, item);
                break;
            case 40: //设置更多参数
                player.sendTip(this.owner.getLanguage().admin_createRoom_setMoreParameters);
                item = Item.get(347, 11);
                item.setNamedTag(new CompoundTag()
                        .putInt("GunWarItemType", 113));
                item.setCustomName(this.owner.getLanguage().admin_createRoom_setMoreParameters);
                player.getInventory().setItem(4, item);
                if (config.getInt("waitTime") > 0 &&
                        config.getInt("gameTime") > 0 &&
                        config.getInt("victoryScore") > 0) {
                    this.owner.setRoomSchedule.put(player, 50);
                    GuiCreate.sendAdminPlayersMenu(player);
                }
                break;
            case 50: //设置房间人数
                player.sendTip(this.owner.getLanguage().admin_createRoom_setRoomPlayers);
                item = Item.get(347, 11);
                item.setNamedTag(new CompoundTag()
                        .putInt("GunWarItemType", 113));
                item.setCustomName(this.owner.getLanguage().admin_createRoom_setRoomPlayers);
                player.getInventory().setItem(4, item);
                if (config.getInt("minPlayers") > 0 &&
                        config.getInt("maxPlayers") > 0) {
                    this.owner.setRoomSchedule.put(player, 60);
                    GuiCreate.sendAdminModeMenu(player);
                }
                break;
            case 60: //设置游戏模式
                player.sendTip(this.owner.getLanguage().admin_createRoom_setGameMode);
                item = Item.get(347, 11);
                item.setNamedTag(new CompoundTag()
                        .putInt("GunWarItemType", 113));
                item.setCustomName(this.owner.getLanguage().admin_createRoom_setGameMode);
                player.getInventory().setItem(4, item);
                if (!"".equals(config.getString("gameMode", "").trim())) {
                    this.owner.setRoomSchedule.put(player, 70);
                }
                break;
            case 70: //完成设置
                player.sendMessage(this.owner.getLanguage().admin_createRoom_setSuccessful);
                config.save(true);
                this.closeEntity();
                this.owner.loadRoom(this.level.getFolderName());
                this.cancel();
                return;
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
    }

    @Override
    public void cancel() {
        this.closeEntity();
        if (this.owner.setRoomSchedule.getOrDefault(player, 0) != 70) {
            this.owner.getRoomConfigs().remove(this.level.getFolderName());
            FileUtil.deleteFile(this.owner.getDataFolder() + "/Rooms/" + level + ".yml");
            this.player.sendMessage(this.owner.getLanguage().admin_createRoom_cancel);
        }
        if (this.player != null && this.player.getInventory() != null) {
            this.player.getInventory().clearAll();
            this.player.getUIInventory().clearAll();
            this.player.getInventory().setContents(this.playerInventory);
            this.player.getOffhandInventory().setItem(0, this.offHandItem);
        }
        this.owner.setRoomSchedule.remove(this.player);
        this.owner.setRoomTask.remove(this.player);
        super.cancel();
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

}
