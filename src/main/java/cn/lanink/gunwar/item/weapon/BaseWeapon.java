package cn.lanink.gunwar.item.weapon;

import cn.lanink.gunwar.item.base.BaseItem;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author lt_name
 */
public abstract class BaseWeapon extends BaseItem {

    protected final double minDamage;
    protected final double maxDamage;
    public final ArrayList<Effect> effects = new ArrayList<>();
    protected final int attackCooldown;
    protected final String killMessage;

    public BaseWeapon(String name, Config config) {
        super(name, config);
        String[] stringDamage = config.getString("damage").split("-");
        this.minDamage = Math.abs(Double.parseDouble(stringDamage[0]));
        if (stringDamage.length > 1) {
            this.maxDamage = Math.abs(Double.parseDouble(stringDamage[1]));
        }else {
            this.maxDamage = minDamage;
        }
        for (Map map : config.getMapList("effect")) {
            try {
                int id = (int) map.getOrDefault("id", 9);
                int amplifier = (int) map.getOrDefault("amplifier", 1);
                int duration = (int) map.getOrDefault("duration", 20);
                boolean visible = (boolean) map.getOrDefault("visible", true);
                Effect effect = Effect.getEffect(id).setAmplifier(amplifier).setDuration(duration).setVisible(visible);
                Map<String, Integer> color = (Map<String, Integer>) map.get("color");
                if (visible && color != null) {
                    effect.setColor(color.get("r"), color.get("g"), color.get("b"));
                }
                this.effects.add(effect);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.attackCooldown = config.getInt("attackCooldown");
        for (Map map : config.getMapList("enchantment")) {
            try {
                int id = (int) map.getOrDefault("id", 17);
                int level = (int) map.getOrDefault("level", 1);
                Enchantment enchantment = Enchantment.get(id);
                enchantment.setLevel(level);
                this.item.addEnchantment(enchantment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.killMessage = config.getString("killMessage");
        this.getCompoundTag()
                .putDouble("minDamage", this.minDamage)
                .putDouble("maxDamage", this.maxDamage)
                .putInt("attackCooldown", this.attackCooldown)
                .putString("killMessage", this.killMessage);
    }

    public double getMinDamage() {
        return this.minDamage;
    }

    public double getMaxDamage() {
        return this.maxDamage;
    }

    public double getRandomDamage() {
        return Tools.randomDouble(this.getMinDamage(), this.getMaxDamage());
    }

    public LinkedList<Effect> getEffects() {
        LinkedList<Effect> list = new LinkedList<>();
        for (Effect effect : this.effects) {
            list.add(effect.clone());
        }
        return list;
    }

    /**
     * @return 攻击间隔
     */
    public int getAttackCooldown() {
        return this.attackCooldown;
    }

    public String getKillMessage() {
        return this.killMessage;
    }

}
