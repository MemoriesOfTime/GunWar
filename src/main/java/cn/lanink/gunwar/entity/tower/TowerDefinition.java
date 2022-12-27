package cn.lanink.gunwar.entity.tower;

import cn.nukkit.entity.data.Skin;
import lombok.Data;

/**
 * @author LT_Name
 */
@Data
public class TowerDefinition {

    public static TowerDefinition DEFAULT = new TowerDefinition();

    private String name;
    private Skin skin;
    private int maxHealth; //血量
    private int attackDamage; //伤害
    private int attackSpeed; //攻速
    private int attackMaxRange; //最大范围
    private int attackMinRange; //最小范围

}
