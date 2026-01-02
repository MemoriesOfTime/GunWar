package cn.lanink.gunwar.camera;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.Player;
import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.CameraInstructionPacket;
import cn.nukkit.network.protocol.types.camera.CameraEase;
import cn.nukkit.network.protocol.types.camera.CameraPreset;
import cn.nukkit.network.protocol.types.camera.CameraSetInstruction;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.CameraPresetManager;
import org.cloudburstmc.protocol.common.util.OptionalBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 摄像机动画任务
 * 使用Nukkit Camera API实现摄像机动画效果
 *
 * @author LT_Name
 */
public class CameraAnimationTask extends PluginTask<GunWar> {

    private final Player player;
    private final List<CameraKeyframe> keyframes;
    private final Consumer<Player> onComplete;
    private final CameraPreset cameraPreset;  // 使用的摄像机预设

    private int currentKeyframeIndex = 0;
    private int tickInCurrentKeyframe = 0;
    private boolean cancelled = false;

    /**
     * 创建摄像机动画任务
     *
     * @param plugin GunWar插件实例
     * @param player 目标玩家
     * @param keyframes 关键帧列表
     * @param onComplete 动画完成回调
     */
    public CameraAnimationTask(GunWar plugin, Player player, List<CameraKeyframe> keyframes,
                              Consumer<Player> onComplete) {
        super(plugin);
        this.player = player;
        this.keyframes = new ArrayList<>(keyframes);
        this.onComplete = onComplete;

        // 获取摄像机预设（使用free预设以获得完全控制）
        this.cameraPreset = CameraPresetManager.FREE;

        // 准备播放动画
        this.prepareAnimation();
    }

    /**
     * 准备动画播放
     */
    private void prepareAnimation() {
        if (this.keyframes.isEmpty()) {
            this.completeAnimation();
            return;
        }

        // 设置第一个关键帧的摄像机位置
        CameraKeyframe firstKeyframe = this.keyframes.get(0);
        this.setCameraPosition(firstKeyframe, 0);

        // 发送第一帧的标题
        if ((firstKeyframe.getTitle() != null && !firstKeyframe.getTitle().isBlank())
                || (firstKeyframe.getSubtitle() != null && !firstKeyframe.getSubtitle().isBlank())) {
            this.sendTitle(firstKeyframe.getTitle(), firstKeyframe.getSubtitle());
        }
    }

    @Override
    public void onRun(int currentTick) {
        // 检查玩家是否在线
        if (!player.isOnline() || cancelled) {
            this.cancel();
            return;
        }

        // 更新时间
        tickInCurrentKeyframe++;

        // 检查是否需要移动到下一个关键帧
        CameraKeyframe current = keyframes.get(currentKeyframeIndex);
        if (tickInCurrentKeyframe >= current.getDuration()) {
            currentKeyframeIndex++;
            tickInCurrentKeyframe = 0;

            // 检查是否完成所有关键帧
            if (currentKeyframeIndex >= keyframes.size()) {
                this.completeAnimation();
                this.cancel();
                return;
            }

            // 移动到下一个关键帧
            CameraKeyframe next = keyframes.get(currentKeyframeIndex);
            float easeTime = next.getDuration() / 20.0f;  // 转换为秒
            this.setCameraPosition(next, easeTime);

            // 如果关键帧有标题，发送标题
            if ((next.getTitle() != null && !next.getTitle().isBlank())
                    || (next.getSubtitle() != null && !next.getSubtitle().isBlank())) {
                this.sendTitle(next.getTitle(), next.getSubtitle());
            }
        }
    }

    /**
     * 设置摄像机位置
     *
     * @param keyframe 关键帧
     * @param easeTime 过渡时间（秒）
     */
    private void setCameraPosition(CameraKeyframe keyframe, float easeTime) {
        CameraInstructionPacket packet = new CameraInstructionPacket();
        CameraSetInstruction setInstruction = new CameraSetInstruction();

        // 设置摄像机预设
        setInstruction.setPreset(this.cameraPreset);

        // 设置位置
        Vector3f pos = new Vector3f(
                (float) keyframe.getPosition().x,
                (float) keyframe.getPosition().y,
                (float) keyframe.getPosition().z
        );
        setInstruction.setPos(pos);

        // 设置旋转（pitch, yaw）
        Vector2f rot = new Vector2f(keyframe.getPitch(), keyframe.getYaw());
        setInstruction.setRot(rot);

        // 设置平滑过渡
        if (easeTime > 0) {
            CameraSetInstruction.EaseData easeData = new CameraSetInstruction.EaseData(
                    CameraEase.LINEAR,  // 使用线性过渡
                    easeTime
            );
            setInstruction.setEase(easeData);
        }

        packet.setSetInstruction(setInstruction);
        player.dataPacket(packet);
    }

    /**
     * 发送标题
     *
     * @param title 标题文本
     * @param subtitle 副标题文本
     */
    private void sendTitle(String title, String subtitle) {
        if (title != null) {
            player.sendTitle(title, subtitle != null ? subtitle : "", 10, 60, 10);
        } else if (subtitle != null) {
            player.sendTitle("", subtitle, 10, 60, 10);
        }
    }

    /**
     * 完成动画
     */
    private void completeAnimation() {
        // 清除摄像机设置，恢复到默认视角
        CameraInstructionPacket packet = new CameraInstructionPacket();
        packet.setClear(OptionalBoolean.of(true));
        player.dataPacket(packet);

        if (onComplete != null) {
            onComplete.accept(player);
        }
    }

    /**
     * 取消动画
     */
    public void cancelAnimation() {
        this.cancelled = true;
        this.completeAnimation();
    }

    /**
     * 构建器类，用于方便地创建摄像机动画
     */
    public static class Builder {
        private final GunWar plugin;
        private final Player player;
        private final List<CameraKeyframe> keyframes = new ArrayList<>();
        private Consumer<Player> onComplete;

        public Builder(GunWar plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
        }

        /**
         * 添加关键帧
         */
        public Builder addKeyframe(CameraKeyframe keyframe) {
            this.keyframes.add(keyframe);
            return this;
        }

        /**
         * 添加关键帧（不带标题）
         */
        public Builder addKeyframe(double x, double y, double z, float yaw, float pitch, int duration) {
            return addKeyframe(new CameraKeyframe(x, y, z, yaw, pitch, duration));
        }

        /**
         * 添加关键帧（带标题）
         */
        public Builder addKeyframe(double x, double y, double z, float yaw, float pitch, int duration, String title, String subtitle) {
            return addKeyframe(new CameraKeyframe(x, y, z, yaw, pitch, duration, title, subtitle));
        }

        /**
         * 设置完成回调
         */
        public Builder onComplete(Consumer<Player> callback) {
            this.onComplete = callback;
            return this;
        }

        /**
         * 构建动画任务
         *
         * @return 动画任务实例
         */
        public CameraAnimationTask build() {
            if (keyframes.isEmpty()) {
                throw new IllegalStateException("至少需要添加一个关键帧");
            }
            return new CameraAnimationTask(plugin, player, keyframes, onComplete);
        }

        /**
         * 构建并立即启动动画
         *
         * @return 动画任务实例
         */
        public CameraAnimationTask buildAndStart() {
            CameraAnimationTask task = build();
            plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, task, 1);
            return task;
        }
    }
}
