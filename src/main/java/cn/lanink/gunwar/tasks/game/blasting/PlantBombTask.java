package cn.lanink.gunwar.tasks.game.blasting;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;

public class PlantBombTask extends PluginTask<GunWar> {

    private final BlastingModeRoom room;
    private final Player player;
    private final Vector3 placePoint;
    private final double base;
    private double placementProgress;
    public static final int maxPlacementProgress = 50;

    public PlantBombTask(BlastingModeRoom room, Player player, double base) {
        super(GunWar.getInstance());
        this.room = room;
        this.player = player;
        this.placePoint = player.clone();
        this.base = base;
    }

    @Override
    public void onRun(int i) {
        this.placementProgress += this.base;
        if (i%2 == 0) {
            this.player.sendTip(Tools.getShowStringProgress((int) this.placementProgress, maxPlacementProgress));
        }
        Item item = player.getInventory().getItemInHand();
        if (!item.hasCompoundTag() ||
                item.getNamedTag().getInt("GunWarItemType") != 201 ||
                this.placePoint.distance(this.player) > 0.5 ||
                this.placementProgress >= maxPlacementProgress) {
            this.cancel();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if (this.placementProgress >= maxPlacementProgress) {
            Tools.sendTitle(this.room, "", "§c炸弹§e已安装！");
            this.player.getInventory().remove(Tools.getItem(201));
            //TODO
        }else {
            this.player.sendTitle("", "§c取消安装");
        }
    }

}
