package cn.lanink.gunwar.listener.capturetheflag;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.entity.EntityFlagStand;
import cn.lanink.gunwar.room.base.IntegralConfig;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.room.capturetheflag.CTFModeRoom;
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
            if (damagePlayer == null) {
                return;
            }
            CTFModeRoom room = this.getListenerRoom(damagePlayer.getLevel());
            if (room == null || !room.isPlaying(damagePlayer)) {
                return;
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                    event.getEntity().distance(damagePlayer) < 3) {
                if (event.getEntity() instanceof EntityFlag) {
                    EntityFlag entityFlag = (EntityFlag) event.getEntity();
                    int team = entityFlag.namedTag.getInt("GunWarTeam");
                    Language language = GunWar.getInstance().getLanguage();
                    if (team == 11 && room.getPlayerTeamAccurate(damagePlayer) == Team.BLUE) {
                        room.haveRedFlag = damagePlayer;
                        Tools.sendTitle(room, "",
                                language.translateString("game_ctf_playerPickUpTheFlag",
                                        damagePlayer.getName(), language.translateString("teamNameRed")));
                    } else if (team == 12 && room.getPlayerTeamAccurate(damagePlayer) == Team.RED) {
                        room.haveBlueFlag = damagePlayer;
                        Tools.sendTitle(room, "",
                                language.translateString("game_ctf_playerPickUpTheFlag",
                                        damagePlayer.getName(), language.translateString("teamNameBlue")));
                    }
                } else if (event.getEntity() instanceof EntityFlagStand) {
                    EntityFlagStand entityFlagStand = (EntityFlagStand) event.getEntity();
                    int team = entityFlagStand.namedTag.getInt("GunWarTeam");
                    if ((team == 1 && room.getPlayerTeamAccurate(damagePlayer) == Team.RED) ||
                            (team == 2 && room.getPlayerTeamAccurate(damagePlayer) == Team.BLUE)) {
                        switch (team) {
                            case 1:
                                if (room.haveBlueFlag == damagePlayer) {
                                    room.blueFlag.teleport(new Vector3(room.getBlueSpawn().getX(),
                                            room.getBlueSpawn().getY() + 0.3D,
                                            room.getBlueSpawn().getZ()));
                                    room.redScore++;
                                    room.haveBlueFlag = null;
                                    Tools.playSound(room, Sound.RANDOM_LEVELUP);
                                    room.addPlayerIntegral(damagePlayer, IntegralConfig.getIntegral(IntegralConfig.IntegralType.FLAG_GET_SCORE));
                                }
                                break;
                            case 2:
                                if (room.haveRedFlag == damagePlayer) {
                                    room.redFlag.teleport(new Vector3(room.getRedSpawn().getX(),
                                            room.getRedSpawn().getY() + 0.3D,
                                            room.getRedSpawn().getZ()));
                                    room.blueScore++;
                                    room.haveRedFlag = null;
                                    Tools.playSound(room, Sound.RANDOM_LEVELUP);
                                    room.addPlayerIntegral(damagePlayer, IntegralConfig.getIntegral(IntegralConfig.IntegralType.FLAG_GET_SCORE));
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            event.setCancelled(true);
        }
    }

}
