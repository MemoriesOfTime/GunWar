package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.event.GunWarRoomEndEvent;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.GameRecord;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import tip.messages.ScoreBoardMessage;
import tip.messages.TipMessage;
import tip.utils.Api;

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
        TipMessage tipMessage = new TipMessage(room.getLevel().getName(), true, 0, null);
        ScoreBoardMessage scoreBoardMessage = new ScoreBoardMessage(
                room.getLevel().getName(), true, this.language.scoreBoardTitle, new LinkedList<>());
        if (this.victory == 1) {
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
        for (Map.Entry<Player, Integer> entry: room.getPlayers().entrySet()) {
            if (this.victory == 1) {
                if (entry.getValue() == 1) {
                    GameRecord.addVictory(entry.getKey());
                }else {
                    GameRecord.addDefeat(entry.getKey());
                }
                entry.getKey().sendTitle(this.language.victoryRed, "", 10, 40, 20);
            }else if (this.victory == 2) {
                if (entry.getValue() == 2) {
                    GameRecord.addVictory(entry.getKey());
                }else {
                    GameRecord.addDefeat(entry.getKey());
                }
                entry.getKey().sendTitle(this.language.victoryBlue, "", 10, 40, 20);
            }
            Api.setPlayerShowMessage(entry.getKey().getName(), tipMessage);
            Api.setPlayerShowMessage(entry.getKey().getName(), scoreBoardMessage);
        }
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 3) {
            this.cancel();
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
                        if (this.victory == 1 && entry.getValue() == 1) {
                            Tools.spawnFirework(entry.getKey());
                        }else if (this.victory == 2 && entry.getValue() == 2) {
                            Tools.spawnFirework(entry.getKey());
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
