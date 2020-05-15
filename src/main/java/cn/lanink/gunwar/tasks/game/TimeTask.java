package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarRoomRoundEndEvent;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.Server;
import cn.nukkit.scheduler.PluginTask;

/**
 * 游戏时间计算
 */
public class TimeTask extends PluginTask<GunWar> {

    private final Room room;

    public TimeTask(GunWar owner, Room room) {
        super(owner);
        this.room = room;
    }

    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
        }
        if (this.room.gameTime > 0) {
            this.room.gameTime--;
        }else {
            Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundEndEvent(this.room, 0));
            this.room.gameTime = this.room.getGameTime();
        }
        int red = 0, blue = 0;
        for (int team : room.getPlayers().values()) {
            if (team == 1) {
                red++;
            }else if (team == 2) {
                blue++;
            }
        }
        if (red == 0) {
            Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundEndEvent(room, 2));
            room.gameTime = room.getGameTime();
        }else if (blue == 0) {
            Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundEndEvent(room, 1));
            room.gameTime = room.getGameTime();
        }
    }

}
