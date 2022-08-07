package cn.lanink.gunwar.supplier;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.supplier.items.SupplyItemConfig;
import cn.lanink.gunwar.supplier.pages.SupplyPageConfig;
import cn.lanink.gunwar.utils.exception.supply.SupplyConfigLoadException;
import cn.nukkit.utils.Config;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public class SupplyConfig {

    private static final GunWar GUN_WAR = GunWar.getInstance();

    private final String dirName;
    private final ImmutableMap<String, SupplyPageConfig> pageConfigMap;
    private final ImmutableMap<String, SupplyItemConfig> itemConfigMap;
    private SupplyPageConfig defaultPageConfig;

    public SupplyConfig(@NotNull String dirName, @NotNull File path) throws SupplyConfigLoadException {
        this.dirName = dirName;
        File[] childDir = path.listFiles();
        if (childDir == null) {
            throw new SupplyConfigLoadException("加载" + dirName + "失败!");
        }

        childDir = Arrays.stream(childDir).filter(File::isDirectory).toArray(File[]::new);

        if (childDir.length != 2 || !Arrays.asList("items", "pages").contains(childDir[0].getName()) ||
                !Arrays.asList("items", "pages").contains(childDir[1].getName())) {
            throw new SupplyConfigLoadException("加载" + dirName + "失败!");
        }
        File itemsPath = new File(path, "items");
        File[] itemsFiles = itemsPath.listFiles();
        File pagesPath = new File(path, "pages");
        File[] pagesFiles = pagesPath.listFiles();
        if (itemsFiles == null || pagesFiles == null) {
            throw new SupplyConfigLoadException("加载" + dirName + "失败!");
        }

        ImmutableMap.Builder<String, SupplyItemConfig> itemConfigMapBuilder = ImmutableMap.builder();
        Arrays.stream(itemsFiles)
                .filter(this::checkItemFileCorrect)
                .forEach(itemFile -> {
            String fileName = itemFile.getName().split("\\.")[0];
                    try {
                        itemConfigMapBuilder.put(fileName, new SupplyItemConfig(fileName, itemFile));
                    } catch (SupplyConfigLoadException e) {
                        GunWar.getInstance().getLogger().error("SupplyItemConfig 加载错误！", e);
                    }
                });
        this.itemConfigMap = itemConfigMapBuilder.build();

        ImmutableMap.Builder<String, SupplyPageConfig> supplyPageConfigBuilder = ImmutableMap.builder();
        Arrays.stream(pagesFiles)
                .filter(this::checkPageFileCorrect)
                .forEach(pageFile -> {
                    String fileName = pageFile.getName().split("\\.")[0];
                    SupplyPageConfig pageConfig = new SupplyPageConfig(fileName, pageFile, this);
                    supplyPageConfigBuilder.put(fileName, pageConfig);
                    Config config = new Config(pageFile, 2);
                    if (config.getBoolean("default", false)) {
                        this.defaultPageConfig = pageConfig;
                    }
                });
        this.pageConfigMap = supplyPageConfigBuilder.build();
        if (this.defaultPageConfig == null) {
            throw new SupplyConfigLoadException("商店供给:" + dirName + " 无默认界面!");
        }
    }

    /**
     * @return 返回识别名称（文件夹名称）
     */
    public String getName() {
        return dirName;
    }

    private boolean checkItemFileCorrect(File file) {
        if (!file.getName().endsWith(".yml")) {
            return false;
        }
        Config config = new Config(file, Config.YAML);
        List<String> authorizedKey = Arrays.asList("title", "subTitle", "items", "needIntegral", "pos");
        for (String necessary : authorizedKey) {
            if (!config.exists(necessary)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPageFileCorrect(File file) {
        if (!file.getName().endsWith(".yml")) {
            return false;
        }
        Config config = new Config(file, 2);
        List<String> authorizedKey = Arrays.asList("title", "linkItems", "items", "default");
        if (config.getAll().keySet().size() != authorizedKey.size() &&
                (config.getAll().containsKey("linkItem") || config.getAll().containsKey("default"))) {
            return false;
        }
        for (Map.Entry<String, Object> entry : config.getAll().entrySet()) {
            if (!authorizedKey.contains(entry.getKey())) {
                return false;
            }
        }
        return true;
    }

}