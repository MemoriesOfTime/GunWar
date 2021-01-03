package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.utils.LanguageOld;
import cn.lanink.gunwar.utils.Tools;
import cn.lanink.gunwar.utils.gamerecord.GameRecord;
import cn.lanink.gunwar.utils.gamerecord.RecordType;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;
import java.util.Map;


public class VictoryTask extends PluginTask<GunWar> {

    private final LanguageOld languageOld;
    private final BaseRoom room;
    private final int victory;
    private int victoryTime;

    public VictoryTask(GunWar owner, BaseRoom room, int victory) {
        super(owner);
        this.languageOld = owner.getLanguageOld();
        this.room = room;
        this.victoryTime = 10;
        this.victory = victory;
        for (Map.Entry<Player, Integer> entry: room.getPlayers().entrySet()) {
            LinkedList<String> ms = new LinkedList<>();
            switch (this.victory) {
                case 1:
                    if (entry.getValue() == 1 || entry.getValue() == 11) {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.VICTORY);
                    }else {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.DEFEAT);
                    }
                    ms.add(this.languageOld.victoryMessage.replace("%teamName%", this.languageOld.teamNameRed));
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.languageOld.scoreBoardTitle, ms);
                    entry.getKey().sendTitle(this.languageOld.victoryRed, "", 10, 40, 20);
                    break;
                case 2:
                    if (entry.getValue() == 2 || entry.getValue() == 12) {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.VICTORY);
                    }else {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.DEFEAT);
                    }
                    ms.add(this.languageOld.victoryMessage.replace("%teamName%", this.languageOld.teamNameBlue));
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.languageOld.scoreBoardTitle, ms);
                    entry.getKey().sendTitle(this.languageOld.victoryBlue, "", 10, 40, 20);
                    break;
                default:
                    ms.add(this.languageOld.game_ctf_draw);
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.languageOld.scoreBoardTitle, ms);
                    entry.getKey().sendTitle(this.languageOld.game_ctf_draw, "", 10, 40, 20);
                    break;
            }
        }
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != 3) {
            this.cancel();
            return;
        }
        if (this.victoryTime < 1) {
            this.room.endGame(this.victory);
            this.cancel();
        }else {
            this.victoryTime--;
            if (room.getPlayers().size() > 0) {
                for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                    if (entry.getValue() != 0) {
                        if (this.victory == 1) {
                            entry.getKey().sendTip(this.languageOld.victoryMessage.replace("%teamName%", this.languageOld.teamNameRed));
                            if (entry.getValue() == 1) {
                                Tools.spawnFirework(entry.getKey());
                            }
                        }else if (this.victory == 2) {
                            entry.getKey().sendTip(this.languageOld.victoryMessage.replace("%teamName%", this.languageOld.teamNameBlue));
                            if (entry.getValue() == 2) {
                                Tools.spawnFirework(entry.getKey());
                            }
                        }else {
                            entry.getKey().sendTip(this.languageOld.game_ctf_draw);
                        }
                    }
                }
            }
        }
    }

}
