package cn.lanink.gunwar.tasks.game.blasting;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;

/**
 * @author lt_name
 */
public class DemolitionBombTask extends PluginTask<GunWar> {

    private final BlastingModeRoom room;
    private final Player player;
    private final Vector3 playerPosition;
    private final double base;
    private double demolitionProgress;
    public static final int MAX_DEMOLITION_PROGRESS = 50;

    public DemolitionBombTask(BlastingModeRoom room, Player player, double base) {
        super(GunWar.getInstance());
        this.room = room;
        this.player = player;
        this.playerPosition = player.clone();
        this.base = base;
    }

    @Override
    public void onRun(int i) {
        this.demolitionProgress += this.base;
        if (i%5 == 0) {
            this.player.sendTip(Tools.getShowStringProgress((int) this.demolitionProgress, MAX_DEMOLITION_PROGRESS));
        }
        if (this.playerPosition.distance(this.player) > 0.5 ||
                this.demolitionProgress >= MAX_DEMOLITION_PROGRESS) {
            this.cancel();
        }
    }

    @Override
    public void cancel() {
        if (this.demolitionProgress >= MAX_DEMOLITION_PROGRESS) {
            Tools.sendTitle(this.room, "", "§a炸弹已被拆除");
            this.room.getEntityGunWarBomb().close();
            this.room.getEntityGunWarBombBlock().close();
            Server.getInstance().getScheduler().scheduleDelayedTask(this.owner, () -> {
                this.room.roundEnd(2);
                this.room.setDemolitionBombPlayer(null);
                }, 60);
        }else {
            this.player.sendTitle("", "§c取消拆除");
        }
        this.player.sendTip(" ");
        super.cancel();
    }
}
