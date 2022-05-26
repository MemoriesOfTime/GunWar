package cn.lanink.gunwar.room.base;

import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.NonNull;

import java.util.EnumMap;
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
        Map<String, Object> integralMap = (Map<String, Object>) config.get("integral");
        for (Map.Entry<IntegralType, Integer> entry : INTEGER_ENUM_MAP.entrySet()) {
            entry.setValue((Integer) integralMap.getOrDefault(entry.getKey().name().toUpperCase(), 0));
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
