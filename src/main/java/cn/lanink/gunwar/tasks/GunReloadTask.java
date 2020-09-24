package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

/**
 * @author lt_name
 */
public class GunReloadTask extends PluginTask<GunWar> {

    private final Player player;
    private final GunWeapon gunWeapon;
    private float bulletsFloat = 0;
    private final float base;

    public GunReloadTask(GunWar owner, Player player, GunWeapon gunWeapon, float base) {
        super(owner);
        this.player = player;
        this.gunWeapon = gunWeapon;
        this.base = base;
    }

    @Override
    public void onRun(int i) {
        if (this.bulletsFloat >= this.gunWeapon.getMaxMagazine()) {
            this.cancel();
        }else {
            this.bulletsFloat += this.base;
            this.gunWeapon.getMagazineMap().put(this.player, (int) this.bulletsFloat);
            player.sendPopup("\n" + (int) this.bulletsFloat + "/" + this.gunWeapon.getMaxMagazine());
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        this.gunWeapon.getReloadTask().remove(player);
    }

}
