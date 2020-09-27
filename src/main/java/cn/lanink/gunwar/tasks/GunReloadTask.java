package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

/**
 * @author lt_name
 */
public class GunReloadTask extends PluginTask<GunWar> {

    private final Player player;
    private final GunWeapon gunWeapon;
    private final float base;
    private int initialQuantity;
    private float bulletsFloat;

    public GunReloadTask(GunWar owner, Player player, GunWeapon gunWeapon, float base) {
        super(owner);
        this.player = player;
        this.gunWeapon = gunWeapon;
        this.base = base;
        this.initialQuantity = gunWeapon.getMagazine(player);
        this.bulletsFloat = this.initialQuantity;
    }

    @Override
    public void onRun(int i) {
        if (this.bulletsFloat >= this.gunWeapon.getMaxMagazine() || !this.player.isOnline()) {
            this.cancel();
        }else {
            this.bulletsFloat += this.base;
            this.gunWeapon.getMagazineMap().put(this.player, (int) this.bulletsFloat);
            if (this.gunWeapon.equals(ItemManage.getGunWeapon(this.player.getInventory().getItemInHand()))) {
                this.player.sendTip(Tools.getShowStringMagazine((int) this.bulletsFloat, this.gunWeapon.getMaxMagazine()));
            }else {
                this.cancel();
            }
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        //换弹中被打断
        if (!this.gunWeapon.isReloadInterrupted() && this.bulletsFloat < this.gunWeapon.getMaxMagazine()) {
            this.gunWeapon.getMagazineMap().put(this.player, this.initialQuantity);
            this.player.sendTip(Tools.getShowStringMagazine(this.initialQuantity, this.gunWeapon.getMaxMagazine()));
        }
        this.gunWeapon.getReloadTask().remove(player);
    }

}
