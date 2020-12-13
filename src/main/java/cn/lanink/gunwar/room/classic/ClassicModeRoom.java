package cn.lanink.gunwar.room.classic;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.ITimeTask;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

/**
 * 房间
 */
public class ClassicModeRoom extends BaseRoom {

    public ClassicModeRoom(Level level, Config config) throws RoomLoadException {
        super(level, config);
    }

    @Override
    public ITimeTask getTimeTask() {
        return this;
    }

    @Override
    public void timeTask() {
        super.timeTask();
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
