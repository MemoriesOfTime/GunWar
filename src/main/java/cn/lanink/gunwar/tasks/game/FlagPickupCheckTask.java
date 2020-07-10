package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;

/**
 * @author lt_name
 */
public class FlagPickupCheckTask extends PluginTask<GunWar> {

    private int time;
    private final Room room;
    private final EntityFlag entityFlag;
    private final int team;

    public FlagPickupCheckTask(GunWar owner, Room room, EntityFlag entityFlag) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.time = 10;
        this.room = room;
        this.entityFlag = entityFlag;
        entityFlag.setNameTagVisible(true);
        entityFlag.setNameTagAlwaysVisible(true);
        this.team = entityFlag.namedTag.getInt("GunWarTeam");
    }

    @Override
    public void onRun(int i) {
        if (time > 0) {
            entityFlag.setNameTag("§e" + time);
            if (team == 11 && room.haveRedFlag != null) {
                this.cancel();
            }else if (team == 12 && room.haveBlueFlag != null) {
                this.cancel();
            }
            time--;
        }else {
            switch (team) {
                case 11:
                    entityFlag.teleport(new Vector3(room.getRedSpawn().getX(),
                            room.getRedSpawn().getY() + 0.3D,
                            room.getRedSpawn().getZ()));
                    break;
                case 12:
                    entityFlag.teleport(new Vector3(room.getBlueSpawn().getX(),
                            room.getBlueSpawn().getY() + 0.3D,
                            room.getBlueSpawn().getZ()));
                    break;
            }
            this.cancel();
        }
    }

    @Override
    public void cancel() {
        entityFlag.setNameTag("");
        entityFlag.setNameTagVisible(false);
        entityFlag.setNameTagAlwaysVisible(false);
        while (owner.taskList.contains(this.getTaskId())) {
            owner.taskList.remove(this.getTaskId());
        }
        super.cancel();
    }

}
