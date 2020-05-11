package cn.lanink.gunwar;

import cn.lanink.gunwar.command.AdminCommand;
import cn.lanink.gunwar.command.UserCommand;
import cn.lanink.gunwar.listener.PlayerGameListener;
import cn.lanink.gunwar.listener.PlayerJoinAndQuit;
import cn.lanink.gunwar.listener.RoomLevelProtection;
import cn.lanink.gunwar.listener.GunWarListener;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.ui.GuiListener;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.lanink.gunwar.utils.MetricsLite;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GunWar extends PluginBase {

    public static String VERSION = "0.0.1-SNAPSHOT git-3d3a885";
    private static GunWar gunWar;
    private Config config;
    private LinkedHashMap<String, Room> rooms = new LinkedHashMap<>();
    private LinkedHashMap<String, Config> roomConfigs = new LinkedHashMap<>();

    public static GunWar getInstance() { return gunWar; }

    @Override
    public void onEnable() {
        getLogger().info("§e插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        if (gunWar == null) gunWar = this;
        getLogger().info("§l§e版本: " + VERSION);
        saveDefaultConfig();
        this.config = new Config(getDataFolder() + "/config.yml", 2);
        File file1 = new File(this.getDataFolder() + "/Rooms");
        File file2 = new File(this.getDataFolder() + "/PlayerInventory");
        if (!file1.exists() && !file1.mkdirs()) {
            getLogger().error("Rooms 文件夹初始化失败");
        }
        if (!file2.exists() && !file2.mkdirs()) {
            getLogger().error("PlayerInventory 文件夹初始化失败");
        }
        getLogger().info("§e开始加载房间");
        this.loadRooms();
        getServer().getCommandMap().register("", new UserCommand(this.config.getString("插件命令", "gunwar")));
        getServer().getCommandMap().register("", new AdminCommand(this.config.getString("管理命令", "gunwaradmin")));
        getServer().getPluginManager().registerEvents(new RoomLevelProtection(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(), this);
        getServer().getPluginManager().registerEvents(new PlayerGameListener(), this);
        getServer().getPluginManager().registerEvents(new GunWarListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        new MetricsLite(this, 7448);
        getLogger().info("§a加载完成");
    }

    public LinkedHashMap<String, Room> getRooms() {
        return this.rooms;
    }

    /**
     * 加载所有房间
     */
    private void loadRooms() {
        File[] s = new File(getDataFolder() + "/Rooms").listFiles();
        if (s != null) {
            for (File file1 : s) {
                String[] fileName = file1.getName().split("\\.");
                if (fileName.length > 0) {
                    Config config = getRoomConfig(fileName[0]);
                    /*if (config.getInt("等待时间", 0) == 0 ||
                            config.getInt("游戏时间", 0) == 0 ||
                            config.getString("出生点", null) == null ||
                            config.getStringList("goldSpawn").size() < 1 ||
                            config.getInt("goldSpawnTime", 0) == 0 ||
                            config.getString("World", null) == null) {
                        getLogger().warning("§c房间：" + fileName[0] + " 配置不完整，加载失败！");
                        continue;
                    }*/
                    Room room = new Room(config);
                    this.rooms.put(fileName[0], room);
                    getLogger().info("§a房间：" + fileName[0] + " 已加载！");
                }
            }
        }
        getLogger().info("§e房间加载完成！当前已加载 " + this.rooms.size() + " 个房间！");
    }

    /**
     * 卸载所有房间
     */
    public void unloadRooms() {
        if (this.rooms.values().size() > 0) {
            Iterator<Map.Entry<String, Room>> it = this.rooms.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, Room> entry = it.next();
                entry.getValue().endGame();
                getLogger().info("§c房间：" + entry.getKey() + " 已卸载！");
                it.remove();
            }
            this.rooms.clear();
        }
        if (this.roomConfigs.values().size() > 0) {
            this.roomConfigs.clear();
        }
        getServer().getScheduler().cancelTask(this);
    }

    /**
     * 重载所有房间
     */
    public void reLoadRooms() {
        this.unloadRooms();
        this.loadRooms();
    }

    public Config getRoomConfig(Level level) {
        return getRoomConfig(level.getName());
    }

    private Config getRoomConfig(String level) {
        if (this.roomConfigs.containsKey(level)) {
            return this.roomConfigs.get(level);
        }
        if (!new File(getDataFolder() + "/Rooms/" + level + ".yml").exists()) {
            saveResource("room.yml", "/Rooms/" + level + ".yml", false);
        }
        Config config = new Config(getDataFolder() + "/Rooms/" + level + ".yml", 2);
        this.roomConfigs.put(level, config);
        return config;
    }

    public void roomSetWaitSpawn(Player player, Config config) {
        String spawn = player.getFloorX() + ":" + player.getFloorY() + ":" + player.getFloorZ();
        String world = player.getLevel().getName();
        config.set("World", world);
        config.set("waitSpawn", spawn);
        config.save();
    }

    public void roomSetRedSpawn(Player player, Config config) {
        String spawn = player.getFloorX() + ":" + player.getFloorY() + ":" + player.getFloorZ();
        config.set("redSpawn", spawn);
        config.save();
    }

    public void roomSetBlueSpawn(Player player, Config config) {
        String spawn = player.getFloorX() + ":" + player.getFloorY() + ":" + player.getFloorZ();
        config.set("blueSpawn", spawn);
        config.save();
    }

    public void roomSetWaitTime(Integer waitTime, Config config) {
        config.set("waitTime", waitTime);
        config.save();
    }

    public void roomSetGameTime(Integer gameTime, Config config) {
        config.set("gameTime", gameTime);
        config.save();
    }

}
