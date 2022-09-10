package cn.lanink.gunwar.room.classic;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.room.base.BaseRoundModeRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

/**
 * 房间
 */
public class ClassicModeRoom extends BaseRoundModeRoom {

    public ClassicModeRoom(Level level, Config config) throws RoomLoadException {
        super(level, config);
    }

    @Override
    protected void checkTeamPlayerCount() {
        int red = 0;
        int blue = 0;
        for (Team team : this.getPlayers().values()) {
            if (team == Team.RED) {
                red++;
            } else if (team == Team.BLUE) {
                blue++;
            }
        }
        if (red == 0) {
            this.roundEnd(Team.BLUE);
            this.gameTime = this.getSetGameTime();
        } else if (blue == 0) {
            this.roundEnd(Team.RED);
            this.gameTime = this.getSetGameTime();
        }
    }

}
