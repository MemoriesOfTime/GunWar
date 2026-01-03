package cn.lanink.gunwar.room.action;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.camera.CameraAnimationTask;
import cn.lanink.gunwar.camera.CameraKeyframe;
import cn.lanink.gunwar.entity.action.EntityGunWarCoverBlue;
import cn.lanink.gunwar.entity.action.EntityGunWarCoverRed;
import cn.lanink.gunwar.entity.flag.EntityLongFlag;
import cn.lanink.gunwar.room.base.BaseRespawnModeRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.tasks.game.action.AsyncControlPointCheckTask;
import cn.lanink.gunwar.tasks.game.action.ControlPointSpawnCheckTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.DummyBossBar;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 行动模式房间
 * 攻防推进型团队玩法
 *
 * @author LT_Name
 */
public class ActionModeRoom extends BaseRespawnModeRoom {

    @Getter
    private int attackerInitialResource;
    @Getter
    private int attackerResourceReward;
    @Getter
    private int attackerCurrentResource;

    @Getter
    private final List<Zone> zones = new ArrayList<>();
    @Getter
    private int currentZoneIndex = 0;

    private final List<Vector3> defenderSpawnPoints = new ArrayList<>();

    private final ConcurrentHashMap<Player, DummyBossBar> bossBarMap = new ConcurrentHashMap<>();
    private int bossBarShowTime = 0;

    @Getter
    private boolean enableCameraAnimation;
    private final Map<Player, CameraAnimationTask> playerCameraAnimations = new ConcurrentHashMap<>();
    private boolean cameraAnimationPlaying = false;
    private boolean pauseGameTime = false;

    @Getter
    private boolean enableOvertime;
    @Getter
    private int overtimeResource;
    @Getter
    private int overtimeTime;
    private boolean isOvertime = false;
    private boolean overtimeTriggered = false;
    private final List<Entity> overtimeCoverEntities = new ArrayList<>();
    private boolean overtimeAnimationPlaying = false;

    /**
     * 区域类
     */
    @Getter
    @EqualsAndHashCode
    public static class Zone {
        private final String name;
        private final List<ControlPoint> controlPoints;
        private boolean captured = false;

        public Zone(String name, List<ControlPoint> controlPoints) {
            this.name = name;
            this.controlPoints = controlPoints;
        }

        public void setCaptured(boolean captured) {
            this.captured = captured;
        }

        public boolean isAllPointsCaptured() {
            return controlPoints.stream().allMatch(ControlPoint::isCaptured);
        }
    }

    /**
     * 控制点类
     */
    @Getter
    public static class ControlPoint {
        private final Vector3 position;
        private EntityLongFlag flag;
        private boolean captured = false;

        public ControlPoint(Vector3 position) {
            this.position = position;
        }

        public void setFlag(EntityLongFlag flag) {
            this.flag = flag;
        }

        public void setCaptured(boolean captured) {
            this.captured = captured;
        }
    }

    /**
     * 初始化
     *
     * @param level  游戏世界
     * @param config 配置文件
     */
    public ActionModeRoom(@NotNull Level level, @NotNull Config config) throws RoomLoadException {
        super(level, config);

        this.attackerInitialResource = config.getInt("attackerInitialResource", 100);
        this.attackerResourceReward = config.getInt("attackerResourceReward", 20);

        this.enableCameraAnimation = config.getBoolean("enableCameraAnimation", true);

        this.enableOvertime = config.getBoolean("enableOvertime", true);
        this.overtimeResource = config.getInt("overtimeResource", 50);
        this.overtimeTime = config.getInt("overtimeTime", 120);

        this.loadZonesFromConfig(config);

        this.loadDefenderSpawnPoints(config);

        if (this.zones.isEmpty()) {
            throw new RoomLoadException("§c房间：" + level.getFolderName() + " 未配置任何区域，加载失败！");
        }
        if (this.defenderSpawnPoints.size() != this.zones.size()) {
            throw new RoomLoadException("§c房间：" + level.getFolderName() + " 防守方重生点数量与区域数量不匹配，加载失败！");
        }
    }

    /**
     * 从配置加载区域
     */
    private void loadZonesFromConfig(Config config) {
        String[] zoneNames = {"A", "B", "C"};
        for (String zoneName : zoneNames) {
            String controlPointsKey = "actionZone" + zoneName + "_controlPoints";
            if (!config.exists(controlPointsKey)) {
                break;
            }

            List<String> controlPointStrings = config.getStringList(controlPointsKey);
            List<ControlPoint> controlPoints = new ArrayList<>();
            for (String posString : controlPointStrings) {
                if (!posString.trim().isEmpty()) {
                    Vector3 position = Tools.stringToVector3(posString);
                    controlPoints.add(new ControlPoint(position));
                }
            }

            if (!controlPoints.isEmpty()) {
                this.zones.add(new Zone("区域" + zoneName, controlPoints));
            }
        }

        for (Zone zone : this.zones) {
            zone.setCaptured(false);
            for (ControlPoint point : zone.getControlPoints()) {
                point.setCaptured(false);
                if (point.getFlag() != null) {
                    point.getFlag().close();
                    point.setFlag(null);
                }
            }
        }
    }

    /**
     * 从配置加载防守方重生点
     */
    private void loadDefenderSpawnPoints(Config config) {
        String[] zoneNames = {"A", "B", "C"};
        for (String zoneName : zoneNames) {
            String spawnKey = "actionZone" + zoneName + "_defenderSpawn";
            if (!config.exists(spawnKey)) {
                break;
            }
            String spawnString = config.getString(spawnKey, "");
            if (!spawnString.trim().isEmpty()) {
                defenderSpawnPoints.add(Tools.stringToVector3(spawnString));
            }
        }
    }

    @Override
    public void saveConfig() {
        super.saveConfig();

        this.config.set("attackerInitialResource", this.attackerInitialResource);
        this.config.set("attackerResourceReward", this.attackerResourceReward);

        this.config.set("enableOvertime", this.enableOvertime);
        this.config.set("overtimeResource", this.overtimeResource);
        this.config.set("overtimeTime", this.overtimeTime);

        this.config.set("enableCameraAnimation", this.enableCameraAnimation);

        String[] zoneNames = {"A", "B", "C"};
        for (int i = 0; i < this.zones.size() && i < zoneNames.length; i++) {
            Zone zone = this.zones.get(i);
            String zoneName = zoneNames[i];

            List<String> controlPointStrings = new ArrayList<>();
            for (ControlPoint point : zone.getControlPoints()) {
                controlPointStrings.add(Tools.vector3ToString(point.getPosition()));
            }
            this.config.set("actionZone" + zoneName + "_controlPoints", controlPointStrings);

            if (i < defenderSpawnPoints.size()) {
                this.config.set("actionZone" + zoneName + "_defenderSpawn",
                    Tools.vector3ToString(defenderSpawnPoints.get(i)));
            }
        }

        this.config.save();
    }

    @Override
    public void timeTask() {
        super.timeTask();

        if (!this.isRoundEnd()) {
            this.updateBossBar();
            this.checkZoneCaptureStatus();
            this.checkVictoryCondition();
        }
    }

    /**
     * 检查游戏时间
     * 重写父类方法，支持暂停游戏时间
     */
    @Override
    protected void checkGameTime() {
        if (this.pauseGameTime) {
            return;
        }
        if (this.gameTime > 0) {
            this.gameTime--;
        }
    }

    /**
     * 更新Boss条显示
     */
    private void updateBossBar() {
        Zone currentZone = getCurrentZone();
        if (currentZone == null) {
            return;
        }

        StringBuilder text = new StringBuilder();

        text.append("§c资源: §f").append(this.attackerCurrentResource).append(" §7|§r ");

        if (this.isOvertime) {
            text.append("§6§l【加时赛】§r §e当前目标: §f").append(currentZone.getName()).append(" §7|§r ");
        } else {
            text.append("§e当前目标: §f").append(currentZone.getName()).append(" §7|§r ");
        }

        for (int i = 0; i < currentZone.getControlPoints().size(); i++) {
            ControlPoint point = currentZone.getControlPoints().get(i);
            if (point.isCaptured()) {
                text.append("§a✓");
            } else {
                text.append("§f").append((char)('A' + i));
            }
            if (i < currentZone.getControlPoints().size() - 1) {
                text.append(" ");
            }
        }

        for (Player player : this.getPlayerDataMap().keySet()) {
            Tools.createBossBar(player, this.bossBarMap);
            DummyBossBar bossBar = this.bossBarMap.get(player);
            bossBar.setText(text.toString());

            Team team = this.getPlayerTeam(player);
            if (team == Team.RED) {
                bossBar.setColor(BossBarColor.RED);
                float maxResource = this.isOvertime ? this.overtimeResource : this.attackerInitialResource;
                bossBar.setLength(Math.max(1, this.attackerCurrentResource * 1.0f / maxResource * 100));
            } else if (team == Team.BLUE) {
                bossBar.setColor(BossBarColor.BLUE);
                float progress = (currentZoneIndex * 1.0f / zones.size()) * 100;
                bossBar.setLength(Math.max(1, progress));
            }
        }
    }

    /**
     * 检查区域占领状态
     */
    private void checkZoneCaptureStatus() {
        Zone currentZone = getCurrentZone();
        if (currentZone == null || currentZone.isCaptured()) {
            return;
        }

        if (currentZone.isAllPointsCaptured()) {
            this.onZoneCaptured(currentZone);
        }
    }

    /**
     * 区域被占领时的处理
     */
    private void onZoneCaptured(Zone zone) {
        zone.setCaptured(true);

        this.attackerCurrentResource += this.attackerResourceReward;

        Tools.sendMessage(this, "§a区域 " + zone.getName() + " 已被进攻方占领！");
        Tools.sendMessage(this, "§e进攻方获得 " + this.attackerResourceReward + " 点资源奖励");

        this.currentZoneIndex++;

        if (this.currentZoneIndex < this.zones.size()) {
            Zone nextZone = this.zones.get(this.currentZoneIndex);
            Tools.sendMessage(this, "§b新目标：占领 " + nextZone.getName());
        }
    }

    /**
     * 检查胜负条件
     */
    private void checkVictoryCondition() {
        if (this.pauseGameTime) {
            return;
        }

        if (this.currentZoneIndex >= this.zones.size()) {
            this.roundEnd(Team.RED);
            return;
        }

        if (this.attackerCurrentResource <= 0) {
            if (this.isOvertime) {
                this.roundEnd(Team.BLUE);
                return;
            }
            if (this.enableOvertime && !this.overtimeTriggered) {
                this.triggerOvertime();
                return;
            }
            this.roundEnd(Team.BLUE);
            return;
        }

        if (this.gameTime <= 0) {
            if (this.isOvertime) {
                this.roundEnd(Team.BLUE);
                return;
            }
            if (this.enableOvertime && !this.overtimeTriggered) {
                this.triggerOvertime();
                return;
            }
            this.roundEnd(Team.BLUE);
        }
    }

    @Override
    public void endGame() {
        this.clearOvertimeCoverEntities();
        super.endGame();
    }

    @Override
    public void endGame(int victory) {
        this.clearOvertimeCoverEntities();
        super.endGame(victory);
    }

    @Override
    public void startGame() {
        this.attackerCurrentResource = this.attackerInitialResource;
        this.currentZoneIndex = 0;

        this.isOvertime = false;
        this.overtimeTriggered = false;

        this.clearOvertimeCoverEntities();

        if (this.bossBarMap != null && !this.bossBarMap.isEmpty()) {
            for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
                entry.getKey().removeBossBar(entry.getValue().getBossBarId());
            }
            this.bossBarMap.clear();
        }

        super.startGame();

        if (this.enableCameraAnimation && !this.zones.isEmpty()) {
            this.playCameraAnimation();
        }

        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar,
                new ControlPointSpawnCheckTask(GunWar.getInstance(), this),
                20
        );

        Server.getInstance().getScheduler().scheduleAsyncTask(
                this.gunWar,
                new AsyncControlPointCheckTask(this)
        );
    }

    @Override
    public void roundEnd(Team victory) {
        if (victory == Team.RED) {
            if (this.isOvertime) {
                Tools.sendMessage(this, "§c§l进攻方胜利！在最后攻势中突破了防线！");
            } else {
                Tools.sendMessage(this, "§c进攻方胜利！成功占领所有区域！");
            }
            this.endGame(1);
        } else if (victory == Team.BLUE) {
            if (this.isOvertime) {
                Tools.sendMessage(this, "§9§l防守方胜利！最后防线守住了！");
            } else {
                Tools.sendMessage(this, "§9防守方胜利！成功阻止了进攻方的推进！");
            }
            this.endGame(2);
        } else {
            this.endGame(0);
        }
    }

    @Override
    public void playerDeath(Player player, Entity damager, String killMessage) {
        super.playerDeath(player, damager, killMessage);

        if (damager instanceof Player killer) {
            if (!killer.equals(player)) {
                Tools.playSound(killer, "gunwar.kill");
            }
        }

        // 进攻方玩家死亡消耗资源
        if (this.getPlayerTeam(player) == Team.RED) {
            this.attackerCurrentResource--;
            if (this.attackerCurrentResource < 0) {
                this.attackerCurrentResource = 0;
            }
        }
    }

    @Override
    public void playerRespawn(Player player) {
        super.playerRespawn(player);

        Team team = this.getPlayerTeamAccurate(player);
        if (team == Team.RED || team == Team.BLUE) {
            Vector3 frontlinePoint = this.getFrontlineControlPoint(team);
            if (frontlinePoint != null) {
                player.teleport(frontlinePoint);
            }
        }
    }

    /**
     * 获取当前激活的区域
     */
    public Zone getCurrentZone() {
        if (this.currentZoneIndex >= 0 && this.currentZoneIndex < this.zones.size()) {
            return this.zones.get(this.currentZoneIndex);
        }
        return null;
    }

    /**
     * 获取控制点半径
     */
    public int getControlPointRadius() {
        return 10;
    }

    /**
     * 播放摄像机动画
     */
    private void playCameraAnimation() {
        this.cameraAnimationPlaying = true;
        this.pauseGameTime = true;

        for (Player player : this.getPlayerDataMap().keySet()) {
            List<CameraKeyframe> keyframes = this.generateCameraPath(player);

            CameraAnimationTask.Builder builder = new CameraAnimationTask.Builder(this.gunWar, player)
                    .onComplete(this::onCameraAnimationComplete);

            for (CameraKeyframe keyframe : keyframes) {
                builder.addKeyframe(keyframe);
            }

            CameraAnimationTask task = builder.buildAndStart();
            this.playerCameraAnimations.put(player, task);
        }
    }

    /**
     * 生成摄像机路径
     *
     * @param player 玩家
     * @return 摄像机关键帧列表
     */
    private List<CameraKeyframe> generateCameraPath(Player player) {
        List<CameraKeyframe> keyframes = new ArrayList<>();

        Vector3 attackerSpawn = this.getRedSpawn();
        Vector3 defenderSpawn = this.getBlueSpawn();

        Vector3 battlefieldCenter = calculateBattlefieldCenter();

        double vx = defenderSpawn.x - attackerSpawn.x;
        double vz = defenderSpawn.z - attackerSpawn.z;
        float overviewYaw = (float) Math.toDegrees(Math.atan2(-vz, -vx));

        double overviewHeight = 50;
        keyframes.add(new CameraKeyframe(
                battlefieldCenter.x,
                battlefieldCenter.y + overviewHeight,
                battlefieldCenter.z,
                overviewYaw,
                70,
                100,
                "§e战场概览",
                "§7准备进入战斗"
        ));

        keyframes.add(new CameraKeyframe(
                attackerSpawn.x,
                attackerSpawn.y + 15,
                attackerSpawn.z,
                this.zones.isEmpty() ? 0 : calculateYawBetween(attackerSpawn, calculateZoneCenter(this.zones.get(0))),
                45,
                60,
                "§c进攻方起点",
                "§7开始推进"
        ));

        for (int i = 0; i < this.zones.size(); i++) {
            Zone zone = this.zones.get(i);
            Vector3 zoneCenter = calculateZoneCenter(zone);

            double height = Math.max(25 - (i * 3), 10);

            String zoneTitle = "§6" + zone.getName();
            String zoneSubtitle = "§7控制点: §b" + zone.getControlPoints().size() + " §7个";

            float yaw;
            if (i >= this.zones.size() - 1) {
                yaw = calculateYawBetween(zoneCenter, defenderSpawn);
            } else {
                Vector3 nextZoneCenter = calculateZoneCenter(this.zones.get(i + 1));
                yaw = calculateYawBetween(zoneCenter, nextZoneCenter);
            }

            keyframes.add(new CameraKeyframe(
                    zoneCenter.x,
                    zoneCenter.y + height,
                    zoneCenter.z,
                    yaw,
                    Math.max(50 - (i * 8), 30),
                    80,
                    zoneTitle,
                    zoneSubtitle
            ));
        }

        Team team = this.getPlayerTeam(player);
        Vector3 spawnPoint = this.getFrontlineControlPoint(team);
        String teamName;
        if (team == Team.RED) {
            teamName = "§c进攻方";
        } else {
            teamName = "§9防守方";
        }

        keyframes.add(new CameraKeyframe(
                spawnPoint.x,
                spawnPoint.y + 2,
                spawnPoint.z,
                0,
                20,
                30,
                teamName,
                "§7战斗开始"
        ));

        return keyframes;
    }

    /**
     * 获取最靠近前线的占领点位置
     */
    private Vector3 getFrontlineControlPoint(Team team) {
        Vector3 fallback = team == Team.RED ? this.getRedSpawn() : this.getBlueSpawn();
        if (this.zones.isEmpty()) {
            return fallback;
        }

        Zone currentZone = this.getCurrentZone();
        if (currentZone == null) {
            return fallback;
        }

        Vector3 targetPos = calculateZoneCenter(currentZone);
        Vector3 bestPoint = null;
        double bestDist = Double.MAX_VALUE;

        if (team == Team.BLUE) {
            for (int i = this.currentZoneIndex; i < this.zones.size(); i++) {
                Zone zone = this.zones.get(i);
                for (ControlPoint point : zone.getControlPoints()) {
                    if (!point.isCaptured()) {
                        Vector3 pos = point.getPosition();
                        double dx = pos.x - targetPos.x;
                        double dy = pos.y - targetPos.y;
                        double dz = pos.z - targetPos.z;
                        double dist = dx * dx + dy * dy + dz * dz;
                        if (dist < bestDist) {
                            bestDist = dist;
                            bestPoint = pos;
                        }
                    }
                }
            }
            return bestPoint != null ? bestPoint : fallback;
        }

        for (Zone zone : this.zones) {
            if (zone.equals(currentZone)) {
                continue;
            }
            for (ControlPoint point : zone.getControlPoints()) {
                if (!point.isCaptured()) {
                    continue;
                }
                Vector3 pos = point.getPosition();
                double dx = pos.x - targetPos.x;
                double dy = pos.y - targetPos.y;
                double dz = pos.z - targetPos.z;
                double dist = dx * dx + dy * dy + dz * dz;
                if (dist < bestDist) {
                    bestDist = dist;
                    bestPoint = pos;
                }
            }
        }

        return bestPoint != null ? bestPoint : fallback;
    }

    /**
     * 计算战场中心点
     */
    private Vector3 calculateBattlefieldCenter() {
        double x = 0, y = 0, z = 0;
        int count = 0;

        for (Zone zone : this.zones) {
            for (ControlPoint point : zone.getControlPoints()) {
                Vector3 pos = point.getPosition();
                x += pos.x;
                y += pos.y;
                z += pos.z;
                count++;
            }
        }

        if (count > 0) {
            return new Vector3(x / count, y / count, z / count);
        }

        return this.getWaitSpawn();
    }

    /**
     * 计算加时赛观察高度
     * 动态计算合适的高度，确保所有占领点都能被看到
     *
     * @return 观察高度（相对于战场中心的Y轴偏移）
     */
    private double calculateOvertimeObservationHeight() {
        if (this.zones.isEmpty()) {
            return 60.0;
        }

        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (Zone zone : this.zones) {
            for (ControlPoint point : zone.getControlPoints()) {
                Vector3 pos = point.getPosition();
                minX = Math.min(minX, pos.x);
                maxX = Math.max(maxX, pos.x);
                minZ = Math.min(minZ, pos.z);
                maxZ = Math.max(maxZ, pos.z);
            }
        }

        double rangeX = maxX - minX;
        double rangeZ = maxZ - minZ;

        double diagonal = Math.sqrt(rangeX * rangeX + rangeZ * rangeZ);

        double height = Math.max(40.0, Math.min(180.0, diagonal * 0.8));

        return height;
    }

    /**
     * 计算区域中心点
     */
    private Vector3 calculateZoneCenter(Zone zone) {
        double x = 0, y = 0, z = 0;
        int count = zone.getControlPoints().size();

        for (ControlPoint point : zone.getControlPoints()) {
            Vector3 pos = point.getPosition();
            x += pos.x;
            y += pos.y;
            z += pos.z;
        }

        if (count > 0) {
            return new Vector3(x / count, y / count, z / count);
        }

        return this.getWaitSpawn();
    }

    /**
     * 计算从起点朝向终点的偏航角
     *
     * @param from 起点位置
     * @param to 终点位置
     * @return 偏航角（度）
     */
    private float calculateYawBetween(Vector3 from, Vector3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;

        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        return (float) yaw;
    }

    /**
     * 计算朝向下一个区域的偏航角
     * @deprecated 使用 {@link #calculateYawBetween(Vector3, Vector3)} 替代
     */
    @Deprecated
    private float calculateYawToNextZone(int currentZoneIndex) {
        if (currentZoneIndex >= this.zones.size() - 1) {
            return 0;
        }

        Vector3 currentCenter = calculateZoneCenter(this.zones.get(currentZoneIndex));
        Vector3 nextCenter = calculateZoneCenter(this.zones.get(currentZoneIndex + 1));

        return calculateYawBetween(currentCenter, nextCenter);
    }

    /**
     * 触发加时赛
     */
    private void triggerOvertime() {
        this.overtimeTriggered = true;

        this.playOvertimeAnimation();
    }

    /**
     * 播放加时赛动画
     * 优化版：按照进攻顺序逐个查看控制点，避免大地图渲染距离问题
     */
    private void playOvertimeAnimation() {
        this.pauseGameTime = true;
        this.overtimeAnimationPlaying = true;

        Vector3 battlefieldCenter = calculateBattlefieldCenter();

        double observationHeight = calculateOvertimeObservationHeight();

        Vector3 attackerSpawn = this.getRedSpawn();
        Vector3 defenderSpawn = this.getBlueSpawn();

        double vx = defenderSpawn.x - attackerSpawn.x;
        double vz = defenderSpawn.z - attackerSpawn.z;
        float overviewYaw = (float) Math.toDegrees(Math.atan2(-vz, -vx));

        for (Player player : this.getPlayerDataMap().keySet()) {
            Team team = this.getPlayerTeam(player);

            String defeatTitle, defeatSubtitle;
            String overtimeTitle, overtimeSubtitle;

            if (team == Team.RED) {
                defeatTitle = "§c§l我们输了";
                defeatSubtitle = "§7无法拿下本区域";
                overtimeTitle = "§c§l最后攻势";
                overtimeSubtitle = "";
            } else {
                defeatTitle = "§9§l我们赢了";
                defeatSubtitle = "§7成功守住本区域";
                overtimeTitle = "§9§l最后防线";
                overtimeSubtitle = "";
            }

            Vector3 playerPos = player.getPosition();

            Vector3 behindPlayer = calculateBehindPlayerPosition(player, 5);
            float yawToPlayer = calculateYawBetween(behindPlayer, playerPos);

            CameraAnimationTask.Builder builder = new CameraAnimationTask.Builder(this.gunWar, player)
                    .onComplete(this::onOvertimeAnimationComplete);

            builder.addKeyframe(
                    behindPlayer.x,
                    behindPlayer.y + 2,
                    behindPlayer.z,
                    yawToPlayer,
                    10,
                    40,
                    "",
                    ""
            );

            builder.addKeyframe(
                    behindPlayer.x,
                    behindPlayer.y + 2,
                    behindPlayer.z,
                    yawToPlayer,
                    10,
                    60,
                    defeatTitle,
                    defeatSubtitle
            );

            int currentTicksAccumulated = 100;
            int controlPointIndex = 0;
            Vector3 previousPointPos = attackerSpawn;

            for (Zone zone : this.zones) {
                for (ControlPoint point : zone.getControlPoints()) {
                    Vector3 pointPos = point.getPosition();

                    double dx = pointPos.x - previousPointPos.x;
                    double dz = pointPos.z - previousPointPos.z;
                    float attackYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

                    double backwardDistance = 5.0;
                    double cameraHeight = 20.0;

                    double offsetX = previousPointPos.x - pointPos.x;
                    double offsetZ = previousPointPos.z - pointPos.z;
                    double offsetLen = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
                    double dirX;
                    double dirZ;
                    if (offsetLen > 0.0001) {
                        dirX = offsetX / offsetLen;
                        dirZ = offsetZ / offsetLen;
                    } else {
                        double yawRadians = Math.toRadians(attackYaw + 180);
                        dirX = Math.sin(yawRadians);
                        dirZ = -Math.cos(yawRadians);
                    }

                    double cameraX = pointPos.x + dirX * backwardDistance;
                    double cameraZ = pointPos.z + dirZ * backwardDistance;
                    double cameraY = pointPos.y + cameraHeight;

                    Vector3 coverEntityPos = new Vector3(pointPos.x, pointPos.y + 10, pointPos.z);
                    float cameraYaw = calculateYawBetween(new Vector3(cameraX, cameraY, cameraZ), coverEntityPos);

                    String pointTitle = (point.isCaptured() ? "§a已占领" : "§c未占领")
                            + " §7- §f" + zone.getName();
                    String pointSubtitle = "§7控制点 " + ((char)('A' + (zone.getControlPoints().indexOf(point))));

                    builder.addKeyframe(
                            cameraX,
                            cameraY,
                            cameraZ,
                            cameraYaw,
                            45,
                            30,
                            "",
                            ""
                    );

                    builder.addKeyframe(
                            cameraX,
                            cameraY,
                            cameraZ,
                            cameraYaw,
                            45,
                            70,
                            pointTitle,
                            pointSubtitle
                    );

                    final int finalControlPointIndex = controlPointIndex;
                    final Vector3 finalPointPos = pointPos;
                    final boolean isCaptured = point.isCaptured();

                    this.gunWar.getServer().getScheduler().scheduleDelayedTask(
                            this.gunWar,
                            () -> this.spawnSingleOvertimeCoverEntity(finalControlPointIndex, finalPointPos, false),
                            currentTicksAccumulated + 30
                    );

                    if (isCaptured) {
                        this.gunWar.getServer().getScheduler().scheduleDelayedTask(
                                this.gunWar,
                                () -> this.updateCoverEntityColor(finalControlPointIndex, finalPointPos, true),
                                currentTicksAccumulated + 30 + 40
                        );
                    }

                    currentTicksAccumulated += 100;
                    controlPointIndex++;
                    previousPointPos = pointPos;
                }
            }

            builder.addKeyframe(
                    battlefieldCenter.x,
                    battlefieldCenter.y + observationHeight,
                    battlefieldCenter.z,
                    overviewYaw,
                    75,
                    40,
                    "",
                    ""
            );

            builder.addKeyframe(
                    battlefieldCenter.x,
                    battlefieldCenter.y + observationHeight,
                    battlefieldCenter.z,
                    overviewYaw,
                    75,
                    60,
                    "",
                    ""
            );

            builder.addKeyframe(
                    battlefieldCenter.x,
                    battlefieldCenter.y + observationHeight,
                    battlefieldCenter.z,
                    overviewYaw,
                    75,
                    80,
                    overtimeTitle,
                    overtimeSubtitle
            );

            CameraAnimationTask task = builder.buildAndStart();
            this.playerCameraAnimations.put(player, task);
        }
    }

    /**
     * 计算朝向下一个控制点的偏航角
     */
    private float calculateNextPointYaw(Zone currentZone, ControlPoint currentPoint, Vector3 defenderSpawn) {
        // 找到当前控制点在所有控制点中的位置
        boolean foundCurrent = false;

        for (Zone zone : this.zones) {
            for (ControlPoint point : zone.getControlPoints()) {
                if (foundCurrent) {
                    // 找到了下一个控制点，朝向它
                    return calculateYawBetween(currentPoint.getPosition(), point.getPosition());
                }
                if (point == currentPoint) {
                    foundCurrent = true;
                }
            }
        }

        // 如果是最后一个控制点，朝向防守方出生点
        return calculateYawBetween(currentPoint.getPosition(), defenderSpawn);
    }

    /**
     * 生成单个控制点的遮盖实体
     */
    private void spawnSingleOvertimeCoverEntity(int index, Vector3 pointPos, boolean isCaptured) {
        if (!this.overtimeAnimationPlaying) {
            return;
        }
        double coverY = pointPos.y + 10;
        Vector3 position = new Vector3(pointPos.x, coverY, pointPos.z);

        CompoundTag nbt = Entity.getDefaultNBT(position);

        Entity coverEntity;
        if (isCaptured) {
            coverEntity = new EntityGunWarCoverRed(this.level.getChunk((int) position.x >> 4, (int) position.z >> 4), nbt);
        } else {
            coverEntity = new EntityGunWarCoverBlue(this.level.getChunk((int) position.x >> 4, (int) position.z >> 4), nbt);
        }

        coverEntity.spawnToAll();

        while (this.overtimeCoverEntities.size() <= index) {
            this.overtimeCoverEntities.add(null);
        }
        this.overtimeCoverEntities.set(index, coverEntity);
    }

    /**
     * 更新遮盖实体颜色
     */
    private void updateCoverEntityColor(int index, Vector3 pointPos, boolean toRed) {
        if (!this.overtimeAnimationPlaying) {
            return;
        }
        if (index < 0) {
            GunWar.getInstance().getLogger().warning("更新遮盖实体颜色失败：索引 " + index + " 小于0");
            return;
        }

        while (this.overtimeCoverEntities.size() <= index) {
            this.overtimeCoverEntities.add(null);
        }

        Entity oldEntity = this.overtimeCoverEntities.get(index);
        if (oldEntity != null && !oldEntity.isClosed()) {
            oldEntity.close();
        }

        double coverY = pointPos.y + 10;
        Vector3 position = new Vector3(pointPos.x, coverY, pointPos.z);

        CompoundTag nbt = Entity.getDefaultNBT(position);

        Entity newEntity;
        if (toRed) {
            newEntity = new EntityGunWarCoverRed(this.level.getChunk((int) position.x >> 4, (int) position.z >> 4), nbt);
        } else {
            newEntity = new EntityGunWarCoverBlue(this.level.getChunk((int) position.x >> 4, (int) position.z >> 4), nbt);
        }

        newEntity.spawnToAll();
        this.overtimeCoverEntities.set(index, newEntity);
    }

    /**
     * 计算玩家身后的位置
     *
     * @param player 玩家
     * @param distance 距离（格）
     * @return 身后的位置
     */
    private Vector3 calculateBehindPlayerPosition(Player player, double distance) {
        double yaw = player.getYaw();
        double yawRadians = Math.toRadians(yaw);
        double x = player.getX() + distance * Math.sin(yawRadians);
        double z = player.getZ() - distance * Math.cos(yawRadians);
        return new Vector3(x, player.getY(), z);
    }

    /**
     * 加时赛动画完成回调
     */
    private void onOvertimeAnimationComplete(Player player) {
        this.playerCameraAnimations.remove(player);

        this.playerRespawn(player);

        if (this.playerCameraAnimations.isEmpty()) {
            this.clearOvertimeCoverEntities();
            this.overtimeAnimationPlaying = false;

            this.isOvertime = true;
            this.attackerCurrentResource = this.overtimeResource;
            this.gameTime = this.overtimeTime;
            this.pauseGameTime = false;

            Tools.sendMessage(this, "§6§l========================================");
            Tools.sendMessage(this, "§c§l加时赛开始！");
            Tools.sendMessage(this, "§e进攻方获得 §6" + this.overtimeResource + " §e点资源");
            Tools.sendMessage(this, "§e剩余时间：§6" + this.overtimeTime + " §e秒");
            Tools.sendMessage(this, "§6§l========================================");
        }
    }

    /**
     * 清除加时赛遮盖实体
     */
    private void clearOvertimeCoverEntities() {
        this.overtimeAnimationPlaying = false;
        for (Entity entity : this.overtimeCoverEntities) {
            if (entity != null && !entity.isClosed()) {
                entity.close();
            }
        }
        this.overtimeCoverEntities.clear();
    }


    /**
     * 摄像机动画完成回调
     */
    private void onCameraAnimationComplete(Player player) {
        this.playerCameraAnimations.remove(player);

        this.playerRespawn(player);

        if (this.playerCameraAnimations.isEmpty()) {
            this.cameraAnimationPlaying = false;
            this.pauseGameTime = false;
        }
    }

    /**
     * 测试摄像机动画（用于调试）
     * 为单个玩家播放开场动画，不影响游戏状态
     *
     * @param player 要播放动画的玩家
     */
    public void testCameraAnimation(Player player) {
        List<CameraKeyframe> keyframes = this.generateCameraPath(player);

        CameraAnimationTask.Builder builder = new CameraAnimationTask.Builder(this.gunWar, player)
                .onComplete(p -> p.sendMessage("§a开场动画测试完成！"));

        for (CameraKeyframe keyframe : keyframes) {
            builder.addKeyframe(keyframe);
        }

        builder.buildAndStart();
    }

    /**
     * 测试加时赛动画（用于调试）
     * 为单个玩家播放加时赛动画，不影响游戏状态
     * 会随机设置控制点的占领状态（按顺序）
     *
     * @param player 要播放动画的玩家
     */
    public void testOvertimeAnimation(Player player) {
        this.overtimeAnimationPlaying = true;
        boolean continueCapture = true;
        for (Zone zone : this.zones) {
            for (ControlPoint point : zone.getControlPoints()) {
                if (continueCapture) {
                    boolean captured = Math.random() < 0.5;
                    point.setCaptured(captured);
                    if (!captured) {
                        continueCapture = false;
                    }
                } else {
                    point.setCaptured(false);
                }
            }
        }

        Vector3 battlefieldCenter = calculateBattlefieldCenter();

        double observationHeight = calculateOvertimeObservationHeight();

        Vector3 attackerSpawn = this.getRedSpawn();
        Vector3 defenderSpawn = this.getBlueSpawn();

        double vx = defenderSpawn.x - attackerSpawn.x;
        double vz = defenderSpawn.z - attackerSpawn.z;
        float overviewYaw = (float) Math.toDegrees(Math.atan2(-vz, -vx));

        Team team = this.getPlayerTeam(player);
        team = Team.RED;

        String defeatTitle, defeatSubtitle;
        String overtimeTitle, overtimeSubtitle;

        if (team == Team.RED) {
            defeatTitle = "§c§l我们输了";
            defeatSubtitle = "§7无法拿下本区域";
            overtimeTitle = "§c§l最后攻势";
            overtimeSubtitle = "";
        } else {
            defeatTitle = "§9§l我们赢了";
            defeatSubtitle = "§7成功守住本区域";
            overtimeTitle = "§9§l最后防线";
            overtimeSubtitle = "";
        }

        Vector3 playerPos = player.getPosition();

        Vector3 behindPlayer = calculateBehindPlayerPosition(player, 5);
        float yawToPlayer = calculateYawBetween(behindPlayer, playerPos);

        CameraAnimationTask.Builder builder = new CameraAnimationTask.Builder(this.gunWar, player)
                .onComplete(p -> {
                    this.clearOvertimeCoverEntities();
                    p.sendMessage("§a加时赛动画测试完成！");
                });

        builder.addKeyframe(
                behindPlayer.x,
                behindPlayer.y + 2,
                behindPlayer.z,
                yawToPlayer,
                10,
                40,
                "",
                ""
        );

        builder.addKeyframe(
                behindPlayer.x,
                behindPlayer.y + 2,
                behindPlayer.z,
                yawToPlayer,
                10,
                60,
                defeatTitle,
                defeatSubtitle
        );

        int currentTicksAccumulated = 100;
        int controlPointIndex = 0;
        Vector3 previousPointPos = attackerSpawn;

        for (Zone zone : this.zones) {
            for (ControlPoint point : zone.getControlPoints()) {
                Vector3 pointPos = point.getPosition();

                double dx = pointPos.x - previousPointPos.x;
                double dz = pointPos.z - previousPointPos.z;
                float attackYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

                double backwardDistance = 5.0;
                double cameraHeight = 20.0;

                double offsetX = previousPointPos.x - pointPos.x;
                double offsetZ = previousPointPos.z - pointPos.z;
                double offsetLen = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
                double dirX;
                double dirZ;
                if (offsetLen > 0.0001) {
                    dirX = offsetX / offsetLen;
                    dirZ = offsetZ / offsetLen;
                } else {
                    double yawRadians = Math.toRadians(attackYaw + 180);
                    dirX = Math.sin(yawRadians);
                    dirZ = -Math.cos(yawRadians);
                }

                double cameraX = pointPos.x + dirX * backwardDistance;
                double cameraZ = pointPos.z + dirZ * backwardDistance;
                double cameraY = pointPos.y + cameraHeight;

                Vector3 coverEntityPos = new Vector3(pointPos.x, pointPos.y + 10, pointPos.z);
                float cameraYaw = calculateYawBetween(new Vector3(cameraX, cameraY, cameraZ), coverEntityPos);

                String pointTitle = (point.isCaptured() ? "§a已占领" : "§c未占领")
                        + " §7- §f" + zone.getName();
                String pointSubtitle = "§7控制点 " + ((char)('A' + (zone.getControlPoints().indexOf(point))));

                builder.addKeyframe(
                        cameraX,
                        cameraY,
                        cameraZ,
                        cameraYaw,
                        45,
                        30,
                        "",
                        ""
                );

                builder.addKeyframe(
                        cameraX,
                        cameraY,
                        cameraZ,
                        cameraYaw,
                        45,
                        70,
                        pointTitle,
                        pointSubtitle
                );

                final int finalControlPointIndex = controlPointIndex;
                final Vector3 finalPointPos = pointPos;
                final boolean isCaptured = point.isCaptured();

                this.gunWar.getServer().getScheduler().scheduleDelayedTask(
                        this.gunWar,
                        () -> this.spawnSingleOvertimeCoverEntity(finalControlPointIndex, finalPointPos, false),
                        currentTicksAccumulated + 30
                );

                if (isCaptured) {
                    this.gunWar.getServer().getScheduler().scheduleDelayedTask(
                            this.gunWar,
                            () -> this.updateCoverEntityColor(finalControlPointIndex, finalPointPos, true),
                            currentTicksAccumulated + 30 + 40
                    );
                }

                currentTicksAccumulated += 100;
                controlPointIndex++;
                previousPointPos = pointPos;
            }
        }

        builder.addKeyframe(
                battlefieldCenter.x,
                battlefieldCenter.y + observationHeight,
                battlefieldCenter.z,
                overviewYaw,
                75,
                40,
                "",
                ""
        );

        builder.addKeyframe(
                battlefieldCenter.x,
                battlefieldCenter.y + observationHeight,
                battlefieldCenter.z,
                overviewYaw,
                75,
                60,
                "",
                ""
        );

        builder.addKeyframe(
                battlefieldCenter.x,
                battlefieldCenter.y + observationHeight,
                battlefieldCenter.z,
                overviewYaw,
                75,
                80,
                overtimeTitle,
                overtimeSubtitle
        );

        builder.buildAndStart();
    }

}

