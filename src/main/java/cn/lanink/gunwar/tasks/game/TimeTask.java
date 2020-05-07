package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarRoomRoundEndEvent;
import cn.lanink.gunwar.room.Room;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.PluginTask;

/**
 * 游戏时间计算
 */
public class TimeTask extends PluginTask<GunWar> {

    private final String taskName = "TimeTask";
    private final Room room;

    public TimeTask(GunWar owner, Room room) {
        super(owner);
        this.room = room;
    }

    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
        }
        //计时与胜利判断
        if (this.room.gameTime > 0) {
            this.room.gameTime--;
        }else {
            Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundEndEvent(this.room, 0));
            this.room.gameTime = this.room.getGameTime();
        }
    }

    private void sendMessage(String string) {
        for (Player player : this.room.getPlayers().keySet()) {
            player.sendMessage(string);
        }
    }


}
