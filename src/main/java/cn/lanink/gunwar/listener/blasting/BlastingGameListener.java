package cn.lanink.gunwar.listener.blasting;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityGunWarBombBlock;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.lanink.gunwar.tasks.game.blasting.DemolitionBombTask;
import cn.lanink.gunwar.tasks.game.blasting.PlantBombTask;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.ItemSpawnEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.SetSpawnPositionPacket;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class BlastingGameListener extends BaseGameListener<BlastingModeRoom> {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null || block.getId() == BlockID.AIR) {
            return;
        }
        BlastingModeRoom room = this.getListenerRoom(player.getLevel());
        if (room == null) {
            return;
        }
        Item item = player.getInventory().getItemInHand();
        if (item.hasCompoundTag() && item.getNamedTag().getInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG) == 201 &&
                room.getEntityGunWarBomb() == null) {
            event.setCancelled(true);

            //修复win10玩家连续触发两次导致无法操作的问题
            CompoundTag tag = item.getNamedTag();
            int nowTick = Server.getInstance().getTick();
            int lastTick = item.getNamedTag().getInt("lastTick");
            if (nowTick - lastTick <= 10) {
                return;
            }
            tag.putInt("lastTick", nowTick);
            item.setNamedTag(tag);
            player.getInventory().setItemInHand(item);

            Vector3 placePoint = block.clone();
            placePoint.y += 1;
            if (player.getLevel().getBlock(placePoint).getId() != BlockID.AIR) {
                return;
            }
            if ((player.distance(room.getBlastingPointA()) < room.getBlastingPointRadius() &&
                    placePoint.distance(room.getBlastingPointA()) < room.getBlastingPointRadius()) ||
                    (player.distance(room.getBlastingPointB()) < room.getBlastingPointRadius() &&
                            placePoint.distance(room.getBlastingPointB()) < room.getBlastingPointRadius())) {
                if (PlantBombTask.PLANT_BOMB_PLAYERS.contains(player)) {
                    PlantBombTask.PLANT_BOMB_PLAYERS.remove(player);
                }else {
                    Server.getInstance().getScheduler().scheduleRepeatingTask(GunWar.getInstance(),
                            new PlantBombTask(room, player, placePoint,
                                    PlantBombTask.MAX_PLACEMENT_PROGRESS / (room.getPlantBombTime() * 1D)),
                            1, true);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            //中断安装/拆除炸弹
            PlantBombTask.PLANT_BOMB_PLAYERS.remove(player);
            DemolitionBombTask.DEMOLITION_BOMB_PLAYERS.remove(player);
        }
    }

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof EntityGunWarBombBlock &&
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            Player player = (Player) event.getDamager();
            if (player == null) {
                return;
            }
            BlastingModeRoom room = this.getListenerRoom(player.getLevel());
            if (room == null || !room.isPlaying(player)) {
                return;
            }
            //开始拆除炸弹
            if (room.getPlayerTeamAccurate(player) == Team.BLUE &&
                    room.demolitionBombPlayer == null &&
                    player.distance(room.getEntityGunWarBombBlock()) <= 5) {
                room.demolitionBombPlayer = player;
                Server.getInstance().getScheduler().scheduleRepeatingTask(GunWar.getInstance(),
                        new DemolitionBombTask(room, player,
                                DemolitionBombTask.MAX_DEMOLITION_PROGRESS / (room.getDemolitionBombTime() * 1D)),
                        1, true);
            }
            event.setCancelled(true);
        }
    }

    /**
     * 玩家丢出物品事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null || !item.hasCompoundTag()) {
            return;
        }
        BlastingModeRoom room = this.getListenerRoom(player.getLevel());
        if (room == null) {
            return;
        }
        //覆盖默认规则，允许丢弃炸弹
        if (item.getNamedTag().getBoolean(ItemManage.IS_GUN_WAR_ITEM_TAG) &&
                item.getNamedTag().getInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG) == 201) {
            event.setCancelled(false);
        }
    }

    /**
     * 掉落物生成事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemSpawn(ItemSpawnEvent event) {
        EntityItem entityItem = event.getEntity();
        if (entityItem == null) {
            return;
        }
        BlastingModeRoom room = this.getListenerRoom(entityItem.getLevel());
        if (room == null) {
            return;
        }
        if (Tools.getItem(201).equals(entityItem.getItem())) {
            entityItem.setNameTag("§l§eTNT");
            entityItem.setNameTagAlwaysVisible(true);
            Tools.sendTitle(room, Team.RED, "",
                    GunWar.getInstance().getLanguage().translateString("game_blasting_bombHasFallen"));
            //掉落3秒后给指南针
            Server.getInstance().getScheduler().scheduleDelayedTask(GunWar.getInstance(), () -> {
                if (room.getStatus() != IRoomStatus.ROOM_STATUS_GAME || entityItem.isClosed()) {
                    return;
                }
                SetSpawnPositionPacket pk = new SetSpawnPositionPacket();
                pk.spawnType = SetSpawnPositionPacket.TYPE_WORLD_SPAWN;
                pk.x = entityItem.getFloorX();
                pk.y = entityItem.getFloorY();
                pk.z = entityItem.getFloorZ();
                pk.dimension = 0;
                for (Player player : room.getPlayersAccurate(Team.RED)) {
                    player.dataPacket(pk);
                    player.getInventory().addItem(Tools.getItem(345));
                }
            }, 60);
        }
    }

    /**
     * 拾取掉落物品事件
     * @param event 事件
     */
    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Item item = event.getItem().getItem();
        if (!item.hasCompoundTag()) {
            return;
        }
        Level level = event.getItem().getLevel();
        BlastingModeRoom room = this.getListenerRoom(level);
        if (room == null) {
            return;
        }
        //拾取炸弹
        if (Tools.getItem(201).equals(item)) {
            if (event.getInventory().getHolder() instanceof Player) {
                Player player = (Player) event.getInventory().getHolder();
                if (room.getPlayerTeamAccurate(player) == Team.RED) {
                    Tools.sendTitle(room, Team.RED, "",
                            GunWar.getInstance().getLanguage().translateString("game_blasting_bombHasBeenPickedUp"));
                    for (Player p : room.getPlayersAccurate(Team.RED)) {
                        p.getInventory().removeItem(Tools.getItem(345));
                    }
                    return;
                }
            }
            event.setCancelled(true);
        }
    }


}
