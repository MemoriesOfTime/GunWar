package cn.lanink.gunwar.tasks;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.scheduler.PluginTask;
import net.fap.stage.FStage;

/**
 * @author LT_Name
 */
public class FStageTask extends PluginTask<GunWar> {

    public FStageTask(GunWar gunWar) {
        super(gunWar);
    }

    @Override
    public void onRun(int i) {
        for (BaseRoom room : this.owner.getGameRoomManager().getGameRoomMap().values()) {
            if (room.getStatus() == IRoomStatus.ROOM_STATUS_TASK_NEED_INITIALIZED || room.getStatus() == IRoomStatus.ROOM_STATUS_WAIT) {
                FStage.setLocalStatus("free");
                return;
            }
        }
        FStage.setLocalStatus("run");
    }

    @Override
    public void onCancel() {
        FStage.setLocalStatus("close");
    }

}
