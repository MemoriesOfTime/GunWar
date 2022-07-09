package cn.lanink.gunwar.tasks.game.blasting;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.IntegralConfig;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;

import java.util.HashSet;

/**
 * @author lt_name
 */
public class DemolitionBombTask extends PluginTask<GunWar> {

    public static final HashSet<Player> DEMOLITION_BOMB_PLAYERS = new HashSet<>();
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
        DEMOLITION_BOMB_PLAYERS.add(player);
    }

    @Override
    public void onRun(int i) {
        this.demolitionProgress += this.base;
        if (i%5 == 0) {
            this.player.sendTip(Tools.getShowStringProgress((int) this.demolitionProgress, MAX_DEMOLITION_PROGRESS));
        }
        if (!DEMOLITION_BOMB_PLAYERS.contains(this.player) ||
                this.room.getEntityGunWarBomb() == null ||
                this.room.getEntityGunWarBomb().isClosed() ||
                this.playerPosition.distance(this.player) > 0.5 ||
                this.demolitionProgress >= MAX_DEMOLITION_PROGRESS) {
            this.cancel();
        }
    }

    @Override
    public void onCancel() {
        Server.getInstance().getScheduler().scheduleTask(this.owner, () -> {
            if (this.demolitionProgress >= MAX_DEMOLITION_PROGRESS) {
                Tools.sendTitle(this.room, "",
                        this.owner.getLanguage().translateString("game_blasting_bombHasBeenDismantled"));
                this.room.setRoundIsEnd(true);
                if (this.room.getEntityGunWarBomb() != null) {
                    this.room.getEntityGunWarBomb().close();
                }
                if (this.room.getEntityGunWarBombBlock() != null) {
                    this.room.getEntityGunWarBombBlock().close();
                }
                Server.getInstance().getScheduler().scheduleDelayedTask(this.owner, () -> this.room.roundEnd(Team.BLUE), 60);

                this.room.addPlayerIntegral(this.player, IntegralConfig.getIntegral(IntegralConfig.IntegralType.DESTROY_SCORE));
            }else {
                this.player.sendTitle("",
                        this.owner.getLanguage().translateString("game_blasting_cancelDemolition"));
                this.room.demolitionBombPlayer = null;
            }
            this.player.sendTip(" ");
            DEMOLITION_BOMB_PLAYERS.remove(this.player);
        });
    }

}
