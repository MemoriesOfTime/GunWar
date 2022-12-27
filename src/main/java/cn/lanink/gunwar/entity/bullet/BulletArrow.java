package cn.lanink.gunwar.entity.bullet;

import cn.lanink.gunwar.GunWar;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

/**
 * @author LT_Name
 */
public class BulletArrow extends EntityArrow implements IBullet {

    protected float gravity = 0.05F;
    protected ParticleEffect particleEffect;

    private BulletArrow(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    public static void launch(EntityCreature entityCreature,
                              Vector3 directionVector,
                              float gravity,
                              float motionMultiply,
                              ParticleEffect particleEffect
                              /*CompoundTag compoundTag*/) {
        CompoundTag nbt = (new CompoundTag())
                .putList((new ListTag<>("Pos"))
                        .add(new DoubleTag("", entityCreature.x))
                        .add(new DoubleTag("", entityCreature.y + entityCreature.getEyeHeight()))
                        .add(new DoubleTag("", entityCreature.z)))
                .putList((new ListTag<>("Motion"))
                        .add(new DoubleTag("", directionVector.x))
                        .add(new DoubleTag("", directionVector.y))
                        .add(new DoubleTag("", directionVector.z)))
                .putList((new ListTag<>("Rotation"))
                        .add(new FloatTag("", (float)entityCreature.yaw))
                        .add(new FloatTag("", (float)entityCreature.pitch)));
        //nbt.putCompound(BaseItem.GUN_WAR_ITEM_TAG, compoundTag);
        BulletArrow bulletArrow = new BulletArrow(entityCreature.getChunk(), nbt, entityCreature);
        bulletArrow.setGravity(gravity);
        bulletArrow.setParticleEffect(particleEffect);
        bulletArrow.setMotion(bulletArrow.getMotion().multiply(motionMultiply));
        bulletArrow.setPickupMode(PICKUP_NONE);
        for (Player p : entityCreature.getLevel().getChunkPlayers(entityCreature.getFloorX() >> 4, entityCreature.getFloorZ() >> 4).values()) {
            if (p != entityCreature) {
                bulletArrow.spawnTo(p);
            }
        }
        if (entityCreature instanceof Player) {
            Server.getInstance().getScheduler().scheduleDelayedTask(GunWar.getInstance(), () -> {
                if (!bulletArrow.isClosed()) {
                    bulletArrow.spawnTo((Player) entityCreature);
                }
            }, 10);
        }
        entityCreature.getLevel().addLevelSoundEvent(entityCreature, LevelSoundEventPacket.SOUND_BOW);
    }

    @Override
    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    @Override
    public float getGravity() {
        return this.gravity;
    }

    @Override
    public void setParticleEffect(ParticleEffect particleEffect) {
        this.particleEffect = particleEffect;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        boolean onUpdate = super.onUpdate(currentTick);
        if (this.particleEffect != null && !this.closed &&
                !this.onGround && !this.hadCollision && currentTick %5 == 0) {
            this.level.addParticleEffect(this, this.particleEffect);
        }
        return onUpdate;
    }

}
