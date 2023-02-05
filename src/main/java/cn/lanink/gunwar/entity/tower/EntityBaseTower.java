package cn.lanink.gunwar.entity.tower;

import cn.lanink.gunwar.entity.bullet.BulletArrow;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author LT_Name
 */
public class EntityBaseTower extends EntityHuman {

    private TowerDefinition towerDefinition = TowerDefinition.DEFAULT;
    private final BaseRoom room;
    private Team team;

    private final PriorityQueue<EntityCreature> targetQueue = new PriorityQueue<>(Comparator.comparingDouble(o -> o.distance(this)));
    private EntityCreature nowTarget;

    private int attackTick;

    public EntityBaseTower(@NotNull BaseRoom room, @NotNull Team team, @NotNull FullChunk chunk, @NotNull CompoundTag nbt) {
        super(chunk, nbt.putCompound("Skin", new CompoundTag()));
        this.room = room;
        this.team = team;
    }

    public void setTowerDefinition(@NotNull TowerDefinition towerDefinition) {
        this.towerDefinition = towerDefinition;
        this.setMaxHealth(towerDefinition.getMaxHealth());
        this.setHealth(this.getMaxHealth());
    }

    public boolean addTarget(@NotNull EntityCreature entity) {
        return this.targetQueue.offer(entity);
    }

    public boolean removeTarget(@NotNull EntityCreature entity) {
        return this.targetQueue.remove(entity);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        this.checkTarget();
        if (this.nowTarget != null) {
            this.seeTarget(this.nowTarget);
            if (this.attackTick >= this.towerDefinition.getAttackSpeed()) {
                this.fire();
                this.attackTick = 0;
            }else {
                this.attackTick++;
            }
        }else {
            this.pitch = 0;
        }

        //TODO 头顶显示血条

        return super.onUpdate(currentTick);
    }

    protected void checkTarget() {
        EntityCreature target = this.targetQueue.peek();
        if (target != null) {
            double distance = this.distance(target);
            if (distance >= this.towerDefinition.getAttackMinRange() && distance <= this.towerDefinition.getAttackMaxRange()) {
                //TODO 检查遮挡物
                this.nowTarget = target;
                return;
            }
        }
        this.nowTarget = null;
    }

    protected void seeTarget(Vector3 target) {
        double dx = this.x - target.x;
        double dy = this.y - (target.y + 0.5);
        double dz = this.z - target.z;
        double yaw = Math.asin(dx / Math.sqrt(dx * dx + dz * dz)) / Math.PI * 180.0D;
        double pitch = Math.round(Math.asin(dy / Math.sqrt(dx * dx + dz * dz + dy * dy)) / Math.PI * 180.0D);
        if (dz > 0.0D) {
            yaw = -yaw + 180.0D;
        }
        this.yaw = yaw;
        this.headYaw = yaw;
        this.pitch = pitch;
    }

    protected void fire() {
        //TODO 射击
        BulletArrow.launch(
                this,
                this.getDirectionVector(),
                0.05F,
                1.8F,
                null);

        //.putDouble("damage", damage)
    }

}
