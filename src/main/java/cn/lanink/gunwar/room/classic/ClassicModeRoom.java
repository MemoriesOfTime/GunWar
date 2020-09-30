package cn.lanink.gunwar.room.classic;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.ITimeTask;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 房间
 */
public class ClassicModeRoom extends BaseRoom implements ITimeTask {

    public ClassicModeRoom(Level level, Config config) throws RoomLoadException {
        super(level, config);
    }

    @Override
    public List<String> getListeners() {
        return new ArrayList<>(Arrays.asList(
                "RoomLevelProtection",
                "DefaultChatListener",
                "DefaultGameListener",
                "DefaultDamageListener"));
    }

    @Override
    public ITimeTask getTimeTask() {
        return this;
    }

    @Override
    public void timeTask() {
        if (this.getPlayers().size() < 1) {
            this.endGame();
            return;
        }
        if (this.gameTime > 0) {
            this.gameTime--;
        }else {
            Server.getInstance().getScheduler().scheduleTask(this.gunWar, () -> this.roundEnd(0));
            this.gameTime = this.getSetGameTime();
        }
        int red = 0, blue = 0;
        for (int team : this.getPlayers().values()) {
            if (team == 1) {
                red++;
            } else if (team == 2) {
                blue++;
            }
        }
        if (red == 0) {
            Server.getInstance().getScheduler().scheduleTask(this.gunWar, () -> this.roundEnd(2));
            this.gameTime = this.getSetGameTime();
        } else if (blue == 0) {
            Server.getInstance().getScheduler().scheduleTask(this.gunWar, () -> this.roundEnd(1));
            this.gameTime = this.getSetGameTime();
        }
    }

}
