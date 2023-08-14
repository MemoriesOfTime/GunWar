package cn.lanink.gunwar.tasks.game;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.PlayerGameData;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.room.freeforall.FreeForAllModeRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;


/**
 * 信息显示
 */
public class ScoreBoardTask extends PluginTask<GunWar> {

    private final Language language;
    private final BaseRoom room;

    public ScoreBoardTask(GunWar owner, BaseRoom room) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != IRoomStatus.ROOM_STATUS_GAME) {
            this.cancel();
            return;
        }
        if (!this.room.getPlayerDataMap().isEmpty()) {
            int red = 0;
            int blue = 0;
            if (this.room instanceof FreeForAllModeRoom) {
                red = this.room.getPlayerDataMap().size();
            }else {
                for (PlayerGameData gameData : this.room.getPlayerDataMap().values()) {
                    if (gameData.getTeam() == Team.RED) {
                        red++;
                    } else if (gameData.getTeam() == Team.BLUE) {
                        blue++;
                    }
                }
            }
            for (Player player : this.room.getPlayerDataMap().keySet()) {
                LinkedList<String> ms = new LinkedList<>();
                for (String string : this.language.translateString("gameTimeScoreBoard").split("\n")) {
                    ms.add(string.replace("%gameMode%", Tools.getShowGameMode(this.room.getGameMode()))
                            .replace("%team%", this.room.getPlayerTeamAccurate(player).getShowName())
                            .replace("%health%", String.format("%.1f", room.getPlayerHealth(player)))
                            .replace("%time%", String.valueOf(room.gameTime))
                            .replace("%red%", String.valueOf(red))
                            .replace("%blue%", String.valueOf(blue))
                            .replace("%redRound%", String.valueOf(room.redScore))
                            .replace("%blueRound%", String.valueOf(room.blueScore))
                            .replace("%integral%", String.valueOf(room.getPlayerIntegral(player))));
                }
                owner.getScoreboard().showScoreboard(player, this.language.translateString("scoreBoardTitle"), ms);
            }
        }
    }

}
