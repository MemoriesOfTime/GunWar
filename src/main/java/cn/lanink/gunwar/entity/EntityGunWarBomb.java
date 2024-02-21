package cn.lanink.gunwar.entity;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.PlayerGameData;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.nukkit.Player;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.HugeExplodeSeedParticle;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.Map;

/**
 * @author lt_name
 */
public class EntityGunWarBomb extends EntityPrimedTNT {

    private final BlastingModeRoom room;
    private final Player source;

    public EntityGunWarBomb(FullChunk chunk, CompoundTag nbt, BlastingModeRoom room, Player source) {
        super(chunk, nbt, source);
        this.room = room;
        this.source = source;
        this.setNameTagAlwaysVisible(false);
        this.fuse = room.getBombExplosionTime() * 20;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.fuse = 50*20;
    }

    @Override
    public String getName() {
        return GunWar.getInstance().getLanguage().translateString("item_Bomb_Name");
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
            for (Map.Entry<Player, PlayerGameData> entry : this.room.getPlayerDataMap().entrySet()) {
                double distance = entry.getKey().distance(this);
                if ((entry.getValue().getTeam() == Team.RED || entry.getValue().getTeam() == Team.BLUE) &&
                        distance < this.room.getBlastingPointRadius()) {
                    this.room.lessHealth(entry.getKey(), this,
                            (float) (20 * (this.room.getBlastingPointRadius() - distance) / this.room.getBlastingPointRadius()) + 5);
                }
            }
            this.kill();
            this.close();
            this.room.bombExplosion();
            return false;
        }
        this.setNameTag(String.valueOf(this.getExplosionTime()));
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

    /**
     * @return 爆炸倒计时
     */
    public int getExplosionTime() {
        return this.fuse / 20;
    }

}
