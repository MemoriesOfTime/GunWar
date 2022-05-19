package cn.lanink.gunwar.supplier;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.utils.exception.supply.SupplyConfigLoadException;
import lombok.Getter;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SupplyConfigManager {

    private static final GunWar GUN_WAR = GunWar.getInstance();

    @Getter
    private static final Map<String, SupplyConfig> SUPPLY_CONFIG_MAP = new HashMap<>();

    private SupplyConfigManager() throws IllegalArgumentException {
        throw new IllegalArgumentException("哎呀！你不能实例化这个类！");
    }

    public static void loadAllSupplyConfig() {
        File dir = new File(GUN_WAR.getDataFolder(), "/Supply/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        GUN_WAR.saveResource("Supply/DefaultSupply/items/DemoGun.yml", GunWar.debug);
        GUN_WAR.saveResource("Supply/DefaultSupply/pages/Main.yml", GunWar.debug);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        AtomicInteger count = new AtomicInteger();
        Arrays.stream(Objects.requireNonNull(files))
                .filter(File::isDirectory)
                .forEach(supplyDir -> {
                    try {
                        SupplyConfig supplyConfig = new SupplyConfig(supplyDir.getName(), supplyDir);
                        SUPPLY_CONFIG_MAP.put(supplyDir.getName(), supplyConfig);
                        count.incrementAndGet();
                    } catch (SupplyConfigLoadException e) {
                        GUN_WAR.getLogger().error("SupplyConfig 加载错误！", e);
                    }
        });
        GUN_WAR.getLogger().info("已成功加载" + count + "个商店配置");
        if (GunWar.debug) {
            GUN_WAR.getLogger().info("[debug] " + SUPPLY_CONFIG_MAP);
        }
    }

    public static SupplyConfig getSupplyConfig(String supply) {
        return SUPPLY_CONFIG_MAP.get(supply);
    }

}