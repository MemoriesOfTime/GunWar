package cn.lanink.gunwar.tasks.game.blasting;

import cn.lanink.gamecore.GameCore;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityGunWarBomb;
import cn.lanink.gunwar.entity.EntityGunWarBombBlock;
import cn.lanink.gunwar.room.base.IntegralConfig;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
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
    public static final int MAX_PLACEMENT_PROGRESS = 50;

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
        PLANT_BOMB_PLAYERS.add(player);
    }

    @Override
    public void onRun(int i) {
        this.placementProgress += this.base;
        if (i%5 == 0) {
            this.player.sendTip(Tools.getShowStringProgress((int) this.placementProgress, MAX_PLACEMENT_PROGRESS));
        }
        Item item = player.getInventory().getItemInHand();
        if (!item.hasCompoundTag() ||
                item.getNamedTag().getInt("GunWarItemType") != 201 ||
                this.playerPosition.distance(this.player) > 0.5 ||
                this.placementProgress >= MAX_PLACEMENT_PROGRESS ||
                !PLANT_BOMB_PLAYERS.contains(this.player)) {
            this.cancel();
        }
    }

    @Override
    public void onCancel() {
        if (this.placementProgress >= MAX_PLACEMENT_PROGRESS) {
            Tools.sendTitle(this.room, "", this.owner.getLanguage().translateString("game_blasting_plantBomb"));
            this.player.getInventory().remove(Tools.getItem(201));

            CompoundTag nbt = Entity.getDefaultNBT(this.placePoint);
            EntityGunWarBomb entityBomb = new EntityGunWarBomb(
                    this.player.getChunk(), nbt,this.room, this.player);
            entityBomb.setPosition(this.placePoint);
            entityBomb.spawnToAll();
            this.room.setEntityGunWarBomb(entityBomb);

            EntityGunWarBombBlock entityBombBlock = new EntityGunWarBombBlock(
                    this.player.getChunk(), nbt.putCompound("Skin", new CompoundTag()));
            entityBombBlock.setPosition(this.placePoint);
            entityBombBlock.setSkin(GameCore.DEFAULT_SKIN);
            Server.getInstance().getScheduler().scheduleDelayedTask(this.getOwner(), () -> {
                if (!entityBombBlock.isClosed()) {
                    entityBombBlock.spawnToAll();
                }
            }, 10);
            this.room.setEntityGunWarBombBlock(entityBombBlock);

            this.room.addPlayerIntegral(this.player, IntegralConfig.getIntegral(IntegralConfig.IntegralType.BOMB_SCORE));
        }else {
            this.player.sendTitle("", this.owner.getLanguage().translateString("game_blasting_cancelPlantBomb"));
        }
        this.player.sendTip(" ");
        PLANT_BOMB_PLAYERS.remove(this.player);
    }

}
