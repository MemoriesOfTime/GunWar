package cn.lanink.gunwar.room.base;

import cn.lanink.gamecore.room.GameRoomManager;
import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * @author LT_Name
 */
public class GunWarGameRoomManager extends GameRoomManager<BaseRoom> {

    private final GunWar gunWar;

    private static final LinkedHashMap<String, Class<? extends BaseRoom>> ROOM_CLASS = new LinkedHashMap<>();

    public GunWarGameRoomManager(GunWar gunWar) {
        super();
        this.gunWar = gunWar;
    }

    public static void registerGameRoomClass(String name, Class<? extends BaseRoom> roomClass) {
        ROOM_CLASS.put(name, roomClass);
    }

    public static LinkedHashMap<String, Class<? extends BaseRoom>> getGameRoomClassMap() {
        return ROOM_CLASS;
    }

    public void loadAllGameRoom() {
        File[] files = new File(this.gunWar.getDataFolder() + "/Rooms").listFiles();
        if (files != null) {
            Arrays.stream(files)
                    .filter(File::isFile)
                    .filter(file -> file.getName().endsWith(".yml"))
                    .filter(file -> file.getName().split("\\.").length > 0)
                    .forEach(file -> this.loadGameRoom(file.getName().split("\\.")[0]));
        }
        this.gunWar.getLogger().info("§e房间加载完成！当前已加载 " + this.getGameRoomMap().size() + " 个房间！");
    }

    @Override
    public boolean loadGameRoom(String level) {
        if (this.hasGameRoom(level)) {
            return false;
        }
        try {
            Config config = this.gunWar.getRoomConfig(level);
            if (config.getInt("waitTime", 0) == 0 ||
                    config.getInt("gameTime", 0) == 0 ||
                    "".equals(config.getString("waitSpawn", "").trim()) ||
                    /*"".equals(config.getString("redSpawn", "").trim()) ||
                    "".equals(config.getString("blueSpawn", "").trim()) ||*/
                    "".equals(config.getString("gameMode", "").trim())) {
                this.gunWar.getLogger().warning("§c房间：" + level + " 配置不完整，加载失败！");
                return false;
            }
            if (Server.getInstance().getLevelByName(level) == null && !Server.getInstance().loadLevel(level)) {
                this.gunWar.getLogger().warning("§c房间：" + level + " 地图加载失败！");
                return false;
            }

            String gameMode = config.getString("gameMode", "classic");
            if (!ROOM_CLASS.containsKey(gameMode)) {
                this.gunWar.getLogger().warning("§c房间：" + level + " 游戏模式设置错误！没有找到游戏模式: " + gameMode);
                return false;
            }

            Constructor<? extends BaseRoom> constructor = ROOM_CLASS.get(gameMode).getConstructor(Level.class, Config.class);
            BaseRoom baseRoom = constructor.newInstance(Server.getInstance().getLevelByName(level), config);
            baseRoom.setGameMode(gameMode);
            baseRoom.saveConfig(); //保存配置，补全缺失的配置项
            this.addGameRoom(level, baseRoom);
            this.gunWar.getLogger().info("§a房间：" + level + " 已加载！");
            return true;
        } catch (Throwable e) {
            this.gunWar.getLogger().error("§c加载房间：" + level + " 时出错，请检查配置文件", e);
            return false;
        }
    }

    public void unloadAllGameRoom() {
        for (String world : new HashSet<>(this.getGameRoomMap().keySet())) {
            this.unloadGameRoom(world);
        }
        this.gunWar.getRoomConfigs().clear();
    }

    public boolean unloadGameRoom(String level) {
        if (this.hasGameRoom(level)) {
            BaseRoom room = this.getGameRoom(level);
            room.endGame();
            room.setStatus(IRoomStatus.ROOM_STATUS_LEVEL_NOT_LOADED);
            this.removeGameRoom(level);
            this.gunWar.getGameListeners().values().forEach(listener -> listener.removeListenerRoom(level));
            this.gunWar.getLogger().info("§c房间：" + level + " 已卸载！");
        }
        this.gunWar.getRoomConfigs().remove(level);
        return true;
    }

    public void reloadAllGameRoom() {
        this.unloadAllGameRoom();
        this.loadAllGameRoom();
    }

    @Override
    public BaseRoom getCanJoinGameRoom() {
        return this.getCanJoinGameRoom(null);
    }

    public BaseRoom getCanJoinGameRoom(Player player) {
        List<BaseRoom> list = this.getCanJoinGameRoomList(player);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<BaseRoom> getCanJoinGameRoomList() {
        return this.getCanJoinGameRoomList((Player) null);
    }

    public List<BaseRoom> getCanJoinGameRoomList(Player player) {
        ArrayList<BaseRoom> list = new ArrayList<>();
        boolean hasPlayer = false;
        for (BaseRoom room : this.getGameRoomMap().values()) {
            if (room.canJoin(player)) {
                list.add(room);
            }
            if (room.getPlayerDataMap().size() > 0) {
                hasPlayer = true;
            }
        }
        if (hasPlayer) {
            list.sort((o1, o2) -> {
                if (o1.getPlayerDataMap().size() > o2.getPlayerDataMap().size()) {
                    return -1;
                } else if (o1.getPlayerDataMap().size() < o2.getPlayerDataMap().size()) {
                    return 1;
                }
                return 0;
            });
        }else {
            Collections.shuffle(list, GunWar.RANDOM);
        }
        return list;
    }

    public List<BaseRoom> getCanJoinGameRoomList(String gameMode) {
        ArrayList<BaseRoom> list = new ArrayList<>();
        boolean hasPlayer = false;
        for (BaseRoom room : this.getGameRoomMap().values()) {
            if (room.canJoin() && room.getGameMode().equalsIgnoreCase(gameMode)) {
                list.add(room);
            }
            if (room.getPlayerDataMap().size() > 0) {
                hasPlayer = true;
            }
        }
        if (hasPlayer) {
            list.sort((o1, o2) -> {
                if (o1.getPlayerDataMap().size() > o2.getPlayerDataMap().size()) {
                    return -1;
                } else if (o1.getPlayerDataMap().size() < o2.getPlayerDataMap().size()) {
                    return 1;
                }
                return 0;
            });
        }else {
            Collections.shuffle(list, GunWar.RANDOM);
        }
        return list;
    }

}
