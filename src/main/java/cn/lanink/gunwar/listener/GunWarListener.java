package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.event.*;
import cn.lanink.gunwar.room.GameMode;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.tasks.VictoryTask;
import cn.lanink.gunwar.tasks.game.FlagTask;
import cn.lanink.gunwar.tasks.game.ScoreBoardTask;
import cn.lanink.gunwar.tasks.game.TimeTask;
import cn.lanink.gunwar.tasks.game.TipTask;
import cn.lanink.gunwar.utils.GameRecord;
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
import cn.nukkit.scheduler.Task;
import tip.messages.NameTagMessage;
import tip.utils.Api;

import java.util.*;

public class GunWarListener implements Listener {

    private final GunWar gunWar;
    private final Language language;

    public GunWarListener(GunWar gunWar) {
        this.gunWar = gunWar;
        this.language = gunWar.getLanguage();
    }

    /**
     * 房间开始事件
     * @param event 事件
     */
    @EventHandler
    public void onRoomStart(GunWarRoomStartEvent event) {
        Room room = event.getRoom();
        room.setMode(2);
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomAssignTeamEvent(room));
        Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundStartEvent(room));
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar, new TimeTask(this.gunWar, room), 20, true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar, new ScoreBoardTask(this.gunWar, room), 18, true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar, new TipTask(this.gunWar, room), 10);
        if (room.getGameMode() == GameMode.CTF) {
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.gunWar,
                    new FlagTask(this.gunWar, room), 10);
        }
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
            NameTagMessage nameTagMessage =
                    new NameTagMessage(player.getLevel().getName(), true, "§c" + player.getName());
            Api.setPlayerShowMessage(player.getName(), nameTagMessage);
        }
        for (Player player : blueTeam) {
            room.getPlayers().put(player, 2);
            player.sendTitle(this.language.teamNameBlue, "", 10, 30, 10);
            NameTagMessage nameTagMessage =
                    new NameTagMessage(player.getLevel().getName(), true, "§9" + player.getName());
            Api.setPlayerShowMessage(player.getName(), nameTagMessage);
        }
    }

    /**
     * 玩家重生事件
     * @param event 事件
     */
    @EventHandler
    public void onPlayerRespawn(GunWarPlayerRespawnEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        Player player = event.getPlayer();
        if (room == null || player == null) return;
        Tools.rePlayerState(player, true);
        player.getInventory().clearAll();
        room.getPlayerHealth().put(player, 20F);
        switch (room.getPlayerMode(player)) {
            case 11:
                room.getPlayers().put(player, 1);
            case 1:
                player.teleport(room.getRedSpawn());
                Tools.giveItem(player, 1);
                break;
            case 12:
                room.getPlayers().put(player, 2);
            case 2:
                player.teleport(room.getBlueSpawn());
                Tools.giveItem(player, 2);
        }
    }

    /**
     * 回合开始事件
     * @param event 事件
     */
    @EventHandler
    public void onRoundStart(GunWarRoomRoundStartEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        if (room == null) return;
        if (room.getGameMode() == GameMode.CTF) {
            room.gameTime = room.getSetGameTime();
            //红方底座
            Skin skin = this.gunWar.getFlagSkin(1);
            CompoundTag nbt = EntityFlagStand.getDefaultNBT(room.getRedSpawn());
            nbt.putFloat("Scale", 1.0F);
            nbt.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            EntityFlagStand entityFlagStand = new EntityFlagStand(room.getRedSpawn().getChunk(), nbt);
            entityFlagStand.setSkin(skin);
            entityFlagStand.spawnToAll();
            room.redFlagStand = entityFlagStand;
            //红方旗帜
            skin = this.gunWar.getFlagSkin(11);
            nbt = EntityFlag.getDefaultNBT(new Vector3(room.getRedSpawn().getX(),
                    room.getRedSpawn().getY() + 0.5D,
                    room.getRedSpawn().getZ()));
            nbt.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            nbt.putFloat("Scale", 1.0F);
            nbt.putInt("GunWarTeam", 1);
            EntityFlag entityFlag = new EntityFlag(room.getRedSpawn().getChunk(), nbt);
            entityFlag.y += 0.5D;
            entityFlag.setSkin(skin);
            entityFlag.spawnToAll();
            room.redFlag = entityFlag;
            //蓝方底座
            skin = this.gunWar.getFlagSkin(2);
            nbt = EntityFlagStand.getDefaultNBT(room.getBlueSpawn());
            nbt.putFloat("Scale", 1.0F);
            nbt.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            entityFlagStand = new EntityFlagStand(room.getRedSpawn().getChunk(), nbt);
            entityFlagStand.setSkin(skin);
            entityFlagStand.spawnToAll();
            room.blueFlagStand = entityFlagStand;
            //蓝方旗帜
            skin = this.gunWar.getFlagSkin(12);
            nbt = EntityFlag.getDefaultNBT(new Vector3(room.getBlueSpawn().getX(),
                    room.getBlueSpawn().getY() + 0.5D,
                    room.getBlueSpawn().getZ()));
            nbt.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId", skin.getSkinId()));
            nbt.putFloat("Scale", 1.0F);
            nbt.putInt("GunWarTeam", 2);
            entityFlag = new EntityFlag(room.getRedSpawn().getChunk(), nbt);
            entityFlag.setSkin(skin);
            entityFlag.spawnToAll();
            room.blueFlag = entityFlag;
        }
        for (Player player : room.getPlayers().keySet()) {
            this.gunWar.getServer().getPluginManager().callEvent(new GunWarPlayerRespawnEvent(room, player));
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
        Tools.cleanEntity(room.getLevel(), true);
        //本回合胜利计算
        if (v == 0) {
            switch (room.getGameMode()) {
                case CTF:
                    if ((room.redScore - room.blueScore) > 0) {
                        room.setMode(3);
                        Server.getInstance().getScheduler().scheduleRepeatingTask(
                                this.gunWar, new VictoryTask(this.gunWar, room, 1), 20);
                        return;
                    }else if ((room.blueScore - room.redScore) > 0) {
                        room.setMode(3);
                        Server.getInstance().getScheduler().scheduleRepeatingTask(
                                this.gunWar, new VictoryTask(this.gunWar, room, 2), 20);
                        return;
                    } else {
                        room.gameTime = room.getSetGameTime() / 5;
                        return;
                    }
                case CLASSIC:
                default:
                    int red = 0, blue = 0;
                    for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                        if (entry.getValue() == 1) {
                            red++;
                        }else if (entry.getValue() == 2) {
                            blue++;
                        }
                    }
                    if (red == blue) {
                        room.redScore++;
                        room.blueScore++;
                        this.sendTitle(room, 0);
                    }else if (red > blue) {
                        room.redScore++;
                        this.sendTitle(room, 1);
                    }else {
                        room.blueScore++;
                        this.sendTitle(room, 2);
                    }
            }
        }else if (v == 1) {
            room.redScore++;
            this.sendTitle(room, 1);
        }else {
            room.blueScore++;
            this.sendTitle(room, 2);
        }
        //房间胜利计算
        int round = room.redScore + room.blueScore;
        if (round >= 5) {
            if ((room.redScore - room.blueScore) > 0) {
                room.setMode(3);
                Server.getInstance().getScheduler().scheduleRepeatingTask(
                        this.gunWar, new VictoryTask(this.gunWar, room, 1), 20);
                return;
            }else if ((room.blueScore - room.redScore) > 0) {
                room.setMode(3);
                Server.getInstance().getScheduler().scheduleRepeatingTask(
                        this.gunWar, new VictoryTask(this.gunWar, room, 2), 20);
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
     * 房间结束事件
     * @param event 事件
     */
    @EventHandler
    public void onRoomEnd(GunWarRoomEndEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        int victory = event.getVictory();
        LinkedList<Player> victoryPlayers = new LinkedList<>();
        LinkedList<Player> defeatPlayers = new LinkedList<>();
        if (room.getPlayers().size() > 0) {
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                if (victory == 1) {
                    if (entry.getValue() == 1 || entry.getValue() == 11) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                }else if (victory == 2) {
                    if (entry.getValue() == 2 || entry.getValue() == 12) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                }
            }
            this.gunWar.getServer().getScheduler().scheduleDelayedTask(this.gunWar, new Task() {
                @Override
                public void onRun(int i) {
                    List<String> vCmds = GunWar.getInstance().getConfig().getStringList("胜利执行命令");
                    List<String> dCmds = GunWar.getInstance().getConfig().getStringList("失败执行命令");
                    if (victoryPlayers.size() > 0 && vCmds.size() > 0) {
                        for (Player player : victoryPlayers) {
                            Tools.cmd(player, vCmds);
                        }
                    }
                    if (defeatPlayers.size() > 0 && dCmds.size() > 0) {
                        for (Player player : defeatPlayers) {
                            Tools.cmd(player, dCmds);
                        }
                    }
                }
            }, 40);
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
        if (room.getGameMode() == GameMode.CTF) {
            room.getPlayerRespawnTime().put(player, 20);
        }
        Player damagePlayer = event.getDamagePlayer();
        GameRecord.addDeaths(player);
        GameRecord.addKills(damagePlayer);
        player.sendTitle(this.language.titleDeathTitle,
                this.language.titleDeathSubtitle.replace("%player%", damagePlayer.getName()),
                10, 30, 10);
        Server.getInstance().getScheduler().scheduleAsyncTask(this.gunWar, new AsyncTask() {
            @Override
            public void onRun() {
                for (Player p : room.getPlayers().keySet()) {
                    p.sendMessage(language.killMessage.replace("%damagePlayer%", damagePlayer.getName())
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
        }else if (room.getPlayerMode(player) == 2) {
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
        nbt.putString("playerName", player.getName());
        EntityPlayerCorpse ent = new EntityPlayerCorpse(player.getChunk(), nbt, room.getPlayerMode(player));
        ent.setSkin(skin);
        ent.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        ent.setGliding(true);
        ent.setRotation(player.getYaw(), 0);
        ent.spawnToAll();
        ent.updateMovement();
    }

}
