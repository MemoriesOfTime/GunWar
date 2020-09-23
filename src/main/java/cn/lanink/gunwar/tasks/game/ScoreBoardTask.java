package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;


/**
 * 信息显示
 */
public class ScoreBoardTask extends PluginTask<GunWar> {

    private final Language language;
    private final Room room;

    public ScoreBoardTask(GunWar owner, Room room) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != 2) {
            this.cancel();
            return;
        }
        if (room.getPlayers().size() > 0) {
            int red = 0, blue = 0;
            for (int team : room.getPlayers().values()) {
                if (team == 1) {
                    red++;
                }else if (team == 2) {
                    blue++;
                }
            }
            for (Player player : room.getPlayers().keySet()) {
                LinkedList<String> ms = new LinkedList<>();
                String team;
                switch (room.getPlayerMode(player)) {
                    case 1:
                    case 11:
                        team = language.teamNameRed;
                        break;
                    default:
                        team = language.teamNameBlue;
                        break;
                }
                for (String string : language.gameTimeScoreBoard.split("\n")) {
                    ms.add(string.replace("%team%", team)
                            .replace("%health%", room.getPlayerHealth().getOrDefault(player, 0F) + "")
                            .replace("%time%", room.gameTime + "")
                            .replace("%red%", red + "")
                            .replace("%blue%", blue + "")
                            .replace("%redRound%", room.redScore + "")
                            .replace("%blueRound%", room.blueScore + ""));
                }
                owner.getScoreboard().showScoreboard(player, this.language.scoreBoardTitle, ms);
            }
        }
    }

}
