package cn.lanink.gunwar.room.base;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;
import org.jetbrains.annotations.NotNull;

/**
 * 多回合不可重生房间类型
 *
 * @author LT_Name
 */
public abstract class BaseRoundModeRoom extends BaseRoom {

    /**
     * 初始化
     *
     * @param level  游戏世界
     * @param config 配置文件
     */
    public BaseRoundModeRoom(@NotNull Level level, @NotNull Config config) throws RoomLoadException {
        super(level, config);
    }

    //TODO 将一些多回合相关操作移到这个类！

}
