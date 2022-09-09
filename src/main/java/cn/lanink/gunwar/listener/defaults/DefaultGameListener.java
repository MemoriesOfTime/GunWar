package cn.lanink.gunwar.listener.defaults;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.entity.EntityLongFlag;
import cn.lanink.gunwar.entity.EntityPlayerCorpse;
import cn.lanink.gunwar.event.GunWarPlayerDamageEvent;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.base.BaseItem;
import cn.lanink.gunwar.item.weapon.GunWeapon;
import cn.lanink.gunwar.item.weapon.ProjectileWeapon;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.supplier.pages.SupplyPageConfig;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityRegainHealthEvent;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerChangeSkinEvent;
import cn.nukkit.event.player.PlayerGameModeChangeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;

import java.util.Map;

/**
 * @author LT_Name
 */
@SuppressWarnings("unused")
public class DefaultGameListener extends BaseGameListener<BaseRoom> {

    private final GunWar gunWar = GunWar.getInstance();
    private final Language language = GunWar.getInstance().getLanguage();

    @EventHandler
    public void onPlayerChangeSkin(PlayerChangeSkinEvent event) { //此事件仅玩家主动修改皮肤时触发，不需要针对插件修改特判
        Player player = event.getPlayer();
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * 实体恢复血量事件
     *
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            BaseRoom room = this.getListenerRoom(player.getLevel());
            if (room == null || !room.isPlaying(player)) {
                return;
            }
            if (room.getStatus() == BaseRoom.ROOM_STATUS_GAME &&
                    event.getRegainReason() != EntityRegainHealthEvent.CAUSE_REGEN &&
                    event.getRegainReason() != EntityRegainHealthEvent.CAUSE_EATING) {
                room.addHealth(player, event.getAmount());
            }
            event.setCancelled(true);
        }
    }

    /**
     * 伤害事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            BaseRoom room = this.getListenerRoom(player.getLevel());
            if (room == null || !room.isPlaying(player)) {
                return;
            }
            
            if (room.getStatus() != IRoomStatus.ROOM_STATUS_GAME || room.getPlayerInvincibleTime(player) > 0) {
                event.setCancelled(true);
                return;
            }

            if (player.getHealth() - event.getFinalDamage() < 1) {
                event.setCancelled(true); //阻止死亡
            }
            
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                    event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
                GunWarPlayerDamageEvent ev = new GunWarPlayerDamageEvent(room, player, player, event.getFinalDamage());
                Server.getInstance().getPluginManager().callEvent(ev);
                if (!ev.isCancelled()) {
                    room.lessHealth(player, ev.getDamagePlayer(), ev.getDamage());
                }
            }
        }else if (event.getEntity() instanceof EntityPlayerCorpse ||
                event.getEntity() instanceof EntityFlagStand ||
                event.getEntity() instanceof EntityFlag ||
                event.getEntity() instanceof EntityLongFlag ||
                event.getEntity() instanceof EntityLongFlag.EntityFlagHead) {
            event.setCancelled(true);
        }
    }

    /**
     * 玩家点击事件
     * @param event 事件
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
    
        Block block = event.getBlock();
        switch (block.getId()) {
            case Item.CRAFTING_TABLE:
            case Item.CHEST:
            case Item.ENDER_CHEST:
            case Item.ANVIL:
            case Item.SHULKER_BOX:
            case Item.UNDYED_SHULKER_BOX:
            case Item.FURNACE:
            case Item.BURNING_FURNACE:
            case Item.DISPENSER:
            case Item.DROPPER:
            case Item.HOPPER:
            case Item.BREWING_STAND:
            case Item.CAULDRON:
            case Item.BEACON:
            case Item.FLOWER_POT:
            case Item.JUKEBOX:
                event.setCancelled(true);
                break;
            default:
                break;
        }
        
        if (item == null) {
            return;
        }
        CompoundTag tag = item.getNamedTag();
        if (tag == null) {
            return;
        }

        //商店
        int startTime = room.getSetGameTime() - room.gameTime;
        if (tag.getBoolean(ItemManage.IS_GUN_WAR_ITEM_TAG) && tag.getInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG) == 13 &&
                startTime <= room.getSupplyEnableTime()) {

            //防止win10重复打开商店
            int nowTick = Server.getInstance().getTick();
            int lastTick = tag.getInt("lastTick");
            if (lastTick != 0 && nowTick - lastTick < 20) {
                event.setCancelled(true);
                return;
            }
            tag.putInt("lastTick", nowTick);
            item.setNamedTag(tag);
            player.getInventory().setItemInHand(item);

            SupplyPageConfig defaultPageConfig = room.getSupplyConfig().getDefaultPageConfig();
            if (player.getLoginChainData().getDeviceOS() == 7) { //Win10
                player.addWindow(defaultPageConfig.generateWindow());
            }else {
                player.showFormWindow(defaultPageConfig.generateForm());
            }
            event.setCancelled(true);
            return;
        }

        if (room.getStatus() == IRoomStatus.ROOM_STATUS_WAIT) {
            switch (tag.getInt(ItemManage.GUN_WAR_ITEM_TYPE_TAG)) {
                //退出房间
                case 10:
                    room.quitRoom(player);
                    break;
                //选择红队
                case 11:
                    room.getPlayers().put(player, Team.RED);
                    player.getInventory().setArmorContents(Tools.getArmors(Team.RED));
                    player.sendTitle(this.language.translateString("teamNameRed"),
                            this.language.translateString("playerTeamSelect"),
                            10, 40, 20);
                    break;
                //选择蓝队
                case 12:
                    room.getPlayers().put(player, Team.BLUE);
                    player.getInventory().setArmorContents(Tools.getArmors(Team.BLUE));
                    player.sendTitle(this.language.translateString("teamNameBlue"),
                            this.language.translateString("playerTeamSelect"),
                            10, 40, 20);
                    break;
                default:
                    break;
            }
            if (item.getId() < 1000) { //不阻止自定义物品
                event.setCancelled(true);
            }
        }else if (room.getStatus() == IRoomStatus.ROOM_STATUS_GAME) {
            if (ItemManage.getItemType(tag) == ItemManage.ItemType.WEAPON_GUN) {
                GunWeapon weapon = ItemManage.getGunWeapon(tag);
                if (weapon == null) {
                    return;
                }
                switch (event.getAction()) {
                    case RIGHT_CLICK_AIR:
                        if (ItemManage.canAttack(player, weapon)) {
                            int bullets = weapon.shooting(player, player.getDirectionVector());
                            player.sendTip(Tools.getShowStringMagazine(bullets, weapon.getMaxMagazine()));
                        }
                        break;
                    case LEFT_CLICK_AIR:
                    case LEFT_CLICK_BLOCK:
                    case RIGHT_CLICK_BLOCK:
                        weapon.startReload(player);
                        break;
                    default:
                        break;
                }
                event.setCancelled(true);
            }
        }
    }

    /**
     * 玩家点击背包栏格子事件
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getInventory() == null) {
            return;
        }
        BaseRoom room = this.getListenerRoom(player.getLevel());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (event.getSlot() >= event.getInventory().getSize()) {
            event.setCancelled(true);
            player.sendMessage(this.language.translateString("gameArmor"));
        }else if (room.getStatus() == IRoomStatus.ROOM_STATUS_WAIT) {
            event.setCancelled(true);
        }
    }

    /**
     * 抛射物被发射事件
     * @param event 事件
     */
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        EntityProjectile entity = event.getEntity();
        if (entity == null || !this.getListenerRooms().containsKey(entity.getLevel().getFolderName())) {
            return;
        }
        if (entity.shootingEntity instanceof Player) {
            Player player = (Player) entity.shootingEntity;
            PlayerInventory playerInventory = player.getInventory();
            CompoundTag tag = playerInventory.getItemInHand().getNamedTag();
            if (tag != null) {
                ProjectileWeapon weapon = ItemManage.getProjectileWeapon(tag);
                if (weapon != null) {
                    if (ItemManage.canAttack(player, weapon)) {
                        entity.namedTag.putCompound(BaseItem.GUN_WAR_ITEM_TAG, tag.getCompound(BaseItem.GUN_WAR_ITEM_TAG));
                    }else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * 抛射物击中物体事件
     * @param event 事件
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        EntityProjectile entity = (EntityProjectile) event.getEntity();
        if (entity == null || entity.namedTag == null) {
            return;
        }
        if (entity.shootingEntity instanceof Player) {
            Level level = entity.getLevel();
            BaseRoom room = this.getListenerRoom(level);
            if (room == null || room.getStatus() != IRoomStatus.ROOM_STATUS_GAME) {
                return;
            }
            Player damager = (Player) entity.shootingEntity;
            CompoundTag tag = entity.namedTag.clone();
            Position position = entity.getPosition();
            if (ItemManage.getItemType(tag) == ItemManage.ItemType.WEAPON_PROJECTILE) {
                ProjectileWeapon weapon = ItemManage.getProjectileWeapon(tag);
                if (weapon == null) {
                    return;
                }
                if (weapon.getRange() > 0) {
                    level.addSound(position, Sound.RANDOM_EXPLODE);
                    level.addParticle(weapon.getParticle(position));
                    for (Map.Entry<Player, Team> entry : room.getPlayers().entrySet()) {
                        //跳过已死亡的玩家
                        if (entry.getValue() != Team.RED && entry.getValue() != Team.BLUE) {
                            continue;
                        }

                        double distance = position.distance(entry.getKey());
                        if (GunWar.debug) {
                            gunWar.getLogger().info("[debug] distance:" + distance +
                                    " damager:" + damager.getName() + " player:" + entry.getKey().getName());
                        }
                        if (distance <= weapon.getRange()) {
                            for (Effect effect : weapon.getEffects()) {
                                entry.getKey().addEffect(effect);
                            }
                            float damage = (float) weapon.getDamage(distance);
                            if (damage > 0) {
                                GunWarPlayerDamageEvent ev = new GunWarPlayerDamageEvent(room, entry.getKey(), damager, damage);
                                Server.getInstance().getPluginManager().callEvent(ev);
                                if (!ev.isCancelled()) {
                                    entry.getKey().attack(0F);
                                    room.lessHealth(entry.getKey(),
                                            damager,
                                            ev.getDamage(),
                                            weapon.getKillMessage()
                                                    .replace("%damager%", damager.getName())
                                                    .replace("%player%", entry.getKey().getName())
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 玩家重生事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        for (BaseRoom room : this.getListenerRooms().values()) {
            if (room.isPlaying(player)) {
                switch (room.getPlayerTeamAccurate(player)) {
                    case RED:
                    case RED_DEATH:
                        event.setRespawnPosition(room.getRedSpawn());
                        return;
                    case BLUE:
                    case BLUE_DEATH:
                        event.setRespawnPosition(room.getBlueSpawn());
                        return;
                    default:
                        event.setRespawnPosition(room.getWaitSpawn());
                        return;
                }
            }
        }
    }

    /**
     * 玩家游戏模式改变事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Level level = event.getPlayer() == null ? null : event.getPlayer().getLevel();
        if (level != null && this.getListenerRooms().containsKey(level.getFolderName())) {
            event.setCancelled(false);
        }
    }

}
