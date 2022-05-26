package cn.lanink.gunwar.tasks.game;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.Team;
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
        if (this.room.getPlayers().size() > 0) {
            int red = 0, blue = 0;
            for (Team team : this.room.getPlayers().values()) {
                if (team == Team.RED) {
                    red++;
                }else if (team == Team.BLUE) {
                    blue++;
                }
            }
            for (Player player : this.room.getPlayers().keySet()) {
                LinkedList<String> ms = new LinkedList<>();
                for (String string : this.language.translateString("gameTimeScoreBoard").split("\n")) {
                    ms.add(string.replace("%team%", this.room.getPlayerTeamAccurate(player).getShowName())
                            .replace("%health%", String.format("%.1f", room.getPlayerHealth().getOrDefault(player, 0F)))
                            .replace("%time%", room.gameTime + "")
                            .replace("%red%", red + "")
                            .replace("%blue%", blue + "")
                            .replace("%redRound%", room.redScore + "")
                            .replace("%blueRound%", room.blueScore + ""));
                }
                owner.getScoreboard().showScoreboard(player, this.language.translateString("scoreBoardTitle"), ms);
            }
        }
    }

}
