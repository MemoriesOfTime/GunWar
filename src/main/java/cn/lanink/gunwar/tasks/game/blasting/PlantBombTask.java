package cn.lanink.gunwar.tasks.game.blasting;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityGunWarBomb;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.PluginTask;

import java.util.HashSet;

public class PlantBombTask extends PluginTask<GunWar> {

    public static final HashSet<Player> PLANT_BOMB_PLAYERS = new HashSet<>();
    private final BlastingModeRoom room;
    private final Player player;
    private final Vector3 playerPosition;
    private final Vector3 placePoint;
    private final double base;
    private double placementProgress;
    public static final int maxPlacementProgress = 50;

    public PlantBombTask(BlastingModeRoom room, Player player, Vector3 placePoint, double base) {
        super(GunWar.getInstance());
        this.room = room;
        this.player = player;
        this.playerPosition = player.clone();
        this.placePoint = placePoint;
        this.placePoint.y += 1;
        this.placePoint.x += 0.5;
        this.placePoint.z += 0.5;
        this.base = base;
        if (PLANT_BOMB_PLAYERS.contains(player)) {
            this.cancel();
            return;
        }
        PLANT_BOMB_PLAYERS.add(player);
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
                this.playerPosition.distance(this.player) > 0.5 ||
                this.placementProgress >= maxPlacementProgress) {
            this.cancel();
        }
    }

    @Override
    public void cancel() {
        if (this.placementProgress >= maxPlacementProgress) {
            Tools.sendTitle(this.room, "", "§c炸弹§e已安装！");
            this.player.getInventory().remove(Tools.getItem(201));
            //TODO
            EntityGunWarBomb entityBomb = new EntityGunWarBomb(this.player.getChunk(), Entity.getDefaultNBT(this.placePoint), this.player);
            entityBomb.setPosition(this.placePoint);
            entityBomb.spawnToAll();
        }else {
            this.player.sendTitle("", "§c取消安装");
        }
        this.player.sendTip(" ");
        PLANT_BOMB_PLAYERS.remove(this.player);
        super.cancel();
    }

}
