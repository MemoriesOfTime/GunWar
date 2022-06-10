package cn.lanink.gunwar.room.base;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.NonNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 积分配置（商店用）
 *
 * @author LT_Name
 */
public class IntegralConfig {

    @Getter
    private final static EnumMap<IntegralType, Integer> INTEGER_ENUM_MAP = new EnumMap<>(IntegralType.class);

    private IntegralConfig() {
        throw new IllegalStateException("Utility class");
    }

    public static void init(Config config) {
        Map<String, Object> integralMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : (config.get("integral", new HashMap<String, Object>())).entrySet()) {
            integralMap.put(entry.getKey().toUpperCase(), entry.getValue()); //key全部转换为大写
        }
        for (IntegralType integralType : IntegralType.values()) {
            if (integralType == IntegralType.CUSTOM) {
                continue;
            }
            INTEGER_ENUM_MAP.put(integralType, (Integer) integralMap.getOrDefault(integralType.name(), 0));
        }
        if (GunWar.debug) {
            GunWar.getInstance().getLogger().info("积分配置：" + INTEGER_ENUM_MAP);
        }
    }

    public static int getIntegral(@NonNull IntegralType integralType) {
        return INTEGER_ENUM_MAP.getOrDefault(integralType, 0);
    }

    public enum IntegralType {

        /**
         * 自定义 (通用)
         */
        CUSTOM,

        /**
         * 游戏开始时玩家基础积分
         */
        START_BASE_INTEGRAL,

        /**
         * 击杀敌人
         */
        KILL_SCORE,

        /**
         * 击杀队友
         */
        KILL_TEAM_SCORE,

        /**
         * 安放炸弹 （仅爆破模式）
         */
        BOMB_SCORE,

        /**
         * 拆除炸弹 （仅爆破模式）
         */
        DESTROY_SCORE,

        /**
         * 回合胜利
         */
        ROUND_WIN_SCORE,

        /**
         * 回合失败
         */
        ROUND_LOSE_SCORE

    }

}
