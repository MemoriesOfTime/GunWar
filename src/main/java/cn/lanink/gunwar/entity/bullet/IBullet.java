package cn.lanink.gunwar.entity.bullet;

import cn.nukkit.level.ParticleEffect;

/**
 * @author lt_name
 */
public interface IBullet {

    void setGravity(float gravity);

    float getGravity();

    void setParticleEffect(ParticleEffect particleEffect);

}
