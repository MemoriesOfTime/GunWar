package cn.lanink.gunwar.entity;

import cn.nukkit.Player;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.HugeExplodeSeedParticle;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author lt_name
 */
public class EntityGunWarBomb extends EntityPrimedTNT {

    private final Player source;

    public EntityGunWarBomb(FullChunk chunk, CompoundTag nbt, Player source) {
        super(chunk, nbt, source);
        this.source = source;
        this.setNameTagAlwaysVisible(false);
        this.fuse = 50*20;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }
        int tickDiff = currentTick - this.lastUpdate;
        if (tickDiff <= 0 && !justCreated) {
            return true;
        }
        if (this.fuse % 5 == 0) {
            this.setDataProperty(new IntEntityData(DATA_FUSE_LENGTH, this.fuse));
        }
        this.fuse -= tickDiff;
        if (this.fuse <= 0) {
            this.level.addSound(this, Sound.RANDOM_EXPLODE);
            this.level.addParticle(new HugeExplodeSeedParticle(this));
            this.kill();
            this.close();
            return false;
        }
        this.setNameTag(String.valueOf(this.fuse/20));
        this.lastUpdate = currentTick;
        this.entityBaseTick(tickDiff);
        if (isAlive()) {
            motionY -= getGravity();
            move(motionX, motionY, motionZ);
            float friction = 1 - getDrag();
            motionX *= friction;
            motionY *= friction;
            motionZ *= friction;
            this.updateMovement();
            if (this.onGround) {
                this.motionY *= -0.5;
                motionX *= 0.7;
                motionZ *= 0.7;
            }
        }
        return true;
    }

    @Override
    public void explode() {

    }

    @Override
    public Player getSource() {
        return this.source;
    }

}
