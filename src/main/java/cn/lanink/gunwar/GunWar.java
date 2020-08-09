package cn.lanink.gunwar;

import cn.lanink.gunwar.command.AdminCommand;
import cn.lanink.gunwar.command.UserCommand;
import cn.lanink.gunwar.lib.scoreboard.IScoreboard;
import cn.lanink.gunwar.lib.scoreboard.ScoreboardDe;
import cn.lanink.gunwar.lib.scoreboard.ScoreboardGt;
import cn.lanink.gunwar.listener.*;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.ui.GuiListener;
import cn.lanink.gunwar.ui.GuiType;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.MetricsLite;
import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GunWar extends PluginBase {

    public static String VERSION = "?";
    private static GunWar gunWar;
    private Language language;
    private Config config, gameRecord;
    private final LinkedHashMap<String, Room> rooms = new LinkedHashMap<>();
    private final HashMap<String, Config> roomConfigs = new HashMap<>();
    private String cmdUser, cmdAdmin;
    private final Skin corpseSkin = new Skin();
    private final HashMap<Integer, Skin> flagSkinMap = new HashMap<>();
    public final LinkedList<Integer> taskList = new LinkedList<>();
    private final HashMap<Integer, GuiType> guiCache = new HashMap<>();
    private IScoreboard scoreboard;
    private boolean hasTips = false;

    public static GunWar getInstance() { return gunWar; }

    @Override
    public void onEnable() {
        getLogger().info("§e插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        if (gunWar == null) gunWar = this;
        getLogger().info("§l§e版本: " + VERSION);
        saveDefaultConfig();
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
        //加载计分板
        try {
            Class.forName("de.theamychan.scoreboard.ScoreboardPlugin");
            this.scoreboard = new ScoreboardDe();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("gt.creeperface.nukkit.scoreboardapi.ScoreboardAPI");
                this.scoreboard = new ScoreboardGt();
            } catch (ClassNotFoundException ignored) {
                getLogger().error("请安装ScoreboardAPI插件！");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        //检查Tips
        try {
            Class.forName("tip.Main");
            if (getServer().getPluginManager().getPlugin("Tips").isDisabled()) {
                throw new Exception("Not Loaded");
            }
            hasTips = true;
        } catch (Exception ignored) {

        }
        this.config = new Config(getDataFolder() + "/config.yml", 2);
        this.gameRecord = new Config(getDataFolder() + "/GameRecord.yml", 2);
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
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new GunWarListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        new MetricsLite(this, 7448);
        getLogger().info("§e插件加载完成！欢迎使用！");
    }

    @Override
    public void onDisable() {
        this.gameRecord.save();
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
        for (int id : this.taskList) {
            getServer().getScheduler().cancelTask(id);
        }
        this.taskList.clear();
        getLogger().info("§c插件卸载完成！");
    }

    public Language getLanguage() {
        return this.language;
    }

    public IScoreboard getScoreboard() {
        return this.scoreboard;
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
                            config.getString("World", "").trim().equals("") ||
                            config.getString("waitSpawn", "").trim().equals("") ||
                            config.getString("redSpawn", "").trim().equals("") ||
                            config.getString("blueSpawn", "").trim().equals("")) {
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
        for (int id : this.taskList) {
            getServer().getScheduler().cancelTask(id);
        }
        this.taskList.clear();
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
        saveResource("Language/zh_CN.yml", false);
        saveResource("Language/ko_KR.yml", false);
        saveResource("Language/en_US.yml", false);
        saveResource("Language/ru_RU.yml", false);
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
        }
        this.corpseSkin.setTrusted(true);
        this.corpseSkin.setSkinData(skinData);
        this.corpseSkin.setSkinId("defaultSkin");
        //加载旗帜皮肤
        saveResource("Resources/Flag/Flag.json", false);
        saveResource("Resources/Flag/FlagStand.json", false);
        saveResource("Resources/Flag/RedFlag.png", false);
        saveResource("Resources/Flag/BlueFlag.png", false);
        File fileJson = new File(getDataFolder() + "/Resources/Flag/FlagStand.json");
        File fileImg = new File(getDataFolder() + "/Resources/Flag/RedFlag.png");
        this.loadFlagSkin(fileImg, fileJson, 1);
        fileJson = new File(getDataFolder() + "/Resources/Flag/Flag.json");
        this.loadFlagSkin(fileImg, fileJson, 11);
        fileImg = new File(getDataFolder() + "/Resources/Flag/BlueFlag.png");
        this.loadFlagSkin(fileImg, fileJson, 12);
        fileJson = new File(getDataFolder() + "/Resources/Flag/FlagStand.json");
        this.loadFlagSkin(fileImg, fileJson, 2);
        getLogger().info("§e资源文件加载完成");
    }

    private void loadFlagSkin(File img, File json, Integer id) {
        BufferedImage skinData;
        try {
            skinData = ImageIO.read(img);
            if (skinData != null) {
                Skin skin = new Skin();
                skin.setTrusted(true);
                skin.setSkinData(skinData);
                skin.setSkinId("flag" + id);
                Map<String, Object> skinJson = new Config(json, 1).getAll();
                String name = null;
                for (Map.Entry<String, Object> entry1 : skinJson.entrySet()) {
                    if (name == null || name.trim().equals("")) {
                        name = entry1.getKey();
                    }else {
                        break;
                    }
                }
                skin.setGeometryName(name);
                skin.setGeometryData(Utils.readFile(json));
                this.flagSkinMap.put(id, skin);
                getLogger().info("§a " + img.getName() + ":" + json.getName() + " 皮肤加载完成！");
            }else {
                getLogger().error("§c " + img.getName() + ":" + json.getName() + " 皮肤加载失败！请检查插件完整性！");
            }
        } catch (IOException e) {
            getLogger().error("§c " + img.getName() + ":" + json.getName() + " 皮肤加载失败！请检查插件完整性！");
        }
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    public boolean isHasTips() {
        return this.hasTips;
    }

    public Config getGameRecord() {
        return this.gameRecord;
    }

    public String getCmdUser() {
        return this.cmdUser;
    }

    public String getCmdAdmin() {
        return this.cmdAdmin;
    }

    public HashMap<Integer, GuiType> getGuiCache() {
        return this.guiCache;
    }

    public Skin getCorpseSkin() {
        return this.corpseSkin;
    }

    public HashMap<Integer, Skin> getFlagSkin() {
        return this.flagSkinMap;
    }

    public Skin getFlagSkin(Integer id) {
        return this.flagSkinMap.get(id);
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
