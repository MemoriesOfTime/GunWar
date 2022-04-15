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
    protected ArrayList<String> initialItems = new ArrayList<>();
    @Getter
    protected ArrayList<String> redTeamInitialItems = new ArrayList<>();
    @Getter
    protected ArrayList<String> blueTeamInitialItems = new ArrayList<>();

    @Getter
    private final int defaultIntegral; //玩家初始积分

    @Getter
    private final SupplyConfig supplyConfig;

    public RoomConfig(@NotNull Level level, @NotNull Config config) {
        this.level = level;
        this.levelName = level.getFolderName();
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

        this.defaultIntegral = config.getInt("defaultIntegral", 1000);
        this.supplyConfig = SupplyConfigManager.getSupplyConfig(config.getString("supply", "DefaultSupply"));
    }

    public final void setGameMode(String gameMode) {
        if (this.gameMode == null) {
            this.gameMode = gameMode;
        }
    }

    public final String getGameMode() {
        return gameMode;
    }

}
