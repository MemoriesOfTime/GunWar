package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarRoomEndEvent;
import cn.lanink.gunwar.room.Room;
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
    private final Room room;
    private final int victory;
    private int victoryTime;

    public VictoryTask(GunWar owner, Room room, int victory) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.language = owner.getLanguage();
        this.room = room;
        this.victoryTime = 10;
        this.victory = victory;
        for (Map.Entry<Player, Integer> entry: room.getPlayers().entrySet()) {
            if (this.victory == 1) {
                if (entry.getValue() == 1) {
                    GameRecord.addPlayerRecord(entry.getKey(), RecordType.VICTORY);
                }else {
                    GameRecord.addPlayerRecord(entry.getKey(), RecordType.DEFEAT);
                }
                LinkedList<String> ms = new LinkedList<>();
                ms.add(this.language.victoryMessage.replace("%teamName%", this.language.teamNameRed));
                owner.getScoreboard().showScoreboard(entry.getKey(), this.language.scoreBoardTitle, ms);
                entry.getKey().sendTitle(this.language.victoryRed, "", 10, 40, 20);
            }else if (this.victory == 2) {
                if (entry.getValue() == 2) {
                    GameRecord.addPlayerRecord(entry.getKey(), RecordType.VICTORY);
                }else {
                    GameRecord.addPlayerRecord(entry.getKey(), RecordType.DEFEAT);
                }
                LinkedList<String> ms = new LinkedList<>();
                ms.add(this.language.victoryMessage.replace("%teamName%", this.language.teamNameBlue));
                owner.getScoreboard().showScoreboard(entry.getKey(), this.language.scoreBoardTitle, ms);
                entry.getKey().sendTitle(this.language.victoryBlue, "", 10, 40, 20);
            }
        }
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 3) {
            this.cancel();
            return;
        }
        if (this.victoryTime < 1) {
            owner.getServer().getPluginManager().callEvent(new GunWarRoomEndEvent(this.room, this.victory));
            this.room.endGame();
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
                        }
                    }
                }
            }
        }
    }

    @Override
    public void cancel() {
        while (owner.taskList.contains(this.getTaskId())) {
            owner.taskList.remove(this.getTaskId());
        }
        super.cancel();
    }

}
