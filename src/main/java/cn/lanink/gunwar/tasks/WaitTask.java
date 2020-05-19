package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarRoomStartEvent;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.PluginTask;
import tip.messages.ScoreBoardMessage;
import tip.messages.TipMessage;
import tip.utils.Api;

import java.util.LinkedList;
import java.util.Map;


public class WaitTask extends PluginTask<GunWar> {

    private final String taskName = "WaitTask";
    private final Language language;
    private final Room room;
    private final boolean bottom, scoreBoard;
    private final TipMessage tipMessage;

    public WaitTask(GunWar owner, Room room) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
        this.bottom = owner.getConfig().getBoolean("底部显示信息", true);
        this.scoreBoard = owner.getConfig().getBoolean("计分板显示信息", true);
        this.tipMessage = new TipMessage(room.getLevel().getName(), true, 0, null);
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 1) {
            this.cancel();
        }
        if (this.room.getPlayers().size() > 1) {
            if (this.room.waitTime > 0) {
                this.room.waitTime--;
                if (this.room.waitTime <= 5) {
                    Tools.addSound(this.room, Sound.RANDOM_CLICK);
                }
                if (!this.room.task.contains(this.taskName)) {
                    this.room.task.add(this.taskName);
                    owner.getServer().getScheduler().scheduleAsyncTask(owner, new AsyncTask() {
                        @Override
                        public void onRun() {
                            tipMessage.setMessage(language.waitTimeBottom
                                    .replace("%playerNumber%", room.getPlayers().size() + "")
                                    .replace("%time%", room.waitTime + ""));
                            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                                if (bottom) {
                                    Api.setPlayerShowMessage(entry.getKey().getName(), tipMessage);
                                }
                                if (scoreBoard) {
                                    String team;
                                    switch (entry.getValue()) {
                                        case 1:
                                        case 11:
                                            team = language.teamNameRed;
                                            break;
                                        case 2:
                                        case 12:
                                            team = language.teamNameBlue;
                                            break;
                                        default:
                                            team = language.noTeamSelect;
                                            break;
                                    }
                                    LinkedList<String> ms = new LinkedList<>();
                                    for (String string : language.waitTimeScoreBoard.split("\n")) {
                                        ms.add(string.replace("%team%", team)
                                                .replace("%playerNumber%", room.getPlayers().size() + "")
                                                .replace("%time%", room.waitTime + ""));
                                    }
                                    ScoreBoardMessage score = new ScoreBoardMessage(
                                            room.getLevel().getName(), true, language.scoreBoardTitle, ms);
                                    Api.setPlayerShowMessage(entry.getKey().getName(), score);
                                }
                            }
                            room.task.remove(taskName);
                        }
                    });
                }
            }else {
                owner.getServer().getPluginManager().callEvent(new GunWarRoomStartEvent(this.room));
                this.cancel();
            }
        }else if (this.room.getPlayers().size() > 0) {
            if (this.room.waitTime != this.room.getWaitTime()) {
                this.room.waitTime = this.room.getWaitTime();
            }
            if (!this.room.task.contains(this.taskName)) {
                this.room.task.add(this.taskName);
                owner.getServer().getScheduler().scheduleAsyncTask(owner, new AsyncTask() {
                    @Override
                    public void onRun() {
                        tipMessage.setMessage(language.waitBottom
                                .replace("%playerNumber%", room.getPlayers().size() + ""));
                        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                            if (bottom) {
                                Api.setPlayerShowMessage(entry.getKey().getName(), tipMessage);
                            }
                            if (scoreBoard) {
                                String team;
                                switch (entry.getValue()) {
                                    case 1:
                                    case 11:
                                        team = language.teamNameRed;
                                        break;
                                    case 2:
                                    case 12:
                                        team = language.teamNameBlue;
                                        break;
                                    default:
                                        team = language.noTeamSelect;
                                        break;
                                }
                                LinkedList<String> ms = new LinkedList<>();
                                for (String string : language.waitScoreBoard.split("\n")) {
                                    ms.add(string.replace("%team%", team)
                                            .replace("%playerNumber%", room.getPlayers().size() + ""));
                                }
                                ScoreBoardMessage score = new ScoreBoardMessage(
                                        room.getLevel().getName(), true, language.scoreBoardTitle, ms);
                                Api.setPlayerShowMessage(entry.getKey().getName(), score);
                            }
                        }
                        room.task.remove(taskName);
                    }
                });
            }
        }else {
            this.room.endGame();
            this.cancel();
        }
    }

}
