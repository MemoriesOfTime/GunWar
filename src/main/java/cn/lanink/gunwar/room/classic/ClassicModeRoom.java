package cn.lanink.gunwar.room.classic;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.ITimeTask;
import cn.lanink.gunwar.room.base.Team;
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
        if (!this.roundIsEnd) {
            int red = 0, blue = 0;
            for (Team team : this.getPlayers().values()) {
                if (team == Team.RED) {
                    red++;
                } else if (team == Team.BLUE) {
                    blue++;
                }
            }
            if (red == 0) {
                this.roundEnd(2);
                this.gameTime = this.getSetGameTime();
            } else if (blue == 0) {
                this.roundEnd(1);
                this.gameTime = this.getSetGameTime();
            }
        }
    }

}
