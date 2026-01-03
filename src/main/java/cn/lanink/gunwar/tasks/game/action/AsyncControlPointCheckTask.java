package cn.lanink.gunwar.tasks.game.action;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.flag.EntityLongFlag;
import cn.lanink.gunwar.room.action.ActionModeRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 异步控制点检查任务
 * 用于检查控制点范围内的玩家并更新占领进度
 *
 * @author LT_Name
 */
public class AsyncControlPointCheckTask extends AsyncTask {

    private static final int CAPTURE_TICK_INTERVAL = 2;

    private int tick = 0;
    private final ActionModeRoom room;
    private final HashMap<Player, Integer> playerLastIn = new HashMap<>();

    public AsyncControlPointCheckTask(ActionModeRoom room) {
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
                GunWar.getInstance().getLogger().error("AsyncControlPointCheckTask Error!", e);
            }

            this.tick++;

            long duration = System.currentTimeMillis() - startTime;
            try {
                Thread.sleep(Math.max(50L - duration, 1));
            } catch (Exception e) {
                GunWar.getInstance().getLogger().error("AsyncControlPointCheckTask Error!", e);
            }
        }
    }

    private void work(int tick) {
        ActionModeRoom.Zone currentZone = this.room.getCurrentZone();
        if (currentZone == null || currentZone.isCaptured()) {
            return;
        }

        // 检查当前区域的每个控制点
        for (ActionModeRoom.ControlPoint controlPoint : currentZone.getControlPoints()) {
            if (controlPoint.isCaptured()) {
                continue;
            }

            // 获取控制点范围内的玩家
            ArrayList<Player> playersInRange = new ArrayList<>();
            for (Player player : this.room.getPlayerDataMap().keySet()) {
                if (player.distance(controlPoint.getPosition()) <= this.room.getControlPointRadius()) {
                    playersInRange.add(player);
                }
            }

            // 检查占领进度
            this.checkControlPoint(playersInRange, controlPoint, tick);
        }

        // 每秒显示一次粒子效果
        if (tick % 20 == 1) {
            this.showParticleEffect();
        }
    }

    /**
     * 检查控制点占领进度
     *
     * @param players 在占领范围内的玩家列表
     * @param controlPoint 控制点
     */
    private void checkControlPoint(ArrayList<Player> players, ActionModeRoom.ControlPoint controlPoint, int tick) {
        if (tick % CAPTURE_TICK_INTERVAL != 0) {
            return;
        }
        ArrayList<Player> attackers = new ArrayList<>();
        ArrayList<Player> defenders = new ArrayList<>();

        for (Player player : players) {
            int last = this.playerLastIn.getOrDefault(player, tick);
            if (tick - last >= 1) {
                Team playerTeam = this.room.getPlayerTeamAccurate(player);
                if (playerTeam == Team.RED) {
                    attackers.add(player);
                } else if (playerTeam == Team.BLUE) {
                    defenders.add(player);
                }
            }
            this.playerLastIn.put(player, tick);
        }

        EntityLongFlag flag = controlPoint.getFlag();
        if (flag == null) {
            return;
        }

        // 进攻方人数多于防守方时增加占领进度
        if (attackers.size() > defenders.size()) {
            flag.addTeamPoints(Team.RED, (attackers.size() - defenders.size()));

            // 检查是否完全占领（team已经在addTeamPoints中自动切换）
            if (flag.getTeam() == Team.RED && flag.getFlagHeight() >= 100) {
                if (!controlPoint.isCaptured()) {
                    controlPoint.setCaptured(true);
                }
                flag.setFlagHeight(100);
            }
        } else if (defenders.size() > attackers.size()) {
            // 防守方人数多时降低占领进度
            flag.addTeamPoints(Team.BLUE, (defenders.size() - attackers.size()));
        }
    }

    /**
     * 显示控制点范围粒子效果
     */
    private void showParticleEffect() {
        ActionModeRoom.Zone currentZone = this.room.getCurrentZone();
        if (currentZone == null || currentZone.isCaptured()) {
            return;
        }

        LinkedList<Vector3> allPoints = new LinkedList<>();

        // 为当前区域的所有未占领控制点显示范围
        for (ActionModeRoom.ControlPoint controlPoint : currentZone.getControlPoints()) {
            if (!controlPoint.isCaptured()) {
                List<Vector3> points = Tools.getRoundEdgePoint(
                        controlPoint.getPosition(),
                        this.room.getControlPointRadius()
                );
                allPoints.addAll(points);
            }
        }

        // 显示粒子效果
        for (Vector3 vector3 : allPoints) {
            vector3.y += 0.1;
            if (this.room.getLevel().getBlock(vector3).getId() == BlockID.AIR) {
                for (int y = vector3.getFloorY(); y > vector3.getFloorY() - 5; y--) {
                    if (this.room.getLevel().getBlock(new Vector3(vector3.x, y, vector3.z)).getId() != BlockID.AIR) {
                        vector3.y = y + 1.1;
                        break;
                    }
                }
            } else {
                for (int y = vector3.getFloorY(); y < vector3.getFloorY() + 5; y++) {
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
