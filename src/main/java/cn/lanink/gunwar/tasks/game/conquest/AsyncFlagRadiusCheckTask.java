package cn.lanink.gunwar.tasks.game.conquest;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.flag.EntityLongFlag;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.room.conquest.ConquestModeRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 用粒子直观显示占领旗帜有效范围
 *
 * @author LT_Name
 */
public class AsyncFlagRadiusCheckTask extends AsyncTask {

    private int tick = 0;
    private final ConquestModeRoom room;
    private final HashMap<Player, Integer> playerLastIn = new HashMap<>();

    public AsyncFlagRadiusCheckTask(ConquestModeRoom room) {
        this.room = room;
    }

    @Override
    public void onRun() {
        long startTime;
        while (this.room.getStatus() == IRoomStatus.ROOM_STATUS_GAME) {
            startTime = System.currentTimeMillis();

            try {
                this.work(this.tick);
            } catch (Exception e) {
                GunWar.getInstance().getLogger().error("FlagRadiusShowTask Error!", e);
            }

            this.tick++;

            long duration = System.currentTimeMillis() - startTime;
            try {
                Thread.sleep(Math.max(50L - duration, 1));
            } catch (Exception e) {
                GunWar.getInstance().getLogger().error("FlagRadiusShowTask Error!", e);
            }
        }
    }

    private void work(int tick) {
        ArrayList<Player> aFlagPlayers = new ArrayList<>();
        ArrayList<Player> bFlagPlayers = new ArrayList<>();
        ArrayList<Player> cFlagPlayers = new ArrayList<>();
        for (Player player : this.room.getPlayerDataMap().keySet()) {
            if (player.distance(this.room.getConquestPointA()) <= this.room.getConquestPointRadius()) {
                aFlagPlayers.add(player);
            }else if (player.distance(this.room.getConquestPointB()) <= this.room.getConquestPointRadius()) {
                bFlagPlayers.add(player);
            }else if (player.distance(this.room.getConquestPointC()) <= this.room.getConquestPointRadius()) {
                cFlagPlayers.add(player);
            }
        }
        this.checkCapturePoints(aFlagPlayers, this.room.aFlag);
        this.checkCapturePoints(bFlagPlayers, this.room.bFlag);
        this.checkCapturePoints(cFlagPlayers, this.room.cFlag);

        if (tick%20 == 1) {
            this.showParticleEffect();
        }
    }

    private void checkCapturePoints(ArrayList<Player> players, EntityLongFlag flag) {
        ArrayList<Player> redCount = new ArrayList<>();
        ArrayList<Player> blueCount = new ArrayList<>();
        for (Player player : players) {
            int last = this.playerLastIn.getOrDefault(player, tick);
            if (tick - last >= 1) {
                Team playerTeam = this.room.getPlayerTeamAccurate(player);
                if (playerTeam == Team.RED) {
                    redCount.add(player);
                }else if (playerTeam == Team.BLUE) {
                    blueCount.add(player);
                }
            }
            this.playerLastIn.put(player, tick);
        }
        if (redCount.size() > blueCount.size()) {
            flag.addTeamPoints(Team.RED, (redCount.size() - blueCount.size()));
        }else if (blueCount.size() > redCount.size()) {
            flag.addTeamPoints(Team.BLUE, (blueCount.size() - redCount.size()));
        }
    }

    private void showParticleEffect() {
        LinkedList<Vector3> list = Tools.getRoundEdgePoint(this.room.getConquestPointA(), this.room.getConquestPointRadius());
        list.addAll(Tools.getRoundEdgePoint(this.room.getConquestPointB(), this.room.getConquestPointRadius()));
        list.addAll(Tools.getRoundEdgePoint(this.room.getConquestPointC(), this.room.getConquestPointRadius()));
        for (Vector3 vector3 : list) {
            vector3.y += 0.1;
            if (this.room.getLevel().getBlock(vector3).getId() == BlockID.AIR) {
                for (int y = vector3.getFloorY(); y > y - 5; y--) {
                    if (this.room.getLevel().getBlock(new Vector3(vector3.x, y, vector3.z)).getId() != BlockID.AIR) {
                        vector3.y = y + 1.1;
                        break;
                    }
                }
            } else {
                for (int y = vector3.getFloorY(); y < y + 5; y++) {
                    if (this.room.getLevel().getBlock(new Vector3(vector3.x, y, vector3.z)).getId() == BlockID.AIR) {
                        vector3.y = y + 0.1;
                        break;
                    }
                }
            }
            this.room.getLevel().addParticleEffect(vector3, ParticleEffect.REDSTONE_TORCH_DUST);
        }
    }

}
