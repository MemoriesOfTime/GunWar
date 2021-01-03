package cn.lanink.gunwar.tasks.game;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.utils.LanguageOld;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;


/**
 * 信息显示
 */
public class ScoreBoardTask extends PluginTask<GunWar> {

    private final LanguageOld languageOld;
    private final BaseRoom room;

    public ScoreBoardTask(GunWar owner, BaseRoom room) {
        super(owner);
        this.languageOld = owner.getLanguageOld();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != IRoomStatus.ROOM_STATUS_GAME) {
            this.cancel();
            return;
        }
        if (this.room.getPlayers().size() > 0) {
            int red = 0, blue = 0;
            for (int team : this.room.getPlayers().values()) {
                if (team == 1) {
                    red++;
                }else if (team == 2) {
                    blue++;
                }
            }
            for (Player player : this.room.getPlayers().keySet()) {
                LinkedList<String> ms = new LinkedList<>();
                String team;
                switch (this.room.getPlayers(player)) {
                    case 1:
                    case 11:
                        team = this.languageOld.teamNameRed;
                        break;
                    default:
                        team = this.languageOld.teamNameBlue;
                        break;
                }
                for (String string : this.languageOld.gameTimeScoreBoard.split("\n")) {
                    ms.add(string.replace("%team%", team)
                            .replace("%health%", String.format("%.1f", room.getPlayerHealth().getOrDefault(player, 0F)))
                            .replace("%time%", room.gameTime + "")
                            .replace("%red%", red + "")
                            .replace("%blue%", blue + "")
                            .replace("%redRound%", room.redScore + "")
                            .replace("%blueRound%", room.blueScore + ""));
                }
                owner.getScoreboard().showScoreboard(player, this.languageOld.scoreBoardTitle, ms);
            }
        }
    }

}
