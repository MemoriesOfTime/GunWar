package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.PluginTask;

import java.util.Map;


public class VictoryTask extends PluginTask<GunWar> {

    private final Room room;
    private int victoryTime;

    public VictoryTask(GunWar owner, Room room, int victory) {
        super(owner);
        this.room = room;
        this.victoryTime = 10;
        this.room.victory = victory;
        for (Player player : room.getPlayers().keySet()) {
            if (victory == 1) {
                player.sendTitle("§c红队获得胜利", "", 10, 30, 20);
            }else if (victory == 2) {
                player.sendTitle("§9蓝队获得胜利", "", 10, 30, 20);
            }
        }
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 3) {
            this.cancel();
        }
        if (this.victoryTime < 1) {
            this.room.endGame();
            this.cancel();
        }else {
            this.victoryTime--;
/*            owner.getServer().getScheduler().scheduleAsyncTask(GunWar.getInstance(), new AsyncTask() {
                @Override
                public void onRun() {
                    for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                        if (entry.getValue() != 0) {
                            if (room.victory == 1 && entry.getValue() == 1) {
                                Tools.spawnFirework(entry.getKey());
                            }else if (room.victory == 2 && entry.getValue() == 2) {
                                Tools.spawnFirework(entry.getKey());
                            }
                        }
                    }
                }
            });*/
        }
    }



}
