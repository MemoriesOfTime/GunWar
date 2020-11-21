package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.ui.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.Map;

/**
 * @author lt_name
 */
public class CreateRoomTask extends PluginTask<GunWar> {

    private final Player player;
    private final Level level;
    private final Map<Integer, Item> playerInventory;
    private final Item offHandItem;

    public CreateRoomTask(GunWar owner, Player player) {
        super(owner);
        this.player = player;
        this.level = player.getLevel();
        this.playerInventory = player.getInventory().getContents();
        this.offHandItem = player.getOffhandInventory().getItem(0);
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
    }

    @Override
    public void onRun(int i) {
        if (!this.player.isOnline() ||
                this.player.getLevel() != this.level ||
                !this.owner.createRoomSchedule.containsKey(this.player)) {
            this.cancel();
            return;
        }
        int createRoomSchedule = this.owner.createRoomSchedule.getOrDefault(player, 0);
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
                    this.owner.createRoomSchedule.put(player, 50);
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
                    this.owner.createRoomSchedule.put(player, 60);
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
                    this.owner.createRoomSchedule.put(player, 70);
                }
                break;
            case 70: //完成设置
                player.sendMessage(this.owner.getLanguage().admin_createRoom_setSuccessful);
                config.save(true);
                this.owner.loadRoom(this.level.getFolderName());
                this.cancel();
                break;
        }
    }

    @Override
    public void cancel() {
        if (this.owner.createRoomSchedule.getOrDefault(player, 0) != 70) {
            this.owner.getRoomConfigs().remove(this.level.getFolderName());
            File file = new File(this.owner.getDataFolder() + "/Rooms/" + level + ".yml");
            file.delete();
            this.player.sendMessage(this.owner.getLanguage().admin_createRoom_cancel);
        }
        this.player.getInventory().clearAll();
        this.player.getUIInventory().clearAll();
        this.player.getInventory().setContents(this.playerInventory);
        this.player.getOffhandInventory().setItem(0, this.offHandItem);
        this.owner.createRoomSchedule.remove(this.player);
        super.cancel();
    }

}
