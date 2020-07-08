package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;

/**
 * @author lt_name
 */
public class FlagTask extends PluginTask<GunWar> {

    private final Room room;

    public FlagTask(GunWar owner, Room room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
            return;
        }
        Player p;
        if (this.room.haveRedFlag != null && this.room.redFlag != null) {
            p = this.room.haveRedFlag;
            this.room.redFlag.setPosition(new Vector3(p.getX(),
                    p.getY() + p.getEyeHeight() + 0.5, p.getZ()));
        }
        if (this.room.haveBlueFlag != null && this.room.blueFlag != null) {
            p = this.room.haveBlueFlag;
            this.room.blueFlag.setPosition(new Vector3(p.getX(),
                    p.getY() + p.getEyeHeight() + 0.5, p.getZ()));
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
