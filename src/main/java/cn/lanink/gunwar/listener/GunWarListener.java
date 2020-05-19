package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.event.*;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.tasks.game.ScoreBoardTask;
import cn.lanink.gunwar.tasks.VictoryTask;
import cn.lanink.gunwar.tasks.game.TimeTask;
import cn.lanink.gunwar.tasks.game.TipTask;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class GunWarListener implements Listener {

    private final Language language = GunWar.getInstance().getLanguage();

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
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                GunWar.getInstance(), new ScoreBoardTask(GunWar.getInstance(), room), 10, true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                GunWar.getInstance(), new TipTask(GunWar.getInstance(), room), 10, true);
    }

    /**
     * 玩家分配队伍事件
     * @param event 事件
     */
    @EventHandler
    public void onAssignTeam(GunWarRoomAssignTeamEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        ArrayList<Player> redTeam = new ArrayList<>();
        ArrayList<Player> blueTeam = new ArrayList<>();
        ArrayList<Player> noTeam = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            switch (entry.getValue()) {
                case 1:
                    redTeam.add(entry.getKey());
                    break;
                case 2:
                    blueTeam.add(entry.getKey());
                    break;
                default:
                    noTeam.add(entry.getKey());
                    break;
            }
        }
        //队伍平衡
        Random random = new Random();
        Player cache;
        while (true) {
            if (noTeam.size() > 0) {
                for (Player player : noTeam) {
                    if (redTeam.size() > blueTeam.size()) {
                        blueTeam.add(player);
                    }else {
                        redTeam.add(player);
                    }
                }
                noTeam.clear();
            }
            if (redTeam.size() != blueTeam.size()) {
                if ((redTeam.size() - blueTeam.size()) == 1 || (blueTeam.size() - redTeam.size() == 1)) {
                    break;
                }
                if (redTeam.size() > blueTeam.size()) {
                    cache = redTeam.get(random.nextInt(redTeam.size()));
                    redTeam.remove(cache);
                    blueTeam.add(cache);
                }else {
                    cache = blueTeam.get(random.nextInt(blueTeam.size()));
                    blueTeam.remove(cache);
                    redTeam.add(cache);
                }
            }else {
                break;
            }
        }
        for (Player player : redTeam) {
            room.getPlayers().put(player, 1);
            player.sendTitle(this.language.teamNameRed, "", 10, 30, 10);
        }
        for (Player player : blueTeam) {
            room.getPlayers().put(player, 2);
            player.sendTitle(this.language.teamNameBlue, "", 10, 30, 10);
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
        Tools.cleanEntity(room.getLevel());
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
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundStartEvent(room));
    }

    private void sendTitle(Room room, int v) {
        for (Player player : room.getPlayers().keySet()) {
            if (v == 1) {
                player.sendTitle(this.language.roundVictoryRed, "", 10, 20, 10);
            }else if (v == 2) {
                player.sendTitle(this.language.roundVictoryBlue, "", 10, 20, 10);
            }else {
                player.sendTitle(this.language.roundVictoryDraw, "", 10, 20, 10);
            }
        }
    }

    /**
     * 玩家伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onPlayerDamage(GunWarPlayerDamageEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        Player player = event.getPlayer();
        Player damagePlayer = event.getDamagePlayer();
        float damage = event.getDamage();
        float health = room.getPlayerHealth().getOrDefault(player, 0F);
        float nowHealth = health - damage;
        if (nowHealth <= 0) {
            room.getPlayerHealth().put(player, 0F);
            Server.getInstance().getPluginManager().callEvent(new GunWarPlayerDeathEvent(room, player, damagePlayer));
        }else {
            room.getPlayerHealth().put(player, nowHealth);
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
        String damagePlayer = event.getDamagePlayer() != null ? event.getDamagePlayer().getName() : "unknown";
        player.sendTitle(this.language.titleDeathTitle,
                this.language.titleDeathSubtitle.replace("%player%", damagePlayer),
                10, 30, 10);
        Server.getInstance().getScheduler().scheduleAsyncTask(GunWar.getInstance(), new AsyncTask() {
            @Override
            public void onRun() {
                for (Player p : room.getPlayers().keySet()) {
                    p.sendMessage(language.killMessage.replace("%damagePlayer%", damagePlayer)
                            .replace("%player%", player.getName()));
                }
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
        Skin skin = player.getSkin();
        switch(skin.getSkinData().data.length) {
            case 8192:
            case 16384:
            case 32768:
            case 65536:
                break;
            default:
                skin = GunWar.getInstance().getCorpseSkin();
        }
        nbt.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        nbt.putFloat("Scale", -1.0F);
        EntityPlayerCorpse ent = new EntityPlayerCorpse(player.getChunk(), nbt, room.getPlayerMode(player));
        ent.setSkin(skin);
        ent.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        ent.setGliding(true);
        ent.setRotation(player.getYaw(), 0);
        ent.spawnToAll();
        ent.updateMovement();
    }

}
