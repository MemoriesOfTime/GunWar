package cn.lanink.gunwar.entity.bullet;

import cn.nukkit.Player;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author lt_name
 */
public interface IBullet {

    static void launch(Player player,
                       Vector3 directionVector,
                       float gravity,
                       float motionMultiply,
                       ParticleEffect particleEffect,
                       CompoundTag compoundTag) {

    }

    float getGravity();

    void setParticleEffect(ParticleEffect particleEffect);

}
