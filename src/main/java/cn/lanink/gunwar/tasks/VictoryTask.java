package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.lanink.gunwar.utils.gamerecord.GameRecord;
import cn.lanink.gunwar.utils.gamerecord.RecordType;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;
import java.util.Map;


public class VictoryTask extends PluginTask<GunWar> {

    private final Language language;
    private final BaseRoom room;
    private final int victory;
    private int victoryTime;

    public VictoryTask(GunWar owner, BaseRoom room, int victory) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
        this.victoryTime = 10;
        this.victory = victory;
        for (Map.Entry<Player, Integer> entry: room.getPlayers().entrySet()) {
            LinkedList<String> ms = new LinkedList<>();
            switch (this.victory) {
                case 1:
                    if (entry.getValue() == 1) {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.VICTORY);
                    }else {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.DEFEAT);
                    }
                    ms.add(this.language.victoryMessage.replace("%teamName%", this.language.teamNameRed));
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.language.scoreBoardTitle, ms);
                    entry.getKey().sendTitle(this.language.victoryRed, "", 10, 40, 20);
                    break;
                case 2:
                    if (entry.getValue() == 2) {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.VICTORY);
                    }else {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.DEFEAT);
                    }
                    ms.add(this.language.game_ctf_draw);
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.language.scoreBoardTitle, ms);
                    entry.getKey().sendTitle(this.language.game_ctf_draw, "", 10, 40, 20);
                    break;
                default:
                    ms.add(this.language.game_ctf_draw);
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.language.scoreBoardTitle, ms);
                    entry.getKey().sendTitle(this.language.game_ctf_draw, "", 10, 40, 20);
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
                            entry.getKey().sendTip(this.language.victoryMessage.replace("%teamName%", this.language.teamNameRed));
                            if (entry.getValue() == 1) {
                                Tools.spawnFirework(entry.getKey());
                            }
                        }else if (this.victory == 2) {
                            entry.getKey().sendTip(this.language.victoryMessage.replace("%teamName%", this.language.teamNameBlue));
                            if (entry.getValue() == 2) {
                                Tools.spawnFirework(entry.getKey());
                            }
                        }else {
                            entry.getKey().sendTip(this.language.game_ctf_draw);
                        }
                    }
                }
            }
        }
    }

}
