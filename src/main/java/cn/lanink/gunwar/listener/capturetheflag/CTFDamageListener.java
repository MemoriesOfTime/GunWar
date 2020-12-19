package cn.lanink.gunwar.listener.capturetheflag;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.listener.base.BaseGameListener;
import cn.lanink.gunwar.room.capturetheflag.CTFModeRoom;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;

/**
 * @author lt_name
 */
public class CTFDamageListener extends BaseGameListener<CTFModeRoom> {

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && !(event.getEntity() instanceof Player)) {
            Player damagePlayer = (Player) event.getDamager();
            if (damagePlayer == null) return;
            CTFModeRoom room = this.getListenerRoom(damagePlayer.getLevel());
            if (room == null || !room.isPlaying(damagePlayer)) {
                return;
            }
            if (event.getEntity() instanceof EntityFlag && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                EntityFlag entityFlag = (EntityFlag) event.getEntity();
                int team = entityFlag.namedTag.getInt("GunWarTeam");
                Language language = GunWar.getInstance().getLanguage();
                if (team == 11 && room.getPlayers(damagePlayer) == 2) {
                    room.haveRedFlag = damagePlayer;
                    Tools.sendTitle(room, "", language.game_ctf_playerPickUpTheFlag
                            .replace("%player%", damagePlayer.getName())
                            .replace("%team%", language.teamNameRed));
                }else if (team == 12 && room.getPlayers(damagePlayer) == 1) {
                    room.haveBlueFlag = damagePlayer;
                    Tools.sendTitle(room, "", language.game_ctf_playerPickUpTheFlag
                            .replace("%player%", damagePlayer.getName())
                            .replace("%team%", language.teamNameBlue));
                }
            }else if (event.getEntity() instanceof EntityFlagStand && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                EntityFlagStand entityFlagStand = (EntityFlagStand) event.getEntity();
                int team = entityFlagStand.namedTag.getInt("GunWarTeam");
                if (team == room.getPlayers(damagePlayer)) {
                    switch (team) {
                        case 1:
                            if (room.haveBlueFlag == damagePlayer) {
                                room.blueFlag.teleport(new Vector3(room.getBlueSpawn().getX(),
                                        room.getBlueSpawn().getY() + 0.3D,
                                        room.getBlueSpawn().getZ()));
                                room.redScore++;
                                room.haveBlueFlag = null;
                                Tools.addSound(room, Sound.RANDOM_LEVELUP);
                            }
                            break;
                        case 2:
                            if (room.haveRedFlag == damagePlayer) {
                                room.redFlag.teleport(new Vector3(room.getRedSpawn().getX(),
                                        room.getRedSpawn().getY() + 0.3D,
                                        room.getRedSpawn().getZ()));
                                room.blueScore++;
                                room.haveRedFlag = null;
                                Tools.addSound(room, Sound.RANDOM_LEVELUP);
                            }
                            break;
                    }
                }
            }
            event.setCancelled(true);
        }
    }

}
