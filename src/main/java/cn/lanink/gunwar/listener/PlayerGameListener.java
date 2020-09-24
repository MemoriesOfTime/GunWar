package cn.lanink.gunwar.listener;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.ItemManage;
import cn.lanink.gunwar.item.base.BaseItem;
import cn.lanink.gunwar.item.weapon.ProjectileWeapon;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.projectile.EntityEgg;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.Map;

public class PlayerGameListener implements Listener {

    private final GunWar gunWar;
    private final Language language;

    public PlayerGameListener(GunWar gunWar) {
        this.gunWar = gunWar;
        this.language = gunWar.getLanguage();
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
        if (room.getStatus() == 1) {
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
        if (event.getSlot() >= event.getInventory().getSize()) {
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
        Room room = this.gunWar.getRooms().get(player.getLevel().getName());
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player == null || message == null) return;
        Room room = this.gunWar.getRooms().get(player.getLevel().getName());
        if (room == null || !room.isPlaying(player) || room.getStatus() != 2) {
            return;
        }
        message = this.language.playerTeamChat.replace("%player%", player.getName())
                .replace("%message%", message);
        int team = room.getPlayerMode(player);
        for (Player p : room.getPlayers().keySet()) {
            if (room.getPlayerMode(p) == team ||
                    (room.getPlayerMode(p) - 10 == team) ||
                    (room.getPlayerMode(p) == team - 10)) {
                p.sendMessage(message);
            }
        }
        event.setMessage("");
        event.setCancelled(true);
    }

    /**
     * 抛射物被发射事件
     * @param event 事件
     */
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        EntityProjectile entity = event.getEntity();
        if (entity == null || !this.gunWar.getRooms().containsKey(entity.getLevel().getFolderName())) {
            return;
        }
        if (entity.shootingEntity instanceof Player) {
            PlayerInventory playerInventory = ((Player) entity.shootingEntity).getInventory();
            if (playerInventory != null) {
                CompoundTag tag = playerInventory.getItemInHand() != null ? playerInventory.getItemInHand().getNamedTag() : null;
                if (tag != null) {
                    entity.namedTag.putCompound(BaseItem.GUN_WAR_ITEM_TAG, tag.getCompound(BaseItem.GUN_WAR_ITEM_TAG));
                }
                /*if (tag != null && tag.getBoolean("isGunWarItem")) {
                    entity.namedTag.putBoolean("isGunWarItem", true);
                    entity.namedTag.putInt("GunWarItemType", tag.getInt("GunWarItemType"));
                }*/
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
        if (entity == null || entity.namedTag == null) return;
        if (entity instanceof EntityEgg && entity.shootingEntity instanceof Player) {
            Level level = entity.getLevel();
            Room room = this.gunWar.getRooms().get(level.getFolderName());
            if (room == null || room.getStatus() != 2) {
                return;
            }
            Player damager = (Player) entity.shootingEntity;
            CompoundTag tag = entity.namedTag.clone();
            Position position = entity.getPosition();
            if (ItemManage.getItemType(tag) == ItemManage.ItemType.PROJECTILE_WEAPON) {
                ProjectileWeapon weapon = ItemManage.getProjectileWeapon(tag);
                if (weapon.getRange() > 0) {
                    level.addSound(position, Sound.RANDOM_EXPLODE);
                    level.addParticle(weapon.getParticle(position));
                    for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                        if (entry.getValue() != 1 && entry.getValue() != 2) {
                            continue;
                        }
                        if (GunWar.debug) {
                            gunWar.getLogger().info("[debug] distance:" + position.distance(entry.getKey()) +
                                    " damager:" + damager.getName() + " player:" + entry.getKey().getName());
                        }
                        float damage = (float) weapon.getDamage(position.distance(entry.getKey()));
                        if (damage > 0) {
                            entry.getKey().attack(0F);
                            if (room.lessHealth(entry.getKey(), damager, damage) < 1) {
                                Tools.sendMessage(room, weapon.getKillMessage()
                                        .replace("%damager%", damager.getName())
                                        .replace("%player%", entry.getKey().getName()));
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
        for (Room room : this.gunWar.getRooms().values()) {
            if (room.isPlaying(player)) {
                switch (room.getPlayerMode(player)) {
                    case 1:
                    case 11:
                        event.setRespawnPosition(room.getRedSpawn());
                        return;
                    case 2:
                    case 12:
                        event.setRespawnPosition(room.getBlueSpawn());
                        return;
                    default:
                        event.setRespawnPosition(room.getWaitSpawn());
                        return;
                }
            }
        }
    }

}
