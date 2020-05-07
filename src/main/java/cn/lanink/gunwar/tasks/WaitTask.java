package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarRoomStartEvent;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;


public class WaitTask extends PluginTask<GunWar> {

    private final Room room;

    public WaitTask(GunWar owner, Room room) {
        super(owner);
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 1) {
            this.cancel();
        }
        if (this.room.getPlayers().size() >= 2) {
            if (this.room.waitTime > 0) {
                this.room.waitTime--;
                if (this.room.waitTime <= 5) {
                    Tools.addSound(this.room, Sound.RANDOM_CLICK);
                }
            }else {
                owner.getServer().getPluginManager().callEvent(new GunWarRoomStartEvent(this.room));
                this.cancel();
            }
        }else if (this.room.getPlayers().size() > 0) {
            if (this.room.waitTime != this.room.getWaitTime()) {
                this.room.waitTime = this.room.getWaitTime();
            }
        }else {
            this.room.endGame();
            this.cancel();
        }
    }

}
