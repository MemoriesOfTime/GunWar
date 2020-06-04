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
    private boolean use = false;

    public TimeTask(GunWar owner, Room room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
    }

    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
        }
        if (this.room.getPlayers().size() < 1) {
            this.room.endGame(true);
            this.cancel();
        }
        if (this.room.gameTime > 0) {
            this.room.gameTime--;
        }else {
            Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundEndEvent(this.room, 0));
            this.room.gameTime = this.room.getSetGameTime();
        }
        if (!use) {
            use = true;
            int red = 0, blue = 0;
            for (int team : room.getPlayers().values()) {
                if (team == 1) {
                    red++;
                } else if (team == 2) {
                    blue++;
                }
            }
            if (red == 0) {
                Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundEndEvent(room, 2));
                room.gameTime = room.getSetGameTime();
            } else if (blue == 0) {
                Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundEndEvent(room, 1));
                room.gameTime = room.getSetGameTime();
            }
            use = false;
        }
    }

    @Override
    public void cancel() {
        while (owner.taskList.contains(this.getTaskId())) {
            owner.taskList.remove(this.getTaskId());
        }
        super.cancel();
    }

}
