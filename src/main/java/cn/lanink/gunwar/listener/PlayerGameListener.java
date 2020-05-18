package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarPlayerDamageEvent;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.projectile.EntityEgg;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.HugeExplodeSeedParticle;
import cn.nukkit.level.particle.SpellParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.AsyncTask;

public class PlayerGameListener implements Listener {

    private final GunWar gunWar;
    private final Language language;

    public PlayerGameListener(GunWar gunWar) {
        this.gunWar = gunWar;
        this.language = gunWar.getLanguage();
    }

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getCause() == null || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damagePlayer = (Player) event.getDamager();
            Player player = (Player) event.getEntity();
            if (damagePlayer == null || player == null) {
                return;
            }
            Room room = this.gunWar.getRooms().getOrDefault(damagePlayer.getLevel().getName(), null);
            if (room == null || !room.isPlaying(damagePlayer) || !room.isPlaying(player)) {
                return;
            }
            if (room.getMode() == 2) {
                if (room.isPlaying(damagePlayer) && room.isPlaying(player) &&
                        room.getPlayerMode(damagePlayer) != room.getPlayerMode(player)) {
                    int id = damagePlayer.getInventory().getItemInHand() == null ? 0 : damagePlayer.getInventory().getItemInHand().getId();
                    if (id == 272) {
                        Server.getInstance().getPluginManager().callEvent(
                                new GunWarPlayerDamageEvent(room, player, damagePlayer, 2F));
                        return;
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    /**
     * 实体受到另一个子实体伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onDamageByChild(EntityDamageByChildEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damagePlayer = ((Player) event.getDamager()).getPlayer();
            Player player = ((Player) event.getEntity()).getPlayer();
            if (damagePlayer == player || event.getChild() == null) {
                return;
            }
            Room room = this.gunWar.getRooms().getOrDefault(damagePlayer.getLevel().getName(), null);
            if (room == null || !room.isPlaying(damagePlayer) || !room.isPlaying(player)) {
                return;
            }
            if (room.getMode() == 2 && room.getPlayerMode(damagePlayer) != room.getPlayerMode(player)) {
                int id = event.getChild().getNetworkId();
                if (id == 80) {
                    Server.getInstance().getPluginManager().callEvent(
                            new GunWarPlayerDamageEvent(room, player, damagePlayer, 10F));
                    return;
                } else if (id == 81) {
                    Server.getInstance().getPluginManager().callEvent(
                            new GunWarPlayerDamageEvent(room, player, damagePlayer, 1F));
                    return;
                }
            }
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
        if (player == null || item == null) {
            return;
        }
        Room room = this.gunWar.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            player.setAllowModifyWorld(false);
        }
        CompoundTag tag = item.getNamedTag();
        if (tag == null || !tag.getBoolean("isGunWarItem")) return;
        if (room.getMode() == 1) {
            if (tag.getInt("GunWarItemType") == 10) {
                event.setCancelled(true);
                room.quitRoom(player, true);
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
        Room room = this.gunWar.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        int size = event.getInventory().getSize();
        if (event.getSlot() >= size) {
            event.setCancelled(true);
            player.sendMessage(this.language.gameArmor);
        }
    }

    /**
     * 玩家执行命令事件
     * @param event 事件
     */
    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getMessage() == null) return;
        Room room = this.gunWar.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (!event.getMessage().startsWith(GunWar.CMD_USER, 1) ||
                !event.getMessage().startsWith(GunWar.CMD_ADMIN, 1)) {
            event.setCancelled(true);
            player.sendMessage(this.language.useCmdInRoom);
        }
    }

    /**
     * 抛射物被发射事件
     * @param event 事件
     */
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        EntityProjectile entity = event.getEntity();
        if (entity == null || !this.gunWar.getRooms().containsKey(entity.getLevel().getName())) {
            return;
        }
        if (entity.shootingEntity instanceof Player) {
            PlayerInventory playerInventory = ((Player) entity.shootingEntity).getInventory();
            if (playerInventory != null) {
                CompoundTag tag = playerInventory.getItemInHand() != null ? playerInventory.getItemInHand().getNamedTag() : null;
                if (tag != null && tag.getBoolean("isGunWarItem")) {
                    entity.namedTag.putBoolean("isGunWarItem", true);
                    entity.namedTag.putInt("GunWarItemType", tag.getInt("GunWarItemType"));
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
        if (entity == null || entity.namedTag == null || !entity.namedTag.getBoolean("isGunWarItem")) return;
        if (entity instanceof EntityEgg && entity.shootingEntity instanceof Player) {
            Level level = entity.getLevel();
            Room room = this.gunWar.getRooms().getOrDefault(level.getName(), null);
            if (room == null || room.getMode() != 2) {
                return;
            }
            this.gunWar.getServer().getScheduler().scheduleAsyncTask(this.gunWar, new AsyncTask() {
                @Override
                public void onRun() {
                    level.addSound(entity, Sound.RANDOM_EXPLODE);
                    switch (entity.namedTag.getInt("GunWarItemType")) {
                        case 4:
                            level.addParticle(new HugeExplodeSeedParticle(entity));
                            break;
                        case 5:
                            level.addParticle(new SpellParticle(entity, 255, 255, 255));
                            break;
                    }
                    for (Player player : room.getPlayers().keySet()) {
                        int x, y, z;
                        if (player.getFloorX() > entity.getFloorX()) {
                            x = player.getFloorX() - entity.getFloorX();
                        }else {
                            x = entity.getFloorX() - player.getFloorX();
                        }
                        if (player.getFloorY() > entity.getFloorY()) {
                            y = player.getFloorY() - entity.getFloorY();
                        }else {
                            y = entity.getFloorY() - player.getFloorY();
                        }
                        if (player.getFloorZ() > entity.getFloorZ()) {
                            z = player.getFloorZ() - entity.getFloorZ();
                        }else {
                            z = entity.getFloorZ() - player.getFloorZ();
                        }
                        if (x > 5 && y > 5 && z > 5) {
                            break;
                        }
                        for (int r = 1; r <= 5; r++) {
                            if (x <= r && y <= r && z <= r) {
                                switch (entity.namedTag.getInt("GunWarItemType")) {
                                    case 4:
                                        player.attack(0F);
                                        float damage = 12F - (r * 2);
                                        Server.getInstance().getPluginManager().callEvent(
                                                new GunWarPlayerDamageEvent(room, player, (Player) entity.shootingEntity, damage));
                                        break;
                                    case 5:
                                        Effect effect = Effect.getEffect(15);
                                        int tick = 90 - (r * 10);
                                        effect.setDuration(tick);
                                        player.addEffect(effect);
                                        break;
                                }
                                break;
                            }
                        }
                    }
                }
            });

        }
    }

}
