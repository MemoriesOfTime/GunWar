package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarPlayerRespawnEvent;
import cn.lanink.gunwar.event.GunWarRoomRoundEndEvent;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.scheduler.Task;

import java.util.Map;

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
            return;
        }
        if (this.room.getPlayers().size() < 1) {
            this.room.endGame(true);
            this.cancel();
            return;
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
            switch (this.room.getGameMode()) {
                case CTF:
                    for (Map.Entry<Player, Integer> entry : this.room.getPlayerRespawnTime().entrySet()) {
                        if (entry.getValue() > 0) {
                            entry.setValue(entry.getValue() - 1);
                            if (entry.getValue() == 0) {
                                owner.getServer().getPluginManager().callEvent(
                                        new GunWarPlayerRespawnEvent(this.room, entry.getKey()));
                                owner.getServer().getScheduler().scheduleDelayedTask(owner, new Task() {
                                    @Override
                                    public void onRun(int i) {
                                        Tools.addSound(entry.getKey(), Sound.RANDOM_ORB);
                                    }
                                }, 10, true);
                            }
                        }
                    }
                    for (int team : this.room.getPlayers().values()) {
                        switch (team) {
                            case 1:
                            case 11:
                                red++;
                                break;
                            case 2:
                            case 12:
                                blue++;
                                break;
                        }
                    }
                    break;
                case CLASSIC:
                default:
                    for (int team : this.room.getPlayers().values()) {
                        if (team == 1) {
                            red++;
                        } else if (team == 2) {
                            blue++;
                        }
                    }
                    break;
            }
            if (red == 0) {
                Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundEndEvent(this.room, 2));
                this.room.gameTime = this.room.getSetGameTime();
            } else if (blue == 0) {
                Server.getInstance().getPluginManager().callEvent(new GunWarRoomRoundEndEvent(this.room, 1));
                this.room.gameTime = this.room.getSetGameTime();
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
