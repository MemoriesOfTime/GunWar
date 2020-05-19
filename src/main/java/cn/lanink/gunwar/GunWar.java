package cn.lanink.gunwar;

import cn.lanink.gunwar.command.AdminCommand;
import cn.lanink.gunwar.command.UserCommand;
import cn.lanink.gunwar.listener.PlayerGameListener;
import cn.lanink.gunwar.listener.PlayerJoinAndQuit;
import cn.lanink.gunwar.listener.RoomLevelProtection;
import cn.lanink.gunwar.listener.GunWarListener;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.ui.GuiListener;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.lanink.gunwar.utils.MetricsLite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GunWar extends PluginBase {

    public static String VERSION = "0.0.1-SNAPSHOT git-d3be852";
    private static GunWar gunWar;
    private Language language;
    private Config config;
    private LinkedHashMap<String, Room> rooms = new LinkedHashMap<>();
    private LinkedHashMap<String, Config> roomConfigs = new LinkedHashMap<>();
    private String cmdUser, cmdAdmin;
    private final Skin corpseSkin = new Skin();

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
        File file3 = new File(this.getDataFolder() + "/Language");
        if (!file1.exists() && !file1.mkdirs()) {
            getLogger().error("Rooms 文件夹初始化失败");
        }
        if (!file2.exists() && !file2.mkdirs()) {
            getLogger().error("PlayerInventory 文件夹初始化失败");
        }
        if (!file3.exists() && !file3.mkdirs()) {
            getLogger().warning("Language 文件夹初始化失败");
        }
        this.loadResources();
        getLogger().info("§e开始加载房间");
        this.loadRooms();
        this.cmdUser = this.config.getString("插件命令", "gunwar");
        this.cmdAdmin = this.config.getString("管理命令", "gunwaradmin");
        getServer().getCommandMap().register("", new UserCommand(this.cmdUser));
        getServer().getCommandMap().register("", new AdminCommand(this.cmdAdmin));
        getServer().getPluginManager().registerEvents(new RoomLevelProtection(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(), this);
        getServer().getPluginManager().registerEvents(new PlayerGameListener(this), this);
        getServer().getPluginManager().registerEvents(new GunWarListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        new MetricsLite(this, 7448);
        getLogger().info("§e插件加载完成！欢迎使用！");
    }

    @Override
    public void onDisable() {
        if (this.rooms.values().size() > 0) {
            Iterator<Map.Entry<String, Room>> it = this.rooms.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, Room> entry = it.next();
                if (entry.getValue().getPlayers().size() > 0) {
                    entry.getValue().endGame(false);
                    getLogger().info("§c房间：" + entry.getKey() + " 非正常结束！");
                }else {
                    getLogger().info("§c房间：" + entry.getKey() + " 已卸载！");
                }
                it.remove();
            }
        }
        this.rooms.clear();
        this.roomConfigs.clear();
        getServer().getScheduler().cancelTask(this);
        getLogger().info("§c插件卸载完成！");
    }

    public Language getLanguage() {
        return this.language;
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
                    if (config.getInt("waitTime", 0) == 0 ||
                            config.getInt("gameTime", 0) == 0 ||
                            config.getString("World", null) == null ||
                            config.getString("waitSpawn", null) == null ||
                            config.getString("redSpawn", null) == null ||
                            config.getString("blueSpawn", null) == null) {
                        getLogger().warning("§c房间：" + fileName[0] + " 配置不完整，加载失败！");
                        continue;
                    }
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

    private void loadResources() {
        getLogger().info("§e开始加载资源文件");
        //语言文件
        saveResource("Language/zh_CN.yml", "/Language/zh_CN.yml", false);
        String s = this.config.getString("language", "zh_CN");
        File languageFile = new File(getDataFolder() + "/Language/" + s + ".yml");
        if (languageFile.exists()) {
            getLogger().info("§aLanguage: " + s + " loaded !");
            this.language = new Language(new Config(languageFile, 2));
        }else {
            getLogger().warning("§cLanguage: " + s + " Not found, Load the default language !");
            this.language = new Language(new Config());
        }
        //加载默认尸体皮肤
        BufferedImage skinData = null;
        try {
            skinData = ImageIO.read(this.getResource("skin.png"));
        } catch (IOException ignored) { }
        if (skinData == null) {
            getLogger().error("§c默认尸体皮肤加载失败！请检查插件完整性！");
            return;
        }
        this.corpseSkin.setSkinData(skinData);
        this.corpseSkin.setSkinId("defaultSkin");
        getLogger().info("§e资源文件加载完成");
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    public String getCmdUser() {
        return this.cmdUser;
    }

    public String getCmdAdmin() {
        return this.cmdAdmin;
    }

    public Skin getCorpseSkin() {
        return this.corpseSkin;
    }

    public Config getRoomConfig(Level level) {
        return getRoomConfig(level.getName());
    }

    private Config getRoomConfig(String level) {
        if (this.roomConfigs.containsKey(level)) {
            return this.roomConfigs.get(level);
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
