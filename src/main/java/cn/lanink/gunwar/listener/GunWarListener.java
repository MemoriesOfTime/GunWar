package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.event.*;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.tasks.VictoryTask;
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
            }else {
                //蓝
                entry.setValue(2);
                player.sendTitle("§9蓝队", "", 10, 30, 10);
            }
            flag = !flag;
        }
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundStartEvent(room));
    }

    /**
     * 回合开始事件
     * @param event 事件
     */
    @EventHandler
    public void onRoundStart(GunWarRoomRoundStartEvent event) {
        Room room = event.getRoom();
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            Tools.rePlayerState(entry.getKey(), true);
            entry.getKey().getInventory().clearAll();
            room.getPlayerHealth().put(entry.getKey(), 20F);
            if (entry.getValue() == 11) {
                entry.setValue(1);
            }else if (entry.getValue() == 12) {
                entry.setValue(2);
            }
            if (entry.getValue() == 1) {
                entry.getKey().teleport(room.getRedSpawn());
                Tools.giveItem(entry.getKey(), 1);
            }else {
                entry.getKey().teleport(room.getBlueSpawn());
                Tools.giveItem(entry.getKey(), 2);
            }
        }
    }


    /**
     * 房间回合结束事件
     * @param event 事件
     */
    @EventHandler
    public void onRoundEnd(GunWarRoomRoundEndEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        int v = event.getVictory();
        //本回合胜利计算
        if (v == 0) {
            int red = 0, blue = 0;
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                if (entry.getValue() == 1) {
                    red++;
                }else if (entry.getValue() == 2) {
                    blue++;
                }
            }
            if (red == blue) {
                room.redRound++;
                room.blueRound++;
                this.sendTitle(room, 0);
            }else if (red > blue) {
                room.redRound++;
                this.sendTitle(room, 1);
            }else {
                room.blueRound++;
                this.sendTitle(room, 2);
            }
        }else if (v == 1) {
            room.redRound++;
            this.sendTitle(room, 1);
        }else {
            room.blueRound++;
            this.sendTitle(room, 2);
        }
        //房间胜利计算
        int round = room.redRound + room.blueRound;
        if (round >= 5) {
            if ((room.redRound - room.blueRound) > 0) {
                room.setMode(3);
                Server.getInstance().getScheduler().scheduleRepeatingTask(
                        GunWar.getInstance(), new VictoryTask(GunWar.getInstance(), room, 1), 20);
                return;
            }else if ((room.blueRound - room.redRound) > 0) {
                room.setMode(3);
                Server.getInstance().getScheduler().scheduleRepeatingTask(
                        GunWar.getInstance(), new VictoryTask(GunWar.getInstance(), room, 2), 20);
                return;
            }
        }
        Tools.cleanEntity(room.getLevel());
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundStartEvent(room));
    }

    private void sendTitle(Room room, int v) {
        for (Player player : room.getPlayers().keySet()) {
            if (v == 1) {
                player.sendTitle("§c红队获得本轮胜利", "", 10, 20, 10);
            }else if (v == 2) {
                player.sendTitle("§9蓝队获得本轮胜利", "", 10, 20, 10);
            }else {
                player.sendTitle("平局", "", 10, 20, 10);
            }
        }
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
                        arrow += item.getCount();
                    }else if (item.getId() == 332) {
                        snowball += item.getCount();
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
        if (room.getPlayerMode(player) == 1) {
            room.getPlayers().put(player, 11);
        }else {
            room.getPlayers().put(player, 12);
        }
        //Server.getInstance().getPluginManager().callEvent(new GunWarPlayerCorpseSpawnEvent(room, player));
    }

    /**
     * 玩家尸体生成事件
     * @param event 事件
     */
    @EventHandler
    public void onCorpseSpawn(GunWarPlayerCorpseSpawnEvent event) {
        if (event.isCancelled()) return;
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
