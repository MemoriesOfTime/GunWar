package cn.lanink.gunwar.tasks;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.Team;
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
    private int victoryTime;
    private int victory = 0;
    private Player victoryPlayer;

    public VictoryTask(GunWar owner, BaseRoom room, int victory) {
        this(owner, room);

        this.victory = victory;

        for (Map.Entry<Player, Team> entry: room.getPlayers().entrySet()) {
            LinkedList<String> ms = new LinkedList<>();
            switch (this.victory) {
                case 1:
                    if (entry.getValue() == Team.RED || entry.getValue() == Team.RED_DEATH) {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.VICTORY);
                    }else {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.DEFEAT);
                    }
                    ms.add(this.language.translateString("victoryMessage",
                            this.language.translateString("teamNameRed")));
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.language.translateString("scoreBoardTitle"), ms);
                    entry.getKey().sendTitle(this.language.translateString("victoryRed"), "", 10, 40, 20);
                    break;
                case 2:
                    if (entry.getValue() == Team.BLUE || entry.getValue() == Team.BLUE_DEATH) {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.VICTORY);
                    }else {
                        GameRecord.addPlayerRecord(entry.getKey(), RecordType.DEFEAT);
                    }
                    ms.add(this.language.translateString("victoryMessage",
                            this.language.translateString("teamNameBlue")));
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.language.translateString("scoreBoardTitle"), ms);
                    entry.getKey().sendTitle(this.language.translateString("victoryBlue"), "", 10, 40, 20);
                    break;
                default:
                    ms.add(this.language.translateString("game_ctf_draw"));
                    owner.getScoreboard().showScoreboard(entry.getKey(), this.language.translateString("scoreBoardTitle"), ms);
                    entry.getKey().sendTitle(this.language.translateString("game_ctf_draw"), "", 10, 40, 20);
                    break;
            }
        }
    }

    public VictoryTask(GunWar owner, BaseRoom room, Player victory) {
        this(owner, room);

        this.victoryPlayer = victory;

        for (Map.Entry<Player, Team> entry: room.getPlayers().entrySet()) {
            LinkedList<String> ms = new LinkedList<>();
            if (entry.getKey() == this.victoryPlayer) {
                GameRecord.addPlayerRecord(entry.getKey(), RecordType.VICTORY);
            }else {
                GameRecord.addPlayerRecord(entry.getKey(), RecordType.DEFEAT);
            }
            ms.add(this.language.translateString("victoryMessage", this.victoryPlayer.getName()));
            owner.getScoreboard().showScoreboard(entry.getKey(), this.language.translateString("scoreBoardTitle"), ms);
        }
    }

    private VictoryTask(GunWar owner, BaseRoom room) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
        this.victoryTime = 10;
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
            if (!this.room.getPlayers().isEmpty()) {
                for (Map.Entry<Player, Team> entry : room.getPlayers().entrySet()) {
                    if (this.victoryPlayer != null) {
                        entry.getKey().sendTip(this.language.translateString("victoryMessage", this.victoryPlayer.getName()));
                        Tools.spawnFirework(this.victoryPlayer);
                    }else if (entry.getValue() != Team.NULL) {
                        if (this.victory == 1) {
                            entry.getKey().sendTip(this.language.translateString("victoryMessage",
                                    this.language.translateString("teamNameRed")));
                            if (entry.getValue() == Team.RED) {
                                Tools.spawnFirework(entry.getKey());
                            }
                        }else if (this.victory == 2) {
                            entry.getKey().sendTip(this.language.translateString("victoryMessage",
                                    this.language.translateString("teamNameBlue")));
                            if (entry.getValue() == Team.BLUE) {
                                Tools.spawnFirework(entry.getKey());
                            }
                        }else {
                            entry.getKey().sendTip(this.language.translateString("game_ctf_draw"));
                        }
                    }
                }
            }
        }
    }

}
