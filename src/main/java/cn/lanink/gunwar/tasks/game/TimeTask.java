package cn.lanink.gunwar.tasks.game;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.ITimeTask;
import cn.nukkit.scheduler.PluginTask;

/**
 * 游戏时间计算
 */
public class TimeTask extends PluginTask<GunWar> {

    private final ITimeTask task;

    public TimeTask(GunWar owner, ITimeTask task) {
        super(owner);
        this.task = task;
    }

    public void onRun(int i) {
        if (this.task.getStatus() != IRoomStatus.ROOM_STATUS_GAME) {
            this.cancel();
            return;
        }
        this.task.timeTask();
    }

}
