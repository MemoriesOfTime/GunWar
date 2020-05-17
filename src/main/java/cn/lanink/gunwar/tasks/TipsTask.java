package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.PluginTask;
import tip.messages.ScoreBoardMessage;
import tip.messages.TipMessage;
import tip.utils.Api;

import java.util.LinkedList;


/**
 * 信息显示
 */
public class TipsTask extends PluginTask<GunWar> {

    private final Language language = GunWar.getInstance().getLanguage();
    private final String taskName = "TipsTask";
    private final Room room;
    private final boolean bottom, scoreBoard;
    private final TipMessage tipMessage;
    private final ScoreBoardMessage scoreBoardMessage;

    public TipsTask(GunWar owner, Room room) {
        super(owner);
        this.room = room;
        this.bottom = owner.getConfig().getBoolean("底部显示信息", true);
        this.scoreBoard = owner.getConfig().getBoolean("计分板显示信息", true);
        this.tipMessage = new TipMessage(room.getLevel().getName(), true, 0, null);
        this.scoreBoardMessage = new ScoreBoardMessage(
                room.getLevel().getName(), true, this.language.scoreBoardTitle, new LinkedList<>());
    }

    @Override
    public void onRun(int i) {
       if (this.room.getMode() == 0) {
            if (this.room.getPlayers().values().size() > 0) {
                this.room.getPlayers().keySet().forEach(player -> {
                            Api.removePlayerShowMessage(player.getName(), this.scoreBoardMessage);
                            Api.removePlayerShowMessage(player.getName(), this.tipMessage);
                        });
            }
            this.cancel();
        }
        if (!this.room.task.contains(this.taskName)) {
            this.room.task.add(this.taskName);
            owner.getServer().getScheduler().scheduleAsyncTask(GunWar.getInstance(), new AsyncTask() {
                @Override
                public void onRun() {
                    if (room.getPlayers().values().size() > 0) {
                        if (room.getMode() == 1) {
                            if (room.getPlayers().values().size() > 1) {
                                tipMessage.setMessage(language.waitTimeBottom
                                        .replace("%playerNumber%", room.getPlayers().size() + "")
                                        .replace("%time%", room.waitTime + ""));
                                LinkedList<String> ms = new LinkedList<>();
                                for (String string : language.waitTimeScoreBoard.split("\n")) {
                                    ms.add(string.replace("%playerNumber%", room.getPlayers().size() + "")
                                            .replace("%time%", room.waitTime + ""));
                                }
                                scoreBoardMessage.setMessages(ms);
                            }else {
                                tipMessage.setMessage(language.waitBottom
                                        .replace("%playerNumber%", room.getPlayers().size() + ""));
                                LinkedList<String> ms = new LinkedList<>();
                                for (String string : language.waitScoreBoard.split("\n")) {
                                    ms.add(string.replace("%playerNumber%", room.getPlayers().size() + ""));
                                }
                                scoreBoardMessage.setMessages(ms);
                            }
                            this.sendMessage();
                        }else if (room.getMode() == 2) {
                            int red = 0, blue = 0;
                            for (int team : room.getPlayers().values()) {
                                if (team == 1) {
                                    red++;
                                }else if (team == 2) {
                                    blue++;
                                }
                            }
                            for (Player player : room.getPlayers().keySet()) {
                                if (bottom) {
                                    TipMessage tip = new TipMessage(room.getLevel().getName(), true, 0, null);
                                    tip.setMessage(language.gameTimeBottom
                                            .replace("%health%", this.getStringHealth(room.getPlayerHealth().getOrDefault(player, 0F))));
                                    Api.setPlayerShowMessage(player.getName(), tip);
                                }
                                if (scoreBoard) {
                                    ScoreBoardMessage score = new ScoreBoardMessage(
                                            room.getLevel().getName(), true, "§eGunWar", new LinkedList<>());
                                    LinkedList<String> ms = new LinkedList<>();
                                    for (String string : language.gameTimeScoreBoard.split("\n")) {
                                        ms.add(string
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
                        }else if (room.getMode() == 3) {
                            if (room.victory == 1) {
                                tipMessage.setMessage(language.victoryMessage.replace("%teamName%", language.teamNameRed));
                                LinkedList<String> ms = new LinkedList<>();
                                ms.add(language.victoryMessage.replace("%teamName%", language.teamNameRed));
                                scoreBoardMessage.setMessages(ms);
                            } else {
                                tipMessage.setMessage(language.victoryMessage.replace("%teamName%", language.teamNameBlue));
                                LinkedList<String> ms = new LinkedList<>();
                                ms.add(language.victoryMessage.replace("%teamName%", language.teamNameBlue));
                                scoreBoardMessage.setMessages(ms);
                            }
                            this.sendMessage();
                        }
                    }
                    room.task.remove(taskName);
                }

                private String getStringHealth(float health) {
                    StringBuilder string = new StringBuilder("§c" + health + "/20  ");
                    for (int j = 0; j < 20; j++) {
                        if (j < health) {
                            string.append("§a▋");
                        }else {
                            string.append("§c▋");
                        }
                    }
                    return string.toString();
                }

                private void sendMessage() {
                    for (Player player : room.getPlayers().keySet()) {
                        if (bottom) {
                            Api.setPlayerShowMessage(player.getName(), tipMessage);
                        }
                        if (scoreBoard) {
                            Api.setPlayerShowMessage(player.getName(), scoreBoardMessage);
                        }
                    }
                }

            });
        }
    }

}
