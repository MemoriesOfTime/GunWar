package cn.lanink.gunwar;

import cn.lanink.gamecore.GameCore;
import cn.lanink.gamecore.listener.BaseGameListener;
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
import cn.lanink.gunwar.room.base.GunWarGameRoomManager;
import cn.lanink.gunwar.room.base.IntegralConfig;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.room.capturetheflag.CTFModeRoom;
import cn.lanink.gunwar.room.classic.ClassicModeRoom;
import cn.lanink.gunwar.room.conquest.ConquestModeRoom;
import cn.lanink.gunwar.room.freeforall.FreeForAllModeRoom;
import cn.lanink.gunwar.room.team.TeamModeRoom;
import cn.lanink.gunwar.supplier.SupplyConfigManager;
import cn.lanink.gunwar.tasks.adminroom.SetRoomTask;
import cn.lanink.gunwar.utils.FlagSkinType;
import cn.lanink.gunwar.utils.ItemKillMessageUtils;
import cn.lanink.gunwar.utils.MetricsLite;
import cn.lanink.gunwar.utils.gamerecord.RankingManager;
import cn.lanink.gunwar.utils.rsnpc.RsNpcVariable;
import cn.lanink.gunwar.utils.rsnpc.RsNpcVariableV2;
import cn.lanink.gunwar.utils.update.ConfigUpdateUtils;
import cn.nukkit.Player;
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
    public static final String VERSION = "1.7.2-SNAPSHOT git-a78cd2b";
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

    @SuppressWarnings("rawtypes")
    private final HashMap<String, BaseGameListener> gameListeners = new HashMap<>();
    private final HashMap<String, Config> roomConfigs = new HashMap<>();

    @Getter
    private GunWarGameRoomManager gameRoomManager;

    private String cmdUser;
    private String cmdAdmin;
    @Getter
    private List<String> cmdWhitelist;

    private final HashMap<FlagSkinType, Skin> flagSkinMap = new HashMap<>();
    private IScoreboard scoreboard;
    private ItemManage itemManage;

    private boolean hasTips = false;
    @Getter
    private boolean hasTeamSystem = false;

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

        this.saveResource("RankingConfig.yml");

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
        GunWarGameRoomManager.registerGameRoomClass("classic", ClassicModeRoom.class);
        GunWarGameRoomManager.registerGameRoomClass("ctf", CTFModeRoom.class);
        GunWarGameRoomManager.registerGameRoomClass("blasting", BlastingModeRoom.class);
        GunWarGameRoomManager.registerGameRoomClass("team", TeamModeRoom.class);
        GunWarGameRoomManager.registerGameRoomClass("ffa", FreeForAllModeRoom.class);
        GunWarGameRoomManager.registerGameRoomClass("conquest", ConquestModeRoom.class);

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
        //对接RsNPC变量
        try {
            Class.forName("com.smallaswater.npc.variable.VariableManage");
            try {
                com.smallaswater.npc.variable.VariableManage.addVariableV2("GunWarVariable", RsNpcVariableV2.class);
            } catch (Exception e) {
                com.smallaswater.npc.variable.VariableManage.addVariable("GunWarVariable", RsNpcVariable.class);
            }
        } catch (Exception ignored) {

        }
        //检查TeamSystem
        try {
            Class.forName("cn.lanink.teamsystem.TeamSystem");
            this.hasTeamSystem = true;
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
        RankingManager.load();

        this.cmdUser = this.config.getString("cmdUser", "gunwar");
        this.cmdAdmin = this.config.getString("cmdAdmin", "gunwaradmin");
        this.cmdWhitelist = this.config.getStringList("cmdWhitelist");

        this.getServer().getCommandMap().register("", new UserCommand(this.cmdUser));
        this.getServer().getCommandMap().register("", new AdminCommand(this.cmdAdmin));

        this.getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(this), this);
        this.getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        this.getServer().getPluginManager().registerEvents(new SetRoomListener(this), this);
        if (GunWar.debug) {
            this.getServer().getPluginManager().registerEvents(new DebugMessageListener(this), this);
        }

        this.loadAllListener();

        this.gameRoomManager = new GunWarGameRoomManager(this);

        this.gameRoomManager.loadAllGameRoom();

        try {
            new MetricsLite(this, 7448);
        } catch (Exception ignored) {

        }

        this.getLogger().info("§e插件加载完成！欢迎使用！");
    }

    @Override
    public void onDisable() {
        this.gameRecord.save();

        SupplyConfigManager.clear();
        ItemKillMessageUtils.clear();
        RankingManager.save();
        RankingManager.clear();

        this.gameRoomManager.unloadAllGameRoom();
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
        this.saveResource("Resources/Flag/Flag.json", true);
        this.saveResource("Resources/Flag/FlagHead.json", true);
        this.saveResource("Resources/Flag/LongFlag.json", true);
        this.saveResource("Resources/Flag/LongFlagNoHead.json", true);
        this.saveResource("Resources/Flag/FlagStand.json", true);
        this.saveResource("Resources/Flag/WhiteFlag.png", true);
        this.saveResource("Resources/Flag/RedFlag.png", true);
        this.saveResource("Resources/Flag/BlueFlag.png", true);

        File whiteFileImg = new File(this.getDataFolder() + "/Resources/Flag/WhiteFlag.png");
        File redFileImg = new File(this.getDataFolder() + "/Resources/Flag/RedFlag.png");
        File blueFileImg = new File(this.getDataFolder() + "/Resources/Flag/BlueFlag.png");
        File flagStandFileJson = new File(this.getDataFolder() + "/Resources/Flag/FlagStand.json");
        File flagFileJson = new File(this.getDataFolder() + "/Resources/Flag/Flag.json");
        File flagHeadJson = new File(this.getDataFolder() + "/Resources/Flag/FlagHead.json");

        this.loadFlagSkin(whiteFileImg, flagStandFileJson, FlagSkinType.FLAG_STAND_WHITE);
        this.loadFlagSkin(whiteFileImg, flagFileJson, FlagSkinType.FLAG_WHITE);
        this.loadFlagSkin(redFileImg, flagStandFileJson, FlagSkinType.FLAG_STAND_RED);
        this.loadFlagSkin(redFileImg, flagFileJson, FlagSkinType.FLAG_RED);
        this.loadFlagSkin(blueFileImg, flagStandFileJson, FlagSkinType.FLAG_STAND_BLUE);
        this.loadFlagSkin(blueFileImg, flagFileJson, FlagSkinType.FLAG_BLUE);
        //长旗杆
        this.loadFlagSkin(
                redFileImg,
                new File(this.getDataFolder() + "/Resources/Flag/LongFlagNoHead.json"),
                FlagSkinType.LONG_FLAGPOLE
        );
        this.loadFlagSkin(whiteFileImg, flagHeadJson, FlagSkinType.FLAG_HEAD_WHITE);
        this.loadFlagSkin(redFileImg, flagHeadJson, FlagSkinType.FLAG_HEAD_RED);
        this.loadFlagSkin(blueFileImg, flagHeadJson, FlagSkinType.FLAG_HEAD_BLUE);

        //加载防御塔皮肤
        this.saveResource("Resources/CrossbowTower/CrossbowTower.png", true);
        this.saveResource("Resources/CrossbowTower/CrossbowTower.json", true);

        Skin cSkin = this.loadSkin(
                new File(this.getDataFolder() + "/Resources/CrossbowTower/CrossbowTower.png"),
                new File(this.getDataFolder() + "/Resources/CrossbowTower/CrossbowTower.json")
        );
        GameCore.MODEL.register("GunWar:CrossbowTower", cSkin);

        this.getLogger().info("§e资源文件加载完成");
    }

    private void loadFlagSkin(File img, File json, FlagSkinType flagSkinType) {
        this.flagSkinMap.put(flagSkinType, this.loadSkin(img, json));
    }

    private Skin loadSkin(File img, File json) {
        BufferedImage skinData;
        try {
            skinData = ImageIO.read(img);
            if (skinData != null) {
                Skin skin = new Skin();
                skin.setTrusted(true);
                skin.setSkinData(skinData);
                String skinId = "GunWar" + img.getName().split("\\.")[0];
                skin.setSkinId(skinId);

                Map<String, Object> skinJson = new Config(json, Config.JSON).getAll();
                String geometryName = null;
                String formatVersion = (String) skinJson.getOrDefault("format_version", "1.10.0");
                skin.setGeometryDataEngineVersion(formatVersion);
                switch (formatVersion) {
                    case "1.16.0":
                    case "1.12.0":
                        geometryName = getGeometryName(json);
                        skin.generateSkinId(skinId);
                        skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"" + geometryName + "\"}}");
                        skin.setGeometryName(geometryName);
                        skin.setGeometryData(Utils.readFile(json));
                        break;
                    case "1.10.0":
                    case "1.8.0":
                    default:
                        for (Map.Entry<String, Object> entry : skinJson.entrySet()) {
                            if (geometryName == null) {
                                if (entry.getKey().startsWith("geometry")) {
                                    geometryName = entry.getKey();
                                }
                            } else {
                                break;
                            }
                        }
                        skin.generateSkinId(skinId);
                        skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"" + geometryName + "\"}}");
                        skin.setGeometryName(geometryName);
                        skin.setGeometryData(Utils.readFile(json));
                        break;
                }
                this.getLogger().info("§a " + img.getName() + ":" + json.getName() + " 皮肤加载完成！");
                return skin;
            }else {
                this.getLogger().error("§c " + img.getName() + ":" + json.getName() + " 皮肤加载失败！请检查插件完整性！");
            }
        } catch (IOException e) {
            this.getLogger().error("§c " + img.getName() + ":" + json.getName() + " 皮肤加载失败！请检查插件完整性！", e);
        }
        return null;
    }

    public String getGeometryName(File file) {
        Config originGeometry = new Config(file, Config.JSON);
        if (!originGeometry.getString("format_version").equals("1.12.0") && !originGeometry.getString("format_version").equals("1.16.0")) {
            return "nullvalue";
        }
        List<Map<String, Object>> geometryList = (List<Map<String, Object>>) originGeometry.get("minecraft:geometry");
        Map<String, Object> geometryMain = geometryList.get(0);
        Map<String, Object> descriptions = (Map<String, Object>) geometryMain.get("description");
        return (String) descriptions.getOrDefault("identifier", "geometry.unknown");
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

    public HashMap<FlagSkinType, Skin> getFlagSkin() {
        return this.flagSkinMap;
    }

    public Skin getFlagSkin(FlagSkinType flagSkinType) {
        return this.flagSkinMap.get(flagSkinType);
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
