package cn.lanink.gunwar.camera;

import cn.nukkit.math.Vector3;
import lombok.Data;

/**
 * 摄像机关键帧
 * 用于定义摄像机动画中的关键位置和视角
 *
 * @author LT_Name
 */
@Data
public class CameraKeyframe {

    /**
     * 关键帧位置
     */
    private Vector3 position;

    /**
     * 视角偏航角（yaw）
     */
    private float yaw;

    /**
     * 视角俯仰角（pitch）
     */
    private float pitch;

    /**
     * 关键帧持续时间（tick）
     */
    private int duration;

    /**
     * 显示的标题文本（可选）
     */
    private String title;

    /**
     * 显示的副标题文本（可选）
     */
    private String subtitle;

    /**
     * 创建一个关键帧（带标题）
     *
     * @param position 位置
     * @param yaw 偏航角
     * @param pitch 俯仰角
     * @param duration 持续时间（tick）
     * @param title 标题文本
     * @param subtitle 副标题文本
     */
    public CameraKeyframe(Vector3 position, float yaw, float pitch, int duration, String title, String subtitle) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.duration = duration;
        this.title = title;
        this.subtitle = subtitle;
    }

    /**
     * 创建一个关键帧（不带标题）
     *
     * @param position 位置
     * @param yaw 偏航角
     * @param pitch 俯仰角
     * @param duration 持续时间（tick）
     */
    public CameraKeyframe(Vector3 position, float yaw, float pitch, int duration) {
        this(position, yaw, pitch, duration, null, null);
    }

    /**
     * 创建一个关键帧
     *
     * @param x 位置X坐标
     * @param y 位置Y坐标
     * @param z 位置Z坐标
     * @param yaw 偏航角
     * @param pitch 俯仰角
     * @param duration 持续时间（tick）
     */
    public CameraKeyframe(double x, double y, double z, float yaw, float pitch, int duration) {
        this(new Vector3(x, y, z), yaw, pitch, duration, null, null);
    }

    /**
     * 创建一个关键帧（带标题）
     *
     * @param x 位置X坐标
     * @param y 位置Y坐标
     * @param z 位置Z坐标
     * @param yaw 偏航角
     * @param pitch 俯仰角
     * @param duration 持续时间（tick）
     * @param title 标题文本
     * @param subtitle 副标题文本
     */
    public CameraKeyframe(double x, double y, double z, float yaw, float pitch, int duration, String title, String subtitle) {
        this(new Vector3(x, y, z), yaw, pitch, duration, title, subtitle);
    }

    /**
     * 在两个关键帧之间进行线性插值
     *
     * @param other 另一个关键帧
     * @param progress 插值进度（0.0 - 1.0）
     * @return 插值后的位置和视角
     */
    public CameraKeyframe lerp(CameraKeyframe other, float progress) {
        // 位置插值
        double x = this.position.x + (other.position.x - this.position.x) * progress;
        double y = this.position.y + (other.position.y - this.position.y) * progress;
        double z = this.position.z + (other.position.z - this.position.z) * progress;

        // 角度插值
        float yaw = lerpAngle(this.yaw, other.yaw, progress);
        float pitch = lerpAngle(this.pitch, other.pitch, progress);

        return new CameraKeyframe(x, y, z, yaw, pitch, 0);
    }

    /**
     * 角度线性插值（处理角度环绕问题）
     *
     * @param start 起始角度
     * @param end 结束角度
     * @param progress 插值进度
     * @return 插值后的角度
     */
    private float lerpAngle(float start, float end, float progress) {
        // 处理角度环绕（-180到180度）
        float diff = end - start;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }

        float result = start + diff * progress;

        // 标准化到-180到180范围
        while (result > 180) result -= 360;
        while (result < -180) result += 360;

        return result;
    }

    /**
     * 克隆关键帧
     *
     * @return 新的关键帧实例
     */
    public CameraKeyframe clone() {
        return new CameraKeyframe(
            this.position.clone(),
            this.yaw,
            this.pitch,
            this.duration,
            this.title,
            this.subtitle
        );
    }
}
