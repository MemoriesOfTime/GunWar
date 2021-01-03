package cn.lanink.gunwar.tasks;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.utils.LanguageOld;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;
import java.util.Map;


public class WaitTask extends PluginTask<GunWar> {

    private final LanguageOld languageOld;
    private final BaseRoom room;

    public WaitTask(GunWar owner, BaseRoom room) {
        super(owner);
        this.languageOld = owner.getLanguageOld();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != IRoomStatus.ROOM_STATUS_WAIT) {
            this.cancel();
            return;
        }
        for (Player player : this.room.getPlayers().keySet()) {
            player.getInventory().setItem(3, Tools.getItem(11));
            player.getInventory().setItem(5, Tools.getItem(12));
            player.getInventory().setItem(8, Tools.getItem(10));
        }
        if (this.room.getPlayers().size() >= this.room.getMinPlayers()) {
            if (this.room.getPlayers().size() == this.room.getMaxPlayers() && this.room.waitTime > 10) {
                this.room.waitTime = 10;
            }
            if (this.room.waitTime > 0) {
                this.room.waitTime--;
                if (this.room.waitTime <= 5) {
                    Tools.addSound(this.room, Sound.RANDOM_CLICK);
                }
                for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                    entry.getKey().sendActionBar(languageOld.waitTimeBottom
                            .replace("%playerNumber%", room.getPlayers().size() + "")
                            .replace("%time%", room.waitTime + ""));
                    String team;
                    switch (entry.getValue()) {
                        case 1:
                        case 11:
                            team = languageOld.teamNameRed;
                            break;
                        case 2:
                        case 12:
                            team = languageOld.teamNameBlue;
                            break;
                        default:
                            team = languageOld.noTeamSelect;
                            break;
                    }
                    LinkedList<String> ms = new LinkedList<>();
                    for (String string : this.languageOld.waitTimeScoreBoard.split("\n")) {
                        ms.add(string.replace("%team%", team)
                                .replace("%playerNumber%", room.getPlayers().size() + "")
                                .replace("%time%", room.waitTime + ""));
                    }
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.languageOld.scoreBoardTitle, ms);
                }
            }else {
                this.room.startGame();
                this.cancel();
            }
        }else if (this.room.getPlayers().size() > 0) {
            if (this.room.waitTime != this.room.getSetWaitTime()) {
                this.room.waitTime = this.room.getSetWaitTime();
            }
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                entry.getKey().sendActionBar(languageOld.waitBottom
                        .replace("%playerNumber%", room.getPlayers().size() + ""));
                String team;
                switch (entry.getValue()) {
                    case 1:
                    case 11:
                        team = languageOld.teamNameRed;
                        break;
                    case 2:
                    case 12:
                        team = languageOld.teamNameBlue;
                        break;
                    default:
                        team = languageOld.noTeamSelect;
                        break;
                }
                LinkedList<String> ms = new LinkedList<>();
                for (String string : languageOld.waitScoreBoard.split("\n")) {
                    ms.add(string.replace("%team%", team)
                            .replace("%playerNumber%", room.getPlayers().size() + ""));
                }
                owner.getScoreboard().showScoreboard(entry.getKey(), this.languageOld.scoreBoardTitle, ms);
            }
        }else {
            this.room.endGame();
            this.cancel();
        }
    }

}
