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

    // 进攻方资源配置
    @Getter
    private int attackerInitialResource;  // 进攻方初始资源
    @Getter
    private int attackerResourceReward;   // 占领区域资源奖励
    @Getter
    private int attackerCurrentResource;  // 当前资源数量

    // 区域配置
    @Getter
    private final List<Zone> zones = new ArrayList<>();
    @Getter
    private int currentZoneIndex = 0;  // 当前激活的区域索引

    // 防守方重生点配置（每个区域一个）
    private final List<Vector3> defenderSpawnPoints = new ArrayList<>();

    // Boss条显示
    private final ConcurrentHashMap<Player, DummyBossBar> bossBarMap = new ConcurrentHashMap<>();
    private int bossBarShowTime = 0;

    // 摄像机动画配置
    @Getter
    private boolean enableCameraAnimation;  // 是否启用摄像机动画
    private final Map<Player, CameraAnimationTask> playerCameraAnimations = new ConcurrentHashMap<>();
    private boolean cameraAnimationPlaying = false;  // 是否正在播放摄像机动画
    private boolean pauseGameTime = false;  // 是否暂停游戏时间（动画播放时）

    // 加时赛配置
    @Getter
    private boolean enableOvertime;  // 是否启用加时赛
    @Getter
    private int overtimeResource;  // 加时赛进攻方资源
    @Getter
    private int overtimeTime;  // 加时赛时间（秒）
    private boolean isOvertime = false;  // 是否处于加时赛状态
    private boolean overtimeTriggered = false;  // 加时赛是否已触发（防止重复触发）
    private final List<Entity> overtimeCoverEntities = new ArrayList<>();  // 加时赛动画遮盖实体列表

    /**
     * 区域类
     */
    @Getter
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

        // 加载进攻方资源配置
        this.attackerInitialResource = config.getInt("attackerInitialResource", 100);
        this.attackerResourceReward = config.getInt("attackerResourceReward", 20);

        // 加载摄像机动画配置
        this.enableCameraAnimation = config.getBoolean("enableCameraAnimation", true);

        // 加载加时赛配置
        this.enableOvertime = config.getBoolean("enableOvertime", true);
        this.overtimeResource = config.getInt("overtimeResource", 50);
        this.overtimeTime = config.getInt("overtimeTime", 120);

        // 加载区域配置
        this.loadZonesFromConfig(config);

        // 加载防守方重生点
        this.loadDefenderSpawnPoints(config);

        // 验证配置完整性
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
                break; // 如果区域不存在则停止
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

        // 重置所有区域和控制点
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
                break; // 如果区域不存在则停止
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

        // 保存进攻方资源配置
        this.config.set("attackerInitialResource", this.attackerInitialResource);
        this.config.set("attackerResourceReward", this.attackerResourceReward);

        // 保存加时赛配置
        this.config.set("enableOvertime", this.enableOvertime);
        this.config.set("overtimeResource", this.overtimeResource);
        this.config.set("overtimeTime", this.overtimeTime);

        // 保存摄像机动画配置
        this.config.set("enableCameraAnimation", this.enableCameraAnimation);

        // 保存区域配置（新格式）
        String[] zoneNames = {"A", "B", "C"};
        for (int i = 0; i < this.zones.size() && i < zoneNames.length; i++) {
            Zone zone = this.zones.get(i);
            String zoneName = zoneNames[i];

            // 保存控制点列表
            List<String> controlPointStrings = new ArrayList<>();
            for (ControlPoint point : zone.getControlPoints()) {
                controlPointStrings.add(Tools.vector3ToString(point.getPosition()));
            }
            this.config.set("actionZone" + zoneName + "_controlPoints", controlPointStrings);

            // 保存防守方重生点
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
            // 更新Boss条显示
            this.updateBossBar();

            // 检查区域占领状态
            this.checkZoneCaptureStatus();

            // 检查胜负条件
            this.checkVictoryCondition();
        }
    }

    /**
     * 检查游戏时间
     * 重写父类方法，支持暂停游戏时间
     */
    @Override
    protected void checkGameTime() {
        // 如果正在播放动画，暂停游戏时间
        if (this.pauseGameTime) {
            return;
        }
        // 否则正常递减时间
        super.checkGameTime();
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

        // 加时赛时显示特殊标题
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
                // 在加时赛中使用加时赛初始资源作为基准
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

        // 检查当前区域是否所有控制点都被占领
        if (currentZone.isAllPointsCaptured()) {
            this.onZoneCaptured(currentZone);
        }
    }

    /**
     * 区域被占领时的处理
     */
    private void onZoneCaptured(Zone zone) {
        zone.setCaptured(true);

        // 进攻方获得资源奖励
        this.attackerCurrentResource += this.attackerResourceReward;

        // 广播消息
        Tools.sendMessage(this, "§a区域 " + zone.getName() + " 已被进攻方占领！");
        Tools.sendMessage(this, "§e进攻方获得 " + this.attackerResourceReward + " 点资源奖励");

        // 推进到下一个区域
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
        // 进攻方胜利：占领所有区域
        if (this.currentZoneIndex >= this.zones.size()) {
            this.roundEnd(Team.RED);
            return;
        }

        // 防守方胜利条件：进攻方资源耗尽
        if (this.attackerCurrentResource <= 0) {
            // 如果启用加时赛且尚未触发
            if (this.enableOvertime && !this.overtimeTriggered) {
                this.triggerOvertime();
                return;
            }
            // 否则防守方直接胜利
            this.roundEnd(Team.BLUE);
            return;
        }

        // 防守方胜利：时间耗尽
        if (this.gameTime <= 0) {
            // 如果已经在加时赛中，直接结束
            if (this.isOvertime) {
                this.roundEnd(Team.BLUE);
                return;
            }
            // 否则检查是否触发加时赛
            if (this.enableOvertime && !this.overtimeTriggered) {
                this.triggerOvertime();
                return;
            }
            this.roundEnd(Team.BLUE);
        }
    }

    @Override
    public void startGame() {
        // 重置进攻方资源
        this.attackerCurrentResource = this.attackerInitialResource;
        this.currentZoneIndex = 0;

        // 重置加时赛状态
        this.isOvertime = false;
        this.overtimeTriggered = false;

        // 清除加时赛遮盖实体
        this.clearOvertimeCoverEntities();

        // 清除Boss条
        if (this.bossBarMap != null && !this.bossBarMap.isEmpty()) {
            for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
                entry.getKey().removeBossBar(entry.getValue().getBossBarId());
            }
            this.bossBarMap.clear();
        }

        super.startGame();

        // 如果启用摄像机动画，先播放开场动画
        if (this.enableCameraAnimation && !this.zones.isEmpty()) {
            this.playCameraAnimation();
        }

        // 启动控制点生成检查任务
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.gunWar,
                new ControlPointSpawnCheckTask(GunWar.getInstance(), this),
                20
        );

        // 启动异步控制点检查任务
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
        } else if (victory == Team.BLUE) {
            if (this.isOvertime) {
                Tools.sendMessage(this, "§9§l防守方胜利！最后防线守住了！");
            } else {
                Tools.sendMessage(this, "§9防守方胜利！成功阻止了进攻方的推进！");
            }
        }
        super.roundEnd(victory);
    }

    @Override
    public void playerDeath(Player player, Entity damager, String killMessage) {
        super.playerDeath(player, damager, killMessage);

        if (damager instanceof Player) {
            Player killer = (Player) damager;
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
        // 调用父类的playerRespawn方法
        super.playerRespawn(player);

        // 对于防守方，重新传送到当前区域的防守方重生点
        Team team = this.getPlayerTeamAccurate(player);
        if (team == Team.BLUE) {
            int spawnIndex = Math.min(this.currentZoneIndex, this.defenderSpawnPoints.size() - 1);
            if (spawnIndex >= 0 && spawnIndex < this.defenderSpawnPoints.size()) {
                Vector3 defenderSpawn = this.defenderSpawnPoints.get(spawnIndex);
                player.teleport(defenderSpawn);
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
        return 5;
    }

    /**
     * 播放摄像机动画
     */
    private void playCameraAnimation() {
        this.cameraAnimationPlaying = true;
        this.pauseGameTime = true;  // 暂停游戏时间

        for (Player player : this.getPlayerDataMap().keySet()) {
            // 为每个玩家生成并播放摄像机动画
            List<CameraKeyframe> keyframes = this.generateCameraPath(player);

            // 创建并启动摄像机动画任务
            CameraAnimationTask.Builder builder = new CameraAnimationTask.Builder(this.gunWar, player)
                    .onComplete(this::onCameraAnimationComplete);

            // 添加所有关键帧
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

        // 获取进攻方和防守方的复活点
        Vector3 attackerSpawn = this.getRedSpawn();   // 红队=进攻方
        Vector3 defenderSpawn = this.getBlueSpawn();  // 蓝队=防守方

        // 计算战场中心点（所有区域的平均位置）
        Vector3 battlefieldCenter = calculateBattlefieldCenter();

        // 计算概览视角的朝向（使进攻方在左侧，防守方在右侧）
        double vx = defenderSpawn.x - attackerSpawn.x;
        double vz = defenderSpawn.z - attackerSpawn.z;
        float overviewYaw = (float) Math.toDegrees(Math.atan2(-vz, -vx));

        // 第一帧：高空俯视战场全景
        double overviewHeight = 50;
        keyframes.add(new CameraKeyframe(
                battlefieldCenter.x,
                battlefieldCenter.y + overviewHeight,
                battlefieldCenter.z,
                overviewYaw,  // 朝向：使进攻方在左侧，防守方在右侧
                70,     // pitch: 向下70度俯视
                100,    // 持续ticks
                "§e战场概览",  // 标题
                "§7准备进入战斗"  // 副标题
        ));

        // 第二帧：移动到进攻方复活点上方
        keyframes.add(new CameraKeyframe(
                attackerSpawn.x,
                attackerSpawn.y + 15,  // 复活点上方15格
                attackerSpawn.z,
                this.zones.isEmpty() ? 0 : calculateYawBetween(attackerSpawn, calculateZoneCenter(this.zones.get(0))),  // 朝向第一个区域
                45,     // pitch: 向下45度
                60,     // 持续60 ticks = 3秒
                "§c进攻方起点",  // 标题
                "§7开始推进"  // 副标题
        ));

        // 沿着区域推进路线飞行，按顺序展示每个区域
        for (int i = 0; i < this.zones.size(); i++) {
            Zone zone = this.zones.get(i);
            Vector3 zoneCenter = calculateZoneCenter(zone);

            // 每个区域上方停留，给玩家充足时间观察目标点
            double height = Math.max(25 - (i * 3), 10);  // 从25格逐渐降低到10格

            // 准备标题和副标题
            String zoneTitle = "§6" + zone.getName();  // 区域名称（金色）
            String zoneSubtitle = "§7控制点: §b" + zone.getControlPoints().size() + " §7个";  // 控制点数量

            // 计算朝向：如果是最后一个区域，朝向防守方复活点；否则朝向下一个区域
            float yaw;
            if (i >= this.zones.size() - 1) {
                // 最后一个区域，朝向防守方复活点
                yaw = calculateYawBetween(zoneCenter, defenderSpawn);
            } else {
                // 朝向下一个区域
                Vector3 nextZoneCenter = calculateZoneCenter(this.zones.get(i + 1));
                yaw = calculateYawBetween(zoneCenter, nextZoneCenter);
            }

            keyframes.add(new CameraKeyframe(
                    zoneCenter.x,
                    zoneCenter.y + height,
                    zoneCenter.z,
                    yaw,                         // 朝向下一个区域或防守方复活点
                    Math.max(50 - (i * 8), 30),  // 逐渐降低俯视角度，从50度到30度
                    80,                          // 持续80 ticks = 4秒
                    zoneTitle,                   // 标题
                    zoneSubtitle                 // 副标题
            ));
        }

        // 最后一帧：降落到玩家所在队伍的重生点
        Team team = this.getPlayerTeam(player);
        Vector3 spawnPoint;
        String teamName;
        if (team == Team.RED) {
            spawnPoint = attackerSpawn;
            teamName = "§c进攻方";
        } else {
            spawnPoint = defenderSpawn;
            teamName = "§9防守方";
        }

        keyframes.add(new CameraKeyframe(
                spawnPoint.x,
                spawnPoint.y + 2,  // 略高于重生点
                spawnPoint.z,
                0,      // yaw
                20,     // pitch: 向下20度
                30,     // 持续ticks
                teamName,  // 标题（队伍名称）
                "§7战斗开始"  // 副标题
        ));

        return keyframes;
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

        // 如果没有控制点，使用等待出生点
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
            return 60.0; // 默认高度
        }

        // 计算所有占领点的边界
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

        // 计算边界范围
        double rangeX = maxX - minX;
        double rangeZ = maxZ - minZ;

        // 计算对角线长度
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

        // 计算偏航角（弧度转角度）
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
            return 0; // 最后一个区域，朝北
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

        // 播放加时赛动画
        this.playOvertimeAnimation();
    }

    /**
     * 播放加时赛动画
     * 优化版：按照进攻顺序逐个查看控制点，避免大地图渲染距离问题
     */
    private void playOvertimeAnimation() {
        this.pauseGameTime = true;  // 暂停游戏时间

        // 计算战场中心点
        Vector3 battlefieldCenter = calculateBattlefieldCenter();

        // 动态计算观察高度
        double observationHeight = calculateOvertimeObservationHeight();

        // 获取进攻方和防守方复活点
        Vector3 attackerSpawn = this.getRedSpawn();
        Vector3 defenderSpawn = this.getBlueSpawn();

        // 计算概览视角的朝向
        double vx = defenderSpawn.x - attackerSpawn.x;
        double vz = defenderSpawn.z - attackerSpawn.z;
        float overviewYaw = (float) Math.toDegrees(Math.atan2(-vz, -vx));

        for (Player player : this.getPlayerDataMap().keySet()) {
            Team team = this.getPlayerTeam(player);

            // 准备不同阶段的标题
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

            // 获取玩家当前位置
            Vector3 playerPos = player.getPosition();

            // 计算玩家身后的位置（第三人称视角）
            Vector3 behindPlayer = calculateBehindPlayerPosition(player, 5);
            float yawToPlayer = calculateYawBetween(behindPlayer, playerPos);

            // 创建加时赛动画
            CameraAnimationTask.Builder builder = new CameraAnimationTask.Builder(this.gunWar, player)
                    .onComplete(this::onOvertimeAnimationComplete);

            // 第一帧：移动到玩家身后（第三人称视角）
            builder.addKeyframe(
                    behindPlayer.x,
                    behindPlayer.y + 2,  // 略高于玩家
                    behindPlayer.z,
                    yawToPlayer,  // 朝向玩家
                    10,     // 俯仰角：略微向下看
                    40,     // 持续2秒
                    "",
                    ""
            );

            // 第二帧：停留在玩家身后，显示胜败标题
            builder.addKeyframe(
                    behindPlayer.x,
                    behindPlayer.y + 2,
                    behindPlayer.z,
                    yawToPlayer,
                    10,
                    60,     // 持续3秒
                    defeatTitle,
                    defeatSubtitle
            );

            // 按照进攻顺序（zones顺序）逐个查看每个控制点
            int currentTicksAccumulated = 100; // 前两帧总共100 ticks
            int controlPointIndex = 0; // 全局控制点索引（用于遮盖实体）
            Vector3 previousPointPos = attackerSpawn;  // 进攻来源起点

            for (Zone zone : this.zones) {
                for (ControlPoint point : zone.getControlPoints()) {
                    Vector3 pointPos = point.getPosition();

                    // 计算进攻方向（从上一个点到当前点）
                    double dx = pointPos.x - previousPointPos.x;
                    double dz = pointPos.z - previousPointPos.z;
                    float attackYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

                    // 相机位置：在上一个进攻点附近，稍微抬高 + 稍微往后退
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

                    // 计算相机朝向：看向当前控制点
                    float cameraYaw = calculateYawBetween(new Vector3(cameraX, cameraY, cameraZ), pointPos);

                    // 准备标题
                    String pointTitle = (point.isCaptured() ? "§a已占领" : "§c未占领")
                            + " §7- §f" + zone.getName();
                    String pointSubtitle = "§7控制点 " + ((char)('A' + (zone.getControlPoints().indexOf(point))));

                    // 第一帧：移动到控制点斜后方（30 ticks = 1.5秒过渡时间）
                    builder.addKeyframe(
                            cameraX,
                            cameraY,
                            cameraZ,
                            cameraYaw,
                            45,
                            30,     // 1.5秒过渡时间
                            "",
                            ""
                    );

                    // 第二帧：在斜后方停留观察（70 ticks = 3.5秒停留时间）
                    builder.addKeyframe(
                            cameraX,
                            cameraY,
                            cameraZ,
                            cameraYaw,
                            45,     // 向下45度俯视
                            70,     // 3.5秒停留时间
                            pointTitle,
                            pointSubtitle
                    );

                    // 在这个控制点先生成蓝色遮盖实体
                    final int finalControlPointIndex = controlPointIndex;
                    final Vector3 finalPointPos = pointPos;
                    final boolean isCaptured = point.isCaptured();

                    // 相机移动到位后立即生成蓝色遮盖实体
                    this.gunWar.getServer().getScheduler().scheduleDelayedTask(
                            this.gunWar,
                            () -> this.spawnSingleOvertimeCoverEntity(finalControlPointIndex, finalPointPos, false),
                            currentTicksAccumulated + 30  // 在移动到位后生成
                    );

                    // 如果被占领，延迟40 ticks (2秒) 后更新为红色
                    if (isCaptured) {
                        this.gunWar.getServer().getScheduler().scheduleDelayedTask(
                                this.gunWar,
                                () -> this.updateCoverEntityColor(finalControlPointIndex, finalPointPos, true),
                                currentTicksAccumulated + 30 + 40  // 移动到位30 + 等待40
                        );
                    }

                    currentTicksAccumulated += 100;  // 30 (移动) + 70 (停留) = 100 ticks
                    controlPointIndex++;
                    previousPointPos = pointPos;  // 更新进攻来源位置
                }
            }

            // 拉到上空（快速过渡）
            builder.addKeyframe(
                    battlefieldCenter.x,
                    battlefieldCenter.y + observationHeight,
                    battlefieldCenter.z,
                    overviewYaw,
                    75,
                    40,     // 2秒过渡时间
                    "",
                    ""
            );

            // 在上空停留等待
            builder.addKeyframe(
                    battlefieldCenter.x,
                    battlefieldCenter.y + observationHeight,
                    battlefieldCenter.z,
                    overviewYaw,
                    75,
                    60,     // 3秒停留时间
                    "",
                    ""
            );

            // 最终帧：显示加时赛标题
            builder.addKeyframe(
                    battlefieldCenter.x,
                    battlefieldCenter.y + observationHeight,
                    battlefieldCenter.z,
                    overviewYaw,
                    75,
                    80,     // 4秒显示标题
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
        // 计算遮盖实体高度
        double coverY = pointPos.y + 10;
        Vector3 position = new Vector3(pointPos.x, coverY, pointPos.z);

        CompoundTag nbt = Entity.getDefaultNBT(position);

        // 根据占领状态生成对应颜色的遮盖实体
        Entity coverEntity;
        if (isCaptured) {
            coverEntity = new EntityGunWarCoverRed(this.level.getChunk((int) position.x >> 4, (int) position.z >> 4), nbt);
        } else {
            coverEntity = new EntityGunWarCoverBlue(this.level.getChunk((int) position.x >> 4, (int) position.z >> 4), nbt);
        }

        coverEntity.spawnToAll();

        // 确保列表足够大
        while (this.overtimeCoverEntities.size() <= index) {
            this.overtimeCoverEntities.add(null);
        }
        this.overtimeCoverEntities.set(index, coverEntity);
    }

    /**
     * 更新遮盖实体颜色
     */
    private void updateCoverEntityColor(int index, Vector3 pointPos, boolean toRed) {
        if (index < 0) {
            GunWar.getInstance().getLogger().warning("更新遮盖实体颜色失败：索引 " + index + " 小于0");
            return;
        }

        // 确保列表足够大
        while (this.overtimeCoverEntities.size() <= index) {
            this.overtimeCoverEntities.add(null);
        }

        // 移除旧的遮盖实体
        Entity oldEntity = this.overtimeCoverEntities.get(index);
        if (oldEntity != null && !oldEntity.isClosed()) {
            oldEntity.close();
        } else if (oldEntity == null) {
            GunWar.getInstance().getLogger().warning("更新遮盖实体颜色：索引 " + index + " 的旧实体为null");
        }

        // 计算遮盖实体高度
        double coverY = pointPos.y + 10;
        Vector3 position = new Vector3(pointPos.x, coverY, pointPos.z);

        CompoundTag nbt = Entity.getDefaultNBT(position);

        // 创建新的遮盖实体
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

        // 复活玩家到重生点（镜头会自动回到玩家身上）
        this.playerRespawn(player);

        // 检查所有玩家的动画是否都完成
        if (this.playerCameraAnimations.isEmpty()) {
            // 清除遮盖实体
            this.clearOvertimeCoverEntities();

            // 进入加时赛状态
            this.isOvertime = true;
            this.attackerCurrentResource = this.overtimeResource;
            this.gameTime = this.overtimeTime;
            this.pauseGameTime = false;  // 恢复游戏时间

            // 广播加时赛开始消息
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

        // 传送玩家到重生点
        this.playerRespawn(player);

        // 检查所有玩家的动画是否都完成
        if (this.playerCameraAnimations.isEmpty()) {
            this.cameraAnimationPlaying = false;
            this.pauseGameTime = false;  // 恢复游戏时间
        }
    }

    /**
     * 测试摄像机动画（用于调试）
     * 为单个玩家播放开场动画，不影响游戏状态
     *
     * @param player 要播放动画的玩家
     */
    public void testCameraAnimation(Player player) {
        // 生成摄像机路径
        List<CameraKeyframe> keyframes = this.generateCameraPath(player);

        // 创建并启动摄像机动画任务（测试模式不设置完成回调）
        CameraAnimationTask.Builder builder = new CameraAnimationTask.Builder(this.gunWar, player)
                .onComplete(p -> p.sendMessage("§a开场动画测试完成！"));

        // 添加所有关键帧
        for (CameraKeyframe keyframe : keyframes) {
            builder.addKeyframe(keyframe);
        }

        // 构建并启动
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
        // 随机设置控制点的占领状态（按顺序）
        boolean continueCapture = true;
        for (Zone zone : this.zones) {
            for (ControlPoint point : zone.getControlPoints()) {
                if (continueCapture) {
                    // 50%的概率被占领
                    boolean captured = Math.random() < 0.5;
                    point.setCaptured(captured);
                    // 如果这个点没被占领，后续的点也不会被占领
                    if (!captured) {
                        continueCapture = false;
                    }
                } else {
                    // 后续点都不会被占领
                    point.setCaptured(false);
                }
            }
        }

        // 计算战场中心点
        Vector3 battlefieldCenter = calculateBattlefieldCenter();

        // 动态计算观察高度
        double observationHeight = calculateOvertimeObservationHeight();

        // 获取进攻方和防守方复活点
        Vector3 attackerSpawn = this.getRedSpawn();
        Vector3 defenderSpawn = this.getBlueSpawn();

        // 计算概览视角的朝向
        double vx = defenderSpawn.x - attackerSpawn.x;
        double vz = defenderSpawn.z - attackerSpawn.z;
        float overviewYaw = (float) Math.toDegrees(Math.atan2(-vz, -vx));

        // 获取玩家队伍
        Team team = this.getPlayerTeam(player);
        team = Team.RED;

        // 准备不同阶段的标题
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

        // 获取玩家当前位置
        Vector3 playerPos = player.getPosition();

        // 计算玩家身后的位置（第三人称视角）
        Vector3 behindPlayer = calculateBehindPlayerPosition(player, 5);
        float yawToPlayer = calculateYawBetween(behindPlayer, playerPos);

        // 创建加时赛测试动画
        CameraAnimationTask.Builder builder = new CameraAnimationTask.Builder(this.gunWar, player)
                .onComplete(p -> {
                    // 动画完成后清除遮盖实体
                    this.clearOvertimeCoverEntities();
                    p.sendMessage("§a加时赛动画测试完成！");
                });

        // 第一帧：移动到玩家身后（第三人称视角）
        builder.addKeyframe(
                behindPlayer.x,
                behindPlayer.y + 2,  // 略高于玩家
                behindPlayer.z,
                yawToPlayer,  // 朝向玩家
                10,     // 俯仰角：略微向下看
                40,     // 持续2秒
                "",
                ""
        );

        // 第二帧：停留在玩家身后，显示胜败标题
        builder.addKeyframe(
                behindPlayer.x,
                behindPlayer.y + 2,
                behindPlayer.z,
                yawToPlayer,
                10,
                60,     // 持续3秒
                defeatTitle,
                defeatSubtitle
        );

        // 按照进攻顺序（zones顺序）逐个查看每个控制点
        int currentTicksAccumulated = 100; // 前两帧总共100 ticks
        int controlPointIndex = 0; // 全局控制点索引（用于遮盖实体）
        Vector3 previousPointPos = attackerSpawn;  // 进攻来源起点

        for (Zone zone : this.zones) {
            for (ControlPoint point : zone.getControlPoints()) {
                Vector3 pointPos = point.getPosition();

                // 计算进攻方向（从上一个点到当前点）
                double dx = pointPos.x - previousPointPos.x;
                double dz = pointPos.z - previousPointPos.z;
                float attackYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

                // 相机位置：在上一个进攻点附近，稍微抬高 + 稍微往后退
                double backwardDistance = 5.0;   // 往后退5格（避免太近）
                double cameraHeight = 20.0;      // 抬高20格

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

                // 计算相机朝向：看向当前控制点
                float cameraYaw = calculateYawBetween(new Vector3(cameraX, cameraY, cameraZ), pointPos);

                // 准备标题
                String pointTitle = (point.isCaptured() ? "§a已占领" : "§c未占领")
                        + " §7- §f" + zone.getName();
                String pointSubtitle = "§7控制点 " + ((char)('A' + (zone.getControlPoints().indexOf(point))));

                // 第一帧：移动到控制点斜后方（30 ticks = 1.5秒过渡时间）
                builder.addKeyframe(
                        cameraX,
                        cameraY,
                        cameraZ,
                        cameraYaw,
                        45,     // 向下45度俯视（角度减小以看清更多区域）
                        30,     // 1.5秒过渡时间
                        "",
                        ""
                );

                // 第二帧：在斜后方停留观察（70 ticks = 3.5秒停留时间）
                builder.addKeyframe(
                        cameraX,
                        cameraY,
                        cameraZ,
                        cameraYaw,
                        45,     // 向下45度俯视
                        70,     // 3.5秒停留时间
                        pointTitle,
                        pointSubtitle
                );

                // 在这个控制点先生成蓝色遮盖实体
                final int finalControlPointIndex = controlPointIndex;
                final Vector3 finalPointPos = pointPos;
                final boolean isCaptured = point.isCaptured();

                // 相机移动到位后立即生成蓝色遮盖实体
                this.gunWar.getServer().getScheduler().scheduleDelayedTask(
                        this.gunWar,
                        () -> this.spawnSingleOvertimeCoverEntity(finalControlPointIndex, finalPointPos, false),
                        currentTicksAccumulated + 30  // 在移动到位后生成
                );

                // 如果被占领，延迟40 ticks (2秒) 后更新为红色
                if (isCaptured) {
                    this.gunWar.getServer().getScheduler().scheduleDelayedTask(
                            this.gunWar,
                            () -> this.updateCoverEntityColor(finalControlPointIndex, finalPointPos, true),
                            currentTicksAccumulated + 30 + 40  // 移动到位30 + 等待40
                    );
                }

                currentTicksAccumulated += 100;  // 30 (移动) + 70 (停留) = 100 ticks
                controlPointIndex++;
                previousPointPos = pointPos;  // 更新进攻来源位置
            }
        }

        // 拉到上空（快速过渡）
        builder.addKeyframe(
                battlefieldCenter.x,
                battlefieldCenter.y + observationHeight,
                battlefieldCenter.z,
                overviewYaw,
                75,
                40,     // 2秒过渡时间
                "",
                ""
        );

        // 在上空停留等待
        builder.addKeyframe(
                battlefieldCenter.x,
                battlefieldCenter.y + observationHeight,
                battlefieldCenter.z,
                overviewYaw,
                75,
                60,     // 3秒停留时间
                "",
                ""
        );

        // 最终帧：显示加时赛标题
        builder.addKeyframe(
                battlefieldCenter.x,
                battlefieldCenter.y + observationHeight,
                battlefieldCenter.z,
                overviewYaw,
                75,
                80,     // 4秒显示标题
                overtimeTitle,
                overtimeSubtitle
        );

        // 构建并启动
        builder.buildAndStart();
    }

}

