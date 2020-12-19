package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.bullet.BulletSnowBall;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.tasks.GunReloadTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lt_name
 */
public class GunWeapon extends BaseWeapon {

    protected int maxMagazine;
    protected float gravity;
    protected float motionMultiply;
    protected float reloadTime;
    protected boolean reloadInterrupted;
    protected ParticleEffect particleEffect;

    protected ConcurrentHashMap<Player, Integer> magazineMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Player, Task> reloadTask = new ConcurrentHashMap<>();

    public GunWeapon(String name, Config config) {
        super(name, config);
        this.item.getNamedTag().putByte("Unbreakable", 1);
        this.maxMagazine = config.getInt("maxMagazine");
        this.gravity = (float) config.getDouble("gravity");
        this.motionMultiply = (float) config.getDouble("motionMultiply");
        this.reloadTime = (float) config.getDouble("reloadTime");
        this.reloadInterrupted = config.getBoolean("reloadInterrupted");
        String stringParticleEffect = config.getString("particleEffect").trim();
        if (!"".equals(stringParticleEffect)) {
            this.particleEffect = ParticleEffect.valueOf(stringParticleEffect);
        }
        this.getCompoundTag().putInt("maxMagazine", this.maxMagazine)
                .putFloat("gravity", this.gravity)
                .putFloat("motionMultiply", this.motionMultiply)
                .putFloat("reloadTime", this.reloadTime)
                .putBoolean("reloadInterrupted", this.reloadInterrupted);
    }

    @Override
    public ItemManage.ItemType getItemType() {
        return ItemManage.ItemType.WEAPON_GUN;
    }

    /**
     * @return 弹匣容量
     */
    public int getMaxMagazine() {
        return this.maxMagazine;
    }

    /**
     * @return 装填时间
     */
    public float getReloadTime() {
        return this.reloadTime;
    }

    public boolean isReloadInterrupted() {
        return this.reloadInterrupted;
    }

    /**
     * @return 重力
     */
    public float getGravity() {
        return this.gravity;
    }

    public float getMotionMultiply() {
        return this.motionMultiply;
    }

    public ParticleEffect getParticleEffect() {
        return this.particleEffect;
    }

    public ConcurrentHashMap<Player, Integer> getMagazineMap() {
        return this.magazineMap;
    }

    public int getMagazine(Player player) {
        return this.magazineMap.getOrDefault(player, 0);
    }

    public int shooting(Player player, Vector3 directionVector) {
        int bullets = this.magazineMap.getOrDefault(player, this.getMaxMagazine());
        if (bullets > 0) {
            if (!this.isReload(player) || this.isReloadInterrupted()) {
                this.stopReload(player);
                BulletSnowBall.launch(
                        player,
                        directionVector,
                        this.getGravity(),
                        this.getMotionMultiply(),
                        this.getParticleEffect(),
                        this.getCompoundTag());
                this.magazineMap.put(player, --bullets);
            }
        }
        if (bullets <= 0) {
            this.startReload(player);
        }
        return bullets;
    }

    public ConcurrentHashMap<Player, Task> getReloadTask() {
        return this.reloadTask;
    }

    public boolean isReload(Player player) {
        return this.reloadTask.containsKey(player);
    }

    public void startReload(Player player) {
        if (this.getMagazine(player) < this.getMaxMagazine() &&
                !this.reloadTask.containsKey(player)) {
            PluginTask<GunWar> task = new GunReloadTask(
                    player, this, (this.getMaxMagazine() / this.getReloadTime()));
            Server.getInstance().getScheduler().scheduleRepeatingTask(task, 1, true);
            this.reloadTask.put(player, task);
        }
    }

    public void stopReload(Player player) {
        if (this.isReload(player)) {
            this.reloadTask.get(player).cancel();
            this.reloadTask.remove(player);
        }
    }

}
