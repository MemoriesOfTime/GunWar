package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.event.*;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.tasks.game.TimeTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;

import java.util.Map;

public class GunWarListener implements Listener {

    /**
     * 房间开始事件
     * @param event 事件
     */
    @EventHandler
    public void onRoomStart(GunWarRoomStartEvent event) {
        Room room = event.getRoom();
        room.setMode(2);
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomAssignTeamEvent(room));
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                GunWar.getInstance(), new TimeTask(GunWar.getInstance(), room), 20, true);
    }

    /**
     * 玩家分配队伍事件
     * @param event 事件
     */
    @EventHandler
    public void onAssignTeam(GunWarRoomAssignTeamEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        boolean flag = true;
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            Player player = entry.getKey();
            if (flag) {
                //红
                entry.setValue(1);
                player.sendTitle("§c红队", "", 10, 30, 10);
                player.teleport(room.getRedSpawn());
                Tools.giveItem(player, 1);
            }else {
                //蓝
                entry.setValue(2);
                player.sendTitle("§9蓝队", "", 10, 30, 10);
                player.teleport(room.getBlueSpawn());
                Tools.giveItem(player, 2);
            }
            flag = !flag;
        }
    }

    /**
     * 房间回合结束事件
     * @param event 事件
     */
    @EventHandler
    public void onRoundEnd(GunWarRoomRoundEndEvent event) {

    }

    /**
     * 玩家死亡事件
     * @param event 事件
     */
    @EventHandler
    public void onPlayerDeath(GunWarPlayerDeathEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        Player player = event.getPlayer();
        Server.getInstance().getScheduler().scheduleAsyncTask(GunWar.getInstance(), new AsyncTask() {
            @Override
            public void onRun() {
                int arrow = 0;
                int snowball = 0;
                for (Item item : player.getInventory().getContents().values()) {
                    if (item.getId() == 262) {
                        arrow++;
                    }else if (item.getId() == 332) {
                        snowball++;
                    }
                }
                if (arrow > 0) {
                    player.getLevel().dropItem(player, Item.get(262, 0, arrow/2));
                }
                if (snowball > 0) {
                    player.getLevel().dropItem(player, Item.get(332, 0, snowball/2));
                }
                player.getInventory().clearAll();
            }
        });
        player.getLevel().addSound(player, Sound.GAME_PLAYER_DIE);
        player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, true));
        player.setGamemode(3);
        room.getPlayers().put(player, 0);
        Server.getInstance().getPluginManager().callEvent(new GunWarPlayerCorpseSpawnEvent(room, player));
    }

    /**
     * 玩家尸体生成事件
     * @param event 事件
     */
    @EventHandler
    public void onCorpseSpawn(GunWarPlayerCorpseSpawnEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        Player player = event.getPlayer();
        CompoundTag nbt = EntityPlayerCorpse.getDefaultNBT(player);
        nbt.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", player.getSkin().getSkinData().data)
                .putString("ModelId", player.getSkin().getSkinId()));
        nbt.putFloat("Scale", -1.0F);
        EntityPlayerCorpse ent = new EntityPlayerCorpse(player.getChunk(), nbt);
        ent.setSkin(player.getSkin());
        ent.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        ent.setGliding(true);
        ent.setRotation(player.getYaw(), 0);
        ent.spawnToAll();
        ent.updateMovement();
    }

}
