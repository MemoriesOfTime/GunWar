package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.event.GunWarPlayerDamageEvent;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.entity.projectile.EntityEgg;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.HugeExplodeSeedParticle;
import cn.nukkit.level.particle.SpellParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;

import java.util.Map;

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
        if (event.getDamager() instanceof Player) {
            Player damagePlayer = (Player) event.getDamager();
            if (damagePlayer == null) return;
            Room room = this.gunWar.getRooms().getOrDefault(damagePlayer.getLevel().getName(), null);
            if (room == null || !room.isPlaying(damagePlayer)) return;
            if (event.getEntity() instanceof EntityFlag) {
                EntityFlag entityFlag = (EntityFlag) event.getEntity();
                //TODO 分开底座和旗帜
                int team = entityFlag.namedTag.getInt("GunWarTeam");
                if (team != room.getPlayerMode(damagePlayer)) {
                    Skin skin;
                    EntityFlag entityFlag1;
                    if (team == 1) {
                        entityFlag.namedTag.putInt("GunWarTeam", 11);
                        room.haveRedFlag = damagePlayer;
                        skin = this.gunWar.getFlagSkin(11);
                    }else if (team == 2) {
                        entityFlag.namedTag.putInt("GunWarTeam", 12);
                        room.haveBlueFlag = damagePlayer;
                        skin = this.gunWar.getFlagSkin(12);
                    }else {
                        event.setCancelled(true);
                        return;
                    }
                    /*entityFlag.setSkin(this.gunWar.getFlagSkin(0));
                    Tools.setPlayerSkin(entityFlag, this.gunWar.getFlagSkin(0));*/
                    CompoundTag nbt = EntityFlag.getDefaultNBT(damagePlayer);
                    nbt.putFloat("Scale", 1.0F);
                    nbt.putCompound("Skin", new CompoundTag()
                            .putByteArray("Data", skin.getSkinData().data)
                            .putString("ModelId", skin.getSkinId()));
                    entityFlag1 = new EntityFlag(damagePlayer.getChunk(), nbt);
                    entityFlag1.setSkin(skin);
                    entityFlag1.spawnToAll();
                    if (team == 1) {
                        room.redFlag = entityFlag1;
                    }else {
                        room.blueFlag = entityFlag1;
                    }
                    damagePlayer.getLevel().addSound(damagePlayer, Sound.RANDOM_ORB);
                }
            }else if ((event.getEntity() instanceof Player)) {
                Player player = (Player) event.getEntity();
                if (room.getMode() == 2 && room.getPlayerMode(damagePlayer) != room.getPlayerMode(player)) {
                    if (event instanceof EntityDamageByChildEntityEvent) {
                        int id = ((EntityDamageByChildEntityEvent) event).getChild().getNetworkId();
                        if (id == 80) {
                            Server.getInstance().getPluginManager().callEvent(
                                    new GunWarPlayerDamageEvent(room, player, damagePlayer, 10F));
                            return;
                        } else if (id == 81) {
                            Server.getInstance().getPluginManager().callEvent(
                                    new GunWarPlayerDamageEvent(room, player, damagePlayer, 2F));
                            return;
                        }
                    }else {
                        int id = damagePlayer.getInventory().getItemInHand() == null ? 0 : damagePlayer.getInventory().getItemInHand().getId();
                        if (id == 272 && !room.swordAttackCD.contains(player)) {
                            room.swordAttackCD.add(player);
                            this.gunWar.getServer().getPluginManager().callEvent(
                                    new GunWarPlayerDamageEvent(room, player, damagePlayer, 2F));
                            this.gunWar.getServer().getScheduler().scheduleDelayedTask(this.gunWar, new Task() {
                                @Override
                                public void onRun(int i) {
                                    while (room.swordAttackCD.contains(player)) {
                                        room.swordAttackCD.remove(player);
                                    }
                                }
                            }, 20);
                            return;
                        }
                    }
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
            switch (tag.getInt("GunWarItemType")) {
                case 10:
                    room.quitRoom(player, true);
                    break;
                case 11:
                    room.getPlayers().put(player, 1);
                    player.getInventory().setArmorContents(Tools.getArmors(1));
                    player.sendTitle(this.language.teamNameRed, this.language.playerTeamSelect, 10, 40, 20);
                    break;
                case 12:
                    room.getPlayers().put(player, 2);
                    player.getInventory().setArmorContents(Tools.getArmors(2));
                    player.sendTitle(this.language.teamNameBlue, this.language.playerTeamSelect, 10, 40, 20);
                    break;
            }
            event.setCancelled(true);
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
        if (event.getMessage().startsWith(this.gunWar.getCmdUser(), 1) ||
                event.getMessage().startsWith(this.gunWar.getCmdAdmin(), 1)) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(this.language.useCmdInRoom);
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
                    for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                        if (entry.getValue() != 1 && entry.getValue() != 2) {
                            continue;
                        }
                        int x = Math.abs(entry.getKey().getFloorX() - entity.getFloorX());
                        int y = Math.abs(entry.getKey().getFloorY() - entity.getFloorY());
                        int z = Math.abs(entry.getKey().getFloorZ() - entity.getFloorZ());
                        if (x > 5 && y > 5 && z > 5) {
                            break;
                        }
                        for (int r = 1; r <= 5; r++) {
                            if (x <= r && y <= r && z <= r) {
                                switch (entity.namedTag.getInt("GunWarItemType")) {
                                    case 4:
                                        entry.getKey().attack(0F);
                                        float damage = 12F - (r * 2);
                                        Server.getInstance().getPluginManager().callEvent(
                                                new GunWarPlayerDamageEvent(room, entry.getKey(), (Player) entity.shootingEntity, damage));
                                        break;
                                    case 5:
                                        Effect effect = Effect.getEffect(15);
                                        int tick = 90 - (r * 10);
                                        effect.setDuration(tick);
                                        entry.getKey().addEffect(effect);
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

    /**
     * 玩家重生事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        for (Room room : this.gunWar.getRooms().values()) {
            if (room.isPlaying(player)) {
                switch (room.getPlayerMode(player)) {
                    case 1:
                    case 11:
                        event.setRespawnPosition(room.getRedSpawn());
                        break;
                    case 2:
                    case 12:
                        event.setRespawnPosition(room.getBlueSpawn());
                        break;
                    default:
                        event.setRespawnPosition(room.getWaitSpawn());
                        break;
                }
                break;
            }
        }
    }

}
