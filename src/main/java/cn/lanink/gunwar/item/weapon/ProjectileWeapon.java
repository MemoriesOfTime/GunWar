package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.utils.exception.item.weapon.ProjectileWeaponLoadException;
import cn.nukkit.item.ProjectileItem;
import cn.nukkit.level.particle.HugeExplodeSeedParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;

/**
 * @author lt_name
 */
public class ProjectileWeapon extends BaseWeapon {

    protected float range;
    protected String particle;

    public ProjectileWeapon(String name, Config config) throws ProjectileWeaponLoadException {
        super(name, config);
        if (!(this.item instanceof ProjectileItem)) {
            throw new ProjectileWeaponLoadException("name:" + this.getName() +
                    " ID:" + this.item.getId() + ":" + this.item.getDamage() +
                    " 不属于抛掷物");
        }
        this.particle = config.getString("particle");
        this.range = Math.abs((float) config.getDouble("range"));
        this.getCompoundTag().putFloat("range", this.range);
    }

    @Override
    public ItemManage.ItemType getItemType() {
        return ItemManage.ItemType.WEAPON_PROJECTILE;
    }

    public String getParticle() {
        return this.particle;
    }

    /**
     * @return 有效半径
     */
    public float getRange() {
        return this.range;
    }

    /**
     * 根据距离获取伤害
     * @param distance 距离
     * @return 伤害
     */
    public double getDamage(double distance) {
        if (distance > this.getRange()) {
            return 0;
        }
        double damage = Math.max((this.getRange() - distance) / this.getRange() * this.getMaxDamage() * 1.1, this.getMinDamage());
        return Math.min(damage, this.getMaxDamage());
    }

    /**
     * @param vector3 位置
     * @return 粒子
     */
    public Particle getParticle(Vector3 vector3) {
        try {
            String[] stringClass = this.getParticle().split("@");
            String className = "cn.nukkit.level.particle." + stringClass[0];
            Class<? extends Particle> particleClass = (Class<? extends Particle>) Class.forName(className);
            LinkedHashMap<Class<?>, Object> map = new LinkedHashMap<>();
            String[] strings = stringClass[1].split("&");
            for (String string : strings) {
                String[] s = string.split(":");
                if ("Vector3".equalsIgnoreCase(s[0])) {
                    map.put(Vector3.class, vector3);
                }else if ("int".equalsIgnoreCase(s[0])) {
                    map.put(int.class, Integer.parseInt(s[1]));
                }
            }
            Constructor<? extends Particle> constructor = particleClass.getConstructor(map.keySet().toArray(new Class<?>[]{}));
            return constructor.newInstance(map.values().toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HugeExplodeSeedParticle(vector3);
    }

}
