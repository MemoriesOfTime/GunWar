package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.PluginTask;
import tip.messages.ScoreBoardMessage;
import tip.utils.Api;

import java.util.LinkedList;


/**
 * 信息显示
 */
public class ScoreBoardTask extends PluginTask<GunWar> {

    private final String taskName = "ScoreBoardTask";
    private final Language language;
    private final Room room;

    public ScoreBoardTask(GunWar owner, Room room) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
        }
        if (!this.room.task.contains(this.taskName)) {
            this.room.task.add(this.taskName);
            owner.getServer().getScheduler().scheduleAsyncTask(GunWar.getInstance(), new AsyncTask() {
                @Override
                public void onRun() {
                    if (room.getPlayers().values().size() > 0) {
                        if (room.getMode() == 2) {
                            int red = 0, blue = 0;
                            for (int team : room.getPlayers().values()) {
                                if (team == 1) {
                                    red++;
                                }else if (team == 2) {
                                    blue++;
                                }
                            }
                            for (Player player : room.getPlayers().keySet()) {
                                ScoreBoardMessage score = new ScoreBoardMessage(
                                        room.getLevel().getName(), true, "§eGunWar", new LinkedList<>());
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
                                            .replace("%redRound%", room.redRound + "")
                                            .replace("%blueRound%", room.blueRound + ""));
                                }
                                score.setMessages(ms);
                                Api.setPlayerShowMessage(player.getName(), score);
                            }
                        }
                    }
                    room.task.remove(taskName);
                }
            });
        }
    }

}
