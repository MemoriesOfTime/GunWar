package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

/**
 * @author lt_name
 */
public class GunReloadTask extends PluginTask<GunWar> {

    private final Player player;
    private final GunWeapon gunWeapon;
    private final float base;
    private float bulletsFloat;

    public GunReloadTask(GunWar owner, Player player, GunWeapon gunWeapon, float base) {
        super(owner);
        this.player = player;
        this.gunWeapon = gunWeapon;
        this.base = base;
        this.bulletsFloat = gunWeapon.getMagazine(player);
    }

    @Override
    public void onRun(int i) {
        if (this.bulletsFloat >= this.gunWeapon.getMaxMagazine() || !this.player.isOnline()) {
            this.cancel();
        }else {
            this.bulletsFloat += this.base;
            this.gunWeapon.getMagazineMap().put(this.player, (int) this.bulletsFloat);
            if (this.gunWeapon.equals(ItemManage.getGunWeapon(this.player.getInventory().getItemInHand()))) {
                this.player.sendPopup("\n" + (int) this.bulletsFloat + "/" + this.gunWeapon.getMaxMagazine());
            }
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        this.gunWeapon.getReloadTask().remove(player);
    }

}
