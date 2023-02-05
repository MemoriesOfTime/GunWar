package cn.lanink.gunwar.room.base;

import cn.lanink.gunwar.supplier.SupplyConfig;
import cn.lanink.gunwar.supplier.SupplyConfigManager;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author LT_Name
 */
public class RoomConfig {

    private String gameMode = null;

    @Getter
    private final String levelName;
    @Getter
    protected Level level;
    protected final Config config;

    @Getter
    protected int minPlayers;
    @Getter
    protected int maxPlayers;

    protected final String waitSpawn;
    protected final String redSpawn;
    protected final String blueSpawn;

    @Getter
    protected int setWaitTime;
    @Getter
    protected int setGameTime;

    @Getter
    public final int victoryScore; //胜利需要分数

    @Getter
    protected boolean roundEndCleanItem;

    @Getter
    protected ArrayList<String> initialItems = new ArrayList<>();
    @Getter
    protected ArrayList<String> redTeamInitialItems = new ArrayList<>();
    @Getter
    protected ArrayList<String> blueTeamInitialItems = new ArrayList<>();

    @Getter
    private final SupplyType supplyType;
    @Getter
    private final SupplyConfig redTeamSupplyConfig;
    @Getter
    private final SupplyConfig blueTeamSupplyConfig;
    @Getter
    private final int supplyEnableTime;


    @Deprecated
    @Getter
    private final SupplyConfig defaultSupplyConfig;

    public RoomConfig(@NotNull Level level, @NotNull Config config) {
        this.level = level;
        this.levelName = level.getFolderName();
        this.config = config;

        this.minPlayers = config.getInt("minPlayers", 2);
        if (this.minPlayers < 2) {
            this.minPlayers = 2;
        }
        this.maxPlayers = config.getInt("maxPlayers", 10);
        if (this.maxPlayers < this.minPlayers) {
            this.maxPlayers = this.minPlayers;
        }
        this.waitSpawn = config.getString("waitSpawn");
        this.redSpawn = config.getString("redSpawn");
        this.blueSpawn = config.getString("blueSpawn");
        this.setWaitTime = config.getInt("waitTime");
        this.setGameTime = config.getInt("gameTime");
        this.victoryScore = config.getInt("victoryScore", 5);

        this.roundEndCleanItem = config.getBoolean("roundEndCleanItem", true);

        if (!config.exists("initialItems")) {
            ArrayList<String> defaultItems = new ArrayList<>(
                    Arrays.asList(
                            "373:28&1@item",
                            "322&1@item",
                            "DemoMelee&1@weapon_melee",
                            "DemoGrenade&1@weapon_projectile",
                            "DemoFlashbang&1@weapon_projectile",
                            "DemoGun&1@weapon_gun"));
            config.set("initialItems", defaultItems);
            config.save();
        }
        this.initialItems.addAll(config.getStringList("initialItems"));
        if (!config.exists("redTeamInitialItems")) {
            config.set("redTeamInitialItems", new ArrayList<>());
            config.save();
        }
        this.redTeamInitialItems.addAll(config.getStringList("redTeamInitialItems"));
        if (!config.exists("blueTeamInitialItems")) {
            config.set("blueTeamInitialItems", new ArrayList<>());
            config.save();
        }
        this.blueTeamInitialItems.addAll(config.getStringList("blueTeamInitialItems"));

        this.supplyType = SupplyType.valueOf(config.getString("supplyType", "ALL_ROUND").toUpperCase());
        this.defaultSupplyConfig = SupplyConfigManager.getSupplyConfig(config.getString("supply", "DefaultSupply"));
        if (!config.exists("redTeamSupply")) {
            config.set("redTeamSupply", config.getString("supply", "DefaultSupply"));
            config.save();
        }
        this.redTeamSupplyConfig = SupplyConfigManager.getSupplyConfig(config.getString("redTeamSupply"));
        if (!config.exists("blueTeamSupply")) {
            config.set("blueTeamSupply", config.getString("supply", "DefaultSupply"));
            config.save();
        }
        this.blueTeamSupplyConfig = SupplyConfigManager.getSupplyConfig(config.getString("blueTeamSupply"));
        this.supplyEnableTime = config.getInt("supplyEnableTime", 10);  //商店启用时间 单位：秒 仅ONLY_ROUND_START商店模式有效
    }

    /**
     * 保存房间配置
     */
    public void saveConfig() {
        for (String key : this.config.getKeys()) {
            this.config.remove(key);
        }

        this.config.set("minPlayers", this.minPlayers);
        this.config.set("maxPlayers", this.maxPlayers);
        this.config.set("waitSpawn", this.waitSpawn);
        this.config.set("redSpawn", this.redSpawn);
        this.config.set("blueSpawn", this.blueSpawn);
        this.config.set("waitTime", this.setWaitTime);
        this.config.set("gameTime", this.setGameTime);
        this.config.set("victoryScore", this.victoryScore);

        this.config.set("roundEndCleanItem", this.roundEndCleanItem);

        this.config.set("initialItems", this.initialItems);
        this.config.set("redTeamInitialItems", this.redTeamInitialItems);
        this.config.set("blueTeamInitialItems", this.blueTeamInitialItems);

        this.config.set("supplyType", this.supplyType.name());
        this.config.set("redTeamSupply", this.redTeamSupplyConfig.getName());
        this.config.set("blueTeamSupply", this.blueTeamSupplyConfig.getName());
        this.config.set("supplyEnableTime", this.supplyEnableTime);

        this.config.save();
    }

    public final void setGameMode(String gameMode) {
        if (this.gameMode == null) {
            this.gameMode = gameMode;
        }
    }

    public final String getGameMode() {
        return gameMode;
    }

    /**
     * 商店启用模式
     */
    public enum SupplyType {

        /**
         * 不启用商店
         */
        CLOSE,

        /**
         * 启用 全局有效
         */
        ALL_ROUND,

        /**
         * 启用 仅回合开始有效
         */
        ONLY_ROUND_START;

    }

}
