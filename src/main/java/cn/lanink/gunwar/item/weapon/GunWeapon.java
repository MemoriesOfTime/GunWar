package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.BulletSnowBall;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.tasks.GunReloadTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
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
    protected float reloadTime;

    protected ConcurrentHashMap<Player, Integer> magazineMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Player, Task> reloadTask = new ConcurrentHashMap<>();

    public GunWeapon(String name, Config config) {
        super(name, config);
        this.maxMagazine = config.getInt("maxMagazine");
        this.gravity = (float) config.getDouble("gravity");
        this.reloadTime = (float) config.getDouble("reloadTime");
    }

    @Override
    public ItemManage.ItemType getItemType() {
        return ItemManage.ItemType.GUN_WEAPON;
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
    public float reloadTime() {
        return this.reloadTime;
    }

    /**
     * @return 重力
     */
    public float getGravity() {
        return this.gravity;
    }

    public ConcurrentHashMap<Player, Integer> getMagazineMap() {
        return this.magazineMap;
    }

    public int shooting(Player player, Vector3 directionVector) {
        int bullets = this.magazineMap.getOrDefault(player, this.getMaxMagazine());
        if (bullets > 0) {
            this.stopReload(player);
            BulletSnowBall.launch(player, directionVector, this.getGravity(), this.getCompoundTag());
            this.magazineMap.put(player, --bullets);
        }
        if (bullets <= 0) {
            this.startReload(player);
        }
        return bullets;
    }

    public ConcurrentHashMap<Player, Task> getReloadTask() {
        return this.reloadTask;
    }

    public void startReload(Player player) {
        if (!this.reloadTask.containsKey(player)) {
            PluginTask<GunWar> task = new GunReloadTask(
                    GunWar.getInstance(), player, this,
                    (this.maxMagazine / this.reloadTime));
            Server.getInstance().getScheduler().scheduleRepeatingTask(task, 1);
            this.reloadTask.put(player, task);
        }
    }

    public void stopReload(Player player) {
        if (this.reloadTask.containsKey(player)) {
            this.reloadTask.get(player).cancel();
            this.reloadTask.remove(player);
        }
    }

}
