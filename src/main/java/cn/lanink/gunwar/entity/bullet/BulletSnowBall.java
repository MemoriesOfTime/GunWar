package cn.lanink.gunwar.entity.bullet;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.base.BaseItem;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

/**
 * @author lt_name
 */
public class BulletSnowBall extends EntitySnowball {

    static {
        Entity.registerEntity("BulletSnowBall", BulletSnowBall.class);
    }

    protected float gravity = 0.03F;

    public BulletSnowBall(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public BulletSnowBall(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    public BulletSnowBall(FullChunk chunk, CompoundTag nbt, Entity shootingEntity, float gravity) {
        super(chunk, nbt, shootingEntity);
        this.gravity = gravity;
    }

    public static void launch(Player player,
                              Vector3 directionVector,
                              float gravity,
                              float motionMultiply,
                              CompoundTag compoundTag) {
        CompoundTag nbt = (new CompoundTag())
                .putList((new ListTag<>("Pos"))
                        .add(new DoubleTag("", player.x))
                        .add(new DoubleTag("", player.y + (double)player.getEyeHeight() - 0.30000000149011613D))
                        .add(new DoubleTag("", player.z)))
                .putList((new ListTag<>("Motion"))
                        .add(new DoubleTag("", directionVector.x))
                        .add(new DoubleTag("", directionVector.y))
                        .add(new DoubleTag("", directionVector.z)))
                .putList((new ListTag<>("Rotation"))
                        .add(new FloatTag("", (float)player.yaw))
                        .add(new FloatTag("", (float)player.pitch)));
        nbt.putCompound(BaseItem.GUN_WAR_ITEM_TAG, compoundTag);
        BulletSnowBall bulletSnowBall = (BulletSnowBall) Entity.createEntity("BulletSnowBall", player.getLevel().getChunk(player.getFloorX() >> 4, player.getFloorZ() >> 4), nbt, player);
        if (bulletSnowBall != null) {
            bulletSnowBall.setGravity(gravity);
            bulletSnowBall.setMotion(bulletSnowBall.getMotion().multiply(motionMultiply));
            for (Player p : player.getLevel().getChunkPlayers(player.getFloorX() >> 4, player.getFloorZ() >> 4).values()) {
                if (p != player) {
                    bulletSnowBall.spawnTo(p);
                }
            }
            Server.getInstance().getScheduler().scheduleDelayedTask(GunWar.getInstance(), () -> {
                if (!bulletSnowBall.isClosed()) {
                    bulletSnowBall.spawnTo(player);
                }
            }, 10);
            player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_BOW);
        }
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    protected float getGravity() {
        return this.gravity;
    }

}
