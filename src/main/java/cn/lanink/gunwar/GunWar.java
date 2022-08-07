package cn.lanink.gunwar;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.scoreboard.ScoreboardUtil;
import cn.lanink.gamecore.scoreboard.base.IScoreboard;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.command.AdminCommand;
import cn.lanink.gunwar.command.UserCommand;
import cn.lanink.gunwar.gui.GuiListener;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.listener.blasting.BlastingGameListener;
import cn.lanink.gunwar.listener.capturetheflag.CTFDamageListener;
import cn.lanink.gunwar.listener.defaults.*;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.IntegralConfig;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.room.capturetheflag.CTFModeRoom;
import cn.lanink.gunwar.room.classic.ClassicModeRoom;
import cn.lanink.gunwar.room.team.TeamModeRoom;
import cn.lanink.gunwar.supplier.SupplyConfigManager;
import cn.lanink.gunwar.tasks.adminroom.SetRoomTask;
import cn.lanink.gunwar.utils.ItemKillMessageUtils;
import cn.lanink.gunwar.utils.MetricsLite;
import cn.lanink.gunwar.utils.rsnpcx.RsNpcXVariable;
import cn.lanink.gunwar.utils.rsnpcx.RsNpcXVariableV2;
import cn.lanink.gunwar.utils.update.ConfigUpdateUtils;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * @author LT_Name
 */
public class GunWar extends PluginBase {

    public static boolean debug = false;
    public static final String VERSION = "?";
    public static final Random RANDOM = new Random();
    private static GunWar gunWar;

    /**
     * 内置语言支持列表
     */
    private final List<String> supportList = Arrays.asList("chs", "kor", "eng", "rus", "spa");
    private Language language;

    private Config config, gameRecord;

    @SuppressWarnings("rawtypes")
    private static final HashMap<String, Class<? extends BaseGameListener>> LISTENER_CLASS = new HashMap<>();
    private static final LinkedHashMap<String, Class<? extends BaseRoom>> ROOM_CLASS = new LinkedHashMap<>();

    @SuppressWarnings("rawtypes")
    private final HashMap<String, BaseGameListener> gameListeners = new HashMap<>();
    private final LinkedHashMap<String, BaseRoom> rooms = new LinkedHashMap<>();
    private final HashMap<String, Config> roomConfigs = new HashMap<>();

    private String cmdUser;
    private String cmdAdmin;
    @Getter
    private List<String> cmdWhitelist;

    private final HashMap<Integer, Skin> flagSkinMap = new HashMap<>();
    private IScoreboard scoreboard;
    private ItemManage itemManage;
    private boolean hasTips = false;
    private boolean restoreWorld = false;
    @Getter
    private boolean enableAloneHealth = true;
    @Getter
    private boolean enableOtherWeaponDamage = false;

    private String serverWorldPath;
    private String worldBackupPath;
    private String roomConfigPath;

    public final HashMap<Player, SetRoomTask> setRoomTask = new HashMap<>();

    public static GunWar getInstance() { return gunWar; }

    @Override
    public void onLoad() {
        gunWar = this;

        this.serverWorldPath = this.getServer().getFilePath() + "/worlds/";
        this.worldBackupPath = this.getDataFolder() + "/RoomLevelBackup/";
        this.roomConfigPath = this.getDataFolder() + "/Rooms/";

        this.saveDefaultConfig();
        this.config = new Config(this.getDataFolder() + "/config.yml", Config.YAML);
        if (this.config.getBoolean("debug", false)) {
            debug = true;
            this.getLogger().warning("警告：您开启了debug模式！");
            this.getLogger().warning("Warning: You have turned on debug mode!");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {

            }
        }

        ConfigUpdateUtils.updateConfig(this);

        File file1 = new File(this.getDataFolder() + "/Rooms");
        File file2 = new File(this.getDataFolder() + "/PlayerInventory");
        File file3 = new File(this.getDataFolder() + "/Language");
        if (!file1.exists() && !file1.mkdirs()) {
            this.getLogger().error("Rooms 文件夹初始化失败");
        }
        if (!file2.exists() && !file2.mkdirs()) {
            this.getLogger().error("PlayerInventory 文件夹初始化失败");
        }
        if (!file3.exists() && !file3.mkdirs()) {
            this.getLogger().warning("Language 文件夹初始化失败");
        }

        //注册监听器
        registerListener("RoomLevelProtection", RoomLevelProtection.class);
        registerListener("DefaultChatListener", DefaultChatListener.class);
        registerListener("DefaultGameListener", DefaultGameListener.class);
        registerListener("DefaultDamageListener", DefaultDamageListener.class);
        registerListener("CTFDamageListener", CTFDamageListener.class);
        registerListener("BlastingGameListener", BlastingGameListener.class);

        //注册房间类
        registerRoom("classic", ClassicModeRoom.class);
        registerRoom("ctf", CTFModeRoom.class);
        registerRoom("blasting", BlastingModeRoom.class);
        registerRoom("team", TeamModeRoom.class);
    }

    @Override
    public void onEnable() {
        this.getLogger().info("§l§e 插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        this.getLogger().info("§l§e https://github.com/MemoriesOfTime/GunWar");
        this.getLogger().info("§l§e Version: " + VERSION);

        this.scoreboard = ScoreboardUtil.getScoreboard();
        //检查Tips
        try {
            Class.forName("tip.Main");
            if (getServer().getPluginManager().getPlugin("Tips").isDisabled()) {
                throw new Exception("Not Loaded");
            }
            hasTips = true;
        } catch (Exception ignored) {

        }
        try {
            Class.forName("com.smallaswater.npc.variable.VariableManage");
            try {
                com.smallaswater.npc.variable.VariableManage.addVariableV2("GunWarVariable", RsNpcXVariableV2.class);
            } catch (Exception e) {
                com.smallaswater.npc.variable.VariableManage.addVariable("GunWarVariable", RsNpcXVariable.class);
            }
        } catch (Exception ignored) {

        }

        this.restoreWorld = this.config.getBoolean("restoreWorld");
        this.enableAloneHealth = this.config.getBoolean("enableAloneHealth", true);
        this.enableOtherWeaponDamage = this.config.getBoolean("enableOtherWeaponDamage", true);

        IntegralConfig.init(this.config);

        this.gameRecord = new Config(this.getDataFolder() + "/GameRecord.yml", Config.YAML);

        this.loadResources();

        this.getLogger().info("§e开始加载物品");
        this.itemManage = new ItemManage(this);

        SupplyConfigManager.loadAllSupplyConfig();
        ItemKillMessageUtils.load();

        this.cmdUser = this.config.getString("cmdUser", "gunwar");
        this.cmdAdmin = this.config.getString("cmdAdmin", "gunwaradmin");
        this.cmdWhitelist = this.config.getStringList("cmdWhitelist");

        this.getServer().getCommandMap().register("", new UserCommand(this.cmdUser));
        this.getServer().getCommandMap().register("", new AdminCommand(this.cmdAdmin));

        this.getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(this), this);
        this.getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        this.getServer().getPluginManager().registerEvents(new SetRoomListener(this), this);

        this.loadAllListener();

        this.loadAllRoom();

        try {
            new MetricsLite(this, 7448);
        } catch (Exception ignored) {

        }

        this.getLogger().info("§e插件加载完成！欢迎使用！");
    }

    @Override
    public void onDisable() {
        this.gameRecord.save();
        this.unloadAllRoom();
        this.getGameListeners().values().forEach(BaseGameListener::clearListenerRooms);
        this.getLogger().info("§c插件卸载完成！");
    }

    @SuppressWarnings("rawtypes")
    public static void registerListener(String name, Class<? extends BaseGameListener> listenerClass) {
        LISTENER_CLASS.put(name, listenerClass);
    }

    @SuppressWarnings("rawtypes")
    public static HashMap<String, Class<? extends BaseGameListener>> getListenerClass() {
        return LISTENER_CLASS;
    }

    public static void registerRoom(String name, Class<? extends BaseRoom> roomClass) {
        ROOM_CLASS.put(name, roomClass);
    }

    public static LinkedHashMap<String, Class<? extends BaseRoom>> getRoomClass() {
        return ROOM_CLASS;
    }

    public String getServerWorldPath() {
        return this.serverWorldPath;
    }

    public String getWorldBackupPath() {
        return this.worldBackupPath;
    }

    public String getRoomConfigPath() {
        return this.roomConfigPath;
    }

    public boolean isRestoreWorld() {
        return this.restoreWorld;
    }

    public List<String> getSupportList() {
        return new ArrayList<>(supportList);
    }

    public Language getLanguage() {
        return this.language;
    }

    public IScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public ItemManage getItemManage() {
        return this.itemManage;
    }

    @SuppressWarnings("rawtypes")
    public HashMap<String, BaseGameListener> getGameListeners() {
        return this.gameListeners;
    }

    public LinkedHashMap<String, BaseRoom> getRooms() {
        return this.rooms;
    }

    public HashMap<String, Config> getRoomConfigs() {
        return this.roomConfigs;
    }

    @SuppressWarnings("rawtypes")
    public void loadAllListener() {
        for (Map.Entry<String, Class<? extends BaseGameListener>> entry : LISTENER_CLASS.entrySet()) {
            try {
                Constructor<? extends BaseGameListener> constructor = entry.getValue().getConstructor();
                BaseGameListener gameListener = constructor.newInstance();
                gameListener.init(entry.getKey());
                this.loadListener(gameListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void loadListener(BaseGameListener gameListener) {
        this.gameListeners.put(gameListener.getListenerName(), gameListener);
        this.getServer().getPluginManager().registerEvents(gameListener, this);
        if (GunWar.debug) {
            this.getLogger().info("[debug] registerListener: " + gameListener.getListenerName());
        }
    }

    /**
     * 加载所有房间
     */
    private void loadAllRoom() {
        File[] s = new File(getDataFolder() + "/Rooms").listFiles();
        if (s != null) {
            for (File file1 : s) {
                String[] fileName = file1.getName().split("\\.");
                if (fileName.length > 0) {
                    this.loadRoom(fileName[0]);
                }
            }
        }
        this.getLogger().info("§e房间加载完成！当前已加载 " + this.rooms.size() + " 个房间！");
    }

    public void loadRoom(String world) {
        Config config = this.getRoomConfig(world);
        if (config.getInt("waitTime", 0) == 0 ||
                config.getInt("gameTime", 0) == 0 ||
                "".equals(config.getString("waitSpawn", "").trim()) ||
                "".equals(config.getString("redSpawn", "").trim()) ||
                "".equals(config.getString("blueSpawn", "").trim()) ||
                "".equals(config.getString("gameMode", "").trim())) {
            this.getLogger().warning("§c房间：" + world + " 配置不完整，加载失败！");
            return;
        }
        if (Server.getInstance().getLevelByName(world) == null && !Server.getInstance().loadLevel(world)) {
            this.getLogger().warning("§c房间：" + world + " 地图加载失败！");
            return;
        }

        String gameMode = config.getString("gameMode", "classic");
        if (!ROOM_CLASS.containsKey(gameMode)) {
            this.getLogger().warning("§c房间：" + world + " 游戏模式设置错误！没有找到游戏模式: " + gameMode);
            return;
        }
        try {
            Constructor<? extends BaseRoom> constructor = ROOM_CLASS.get(gameMode).getConstructor(Level.class, Config.class);
            BaseRoom baseRoom = constructor.newInstance(Server.getInstance().getLevelByName(world), config);
            baseRoom.setGameMode(gameMode);
            this.rooms.put(world, baseRoom);
            this.getLogger().info("§a房间：" + world + " 已加载！");
        } catch (Exception e) {
            this.getLogger().error("§c加载房间：" + world + " 时出错，请检查配置文件", e);
        }
    }

    /**
     * 卸载所有房间
     */
    public void unloadAllRoom() {
        for (String world : new HashSet<>(this.rooms.keySet())) {
            this.unloadRoom(world);
        }
        this.rooms.clear();
        this.roomConfigs.clear();
    }

    public void unloadRoom(String world) {
        if (this.rooms.containsKey(world)) {
            BaseRoom room = this.rooms.get(world);
            room.endGame();
            room.setStatus(IRoomStatus.ROOM_STATUS_LEVEL_NOT_LOADED);
            this.rooms.remove(world);
            this.getGameListeners().values().forEach(listener -> listener.removeListenerRoom(world));
            this.getLogger().info("§c房间：" + world + " 已卸载！");
        }
        this.roomConfigs.remove(world);
    }

    /**
     * 重载所有房间
     */
    public void reLoadRooms() {
        this.unloadAllRoom();
        this.loadAllRoom();
    }

    private void loadResources() {
        this.getLogger().info("§e开始加载资源文件");
        //语言文件
        this.saveResource("Language/chs.yml", "Language/chs_customize.yml", false);
        this.saveResource("Language/kor.yml", "Language/kor_customize.yml", false);
        this.saveResource("Language/eng.yml", "Language/eng_customize.yml", false);
        this.saveResource("Language/rus.yml", "Language/rus_customize.yml", false);
        this.saveResource("Language/spa.yml", "Language/spa_customize.yml", false);
        String setLang = this.config.getString("language", "chs");
        File languageFile = new File(this.getDataFolder() + "/Language/" + setLang + ".yml");
        if (!languageFile.exists() || supportList.contains(setLang.toLowerCase().trim())) {
            Config config = new Config();
            config.load(this.getResource("Language/" + setLang + ".yml"));
            this.language = new Language(config);
        }else {
            this.language = new Language(new Config(languageFile, Config.YAML));
            Config config = new Config();
            config.load(this.getResource("Language/chs.yml"));
            this.language.update(config);
        }
        this.getLogger().info("§aLanguage: " + setLang + " loaded !");

        //加载旗帜皮肤
        this.saveResource("Resources/Flag/Flag.json", false);
        this.saveResource("Resources/Flag/FlagStand.json", false);
        this.saveResource("Resources/Flag/RedFlag.png", false);
        this.saveResource("Resources/Flag/BlueFlag.png", false);
        File fileJson = new File(this.getDataFolder() + "/Resources/Flag/FlagStand.json");
        File fileImg = new File(this.getDataFolder() + "/Resources/Flag/RedFlag.png");
        this.loadFlagSkin(fileImg, fileJson, 1);
        fileJson = new File(this.getDataFolder() + "/Resources/Flag/Flag.json");
        this.loadFlagSkin(fileImg, fileJson, 11);
        fileImg = new File(this.getDataFolder() + "/Resources/Flag/BlueFlag.png");
        this.loadFlagSkin(fileImg, fileJson, 12);
        fileJson = new File(this.getDataFolder() + "/Resources/Flag/FlagStand.json");
        this.loadFlagSkin(fileImg, fileJson, 2);

        this.getLogger().info("§e资源文件加载完成");
    }

    private void loadFlagSkin(File img, File json, Integer id) {
        BufferedImage skinData;
        try {
            skinData = ImageIO.read(img);
            if (skinData != null) {
                Skin skin = new Skin();
                skin.setSkinResourcePatch(Skin.GEOMETRY_CUSTOM);
                skin.setTrusted(true);
                skin.setSkinData(skinData);
                skin.setSkinId("flag" + id);
                Map<String, Object> skinJson = new Config(json, Config.JSON).getAll();
                String name = null;
                for (Map.Entry<String, Object> entry1 : skinJson.entrySet()) {
                    if (name == null || "".equals(name.trim())) {
                        name = entry1.getKey();
                    }else {
                        break;
                    }
                }
                skin.setGeometryName(name);
                skin.setGeometryData(Utils.readFile(json));
                this.flagSkinMap.put(id, skin);
                this.getLogger().info("§a " + img.getName() + ":" + json.getName() + " 皮肤加载完成！");
            }else {
                this.getLogger().error("§c " + img.getName() + ":" + json.getName() + " 皮肤加载失败！请检查插件完整性！");
            }
        } catch (IOException e) {
            this.getLogger().error("§c " + img.getName() + ":" + json.getName() + " 皮肤加载失败！请检查插件完整性！");
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

    public HashMap<Integer, Skin> getFlagSkin() {
        return this.flagSkinMap;
    }

    public Skin getFlagSkin(Integer id) {
        return this.flagSkinMap.get(id);
    }

    public Config getRoomConfig(Level level) {
        return getRoomConfig(level.getFolderName());
    }

    public Config getRoomConfig(String level) {
        if (this.roomConfigs.containsKey(level)) {
            return this.roomConfigs.get(level);
        }
        Config config = new Config(this.getDataFolder() + "/Rooms/" + level + ".yml", Config.YAML);
        this.roomConfigs.put(level, config);
        return config;
    }

}
