package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.scheduler.PluginTask;
import tip.messages.ScoreBoardMessage;
import tip.messages.TipMessage;
import tip.utils.Api;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class VictoryTask extends PluginTask<GunWar> {

    private final String taskName = "VictoryTask";
    private final Language language = GunWar.getInstance().getLanguage();
    private final Room room;
    private int victoryTime;

    public VictoryTask(GunWar owner, Room room, int victory) {
        super(owner);
        this.room = room;
        this.victoryTime = 10;
        this.room.victory = victory;
        TipMessage tipMessage = new TipMessage(room.getLevel().getName(), true, 0, null);
        ScoreBoardMessage scoreBoardMessage = new ScoreBoardMessage(
                room.getLevel().getName(), true, this.language.scoreBoardTitle, new LinkedList<>());
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
        for (Player player: room.getPlayers().keySet()) {
            if (this.room.victory == 1) {
                player.sendTitle(this.language.victoryRed, "", 10, 40, 20);
            }else if (this.room.victory == 2) {
                player.sendTitle(this.language.victoryBlue, "", 10, 40, 20);
            }
            if (owner.getConfig().getBoolean("底部显示信息", true)) {
                Api.setPlayerShowMessage(player.getName(), tipMessage);
            }
            if (owner.getConfig().getBoolean("计分板显示信息", true)) {
                Api.setPlayerShowMessage(player.getName(), scoreBoardMessage);
            }
        }
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 3) {
            this.cancel();
        }
        if (this.victoryTime < 1) {
            if (!this.room.task.contains(this.taskName)) {
                this.room.task.add(this.taskName);
                if (this.room.getPlayers().size() > 0) {
                    List<String> vCmds = owner.getConfig().getStringList("胜利执行命令");
                    List<String> dCmds = owner.getConfig().getStringList("失败执行命令");
                    for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                        if (this.room.victory == 1) {
                            entry.getKey().sendTitle(this.language.victoryRed, "", 10, 30, 20);
                            if (entry.getValue() == 1 || entry.getValue() == 11) {
                                this.cmd(entry.getKey(), vCmds);
                            } else if (entry.getValue() == 2 || entry.getValue() == 12) {
                                this.cmd(entry.getKey(), dCmds);
                            }
                        } else if (this.room.victory == 2) {
                            entry.getKey().sendTitle(this.language.victoryBlue, "", 10, 30, 20);
                            if (entry.getValue() == 1 || entry.getValue() == 11) {
                                this.cmd(entry.getKey(), dCmds);
                            } else if (entry.getValue() == 2 || entry.getValue() == 12) {
                                this.cmd(entry.getKey(), vCmds);
                            }
                        }
                    }
                }
                this.room.task.remove(this.taskName);
                this.room.endGame();
            }
            this.cancel();
        }else {
            this.victoryTime--;
            if (room.getPlayers().size() > 0) {
                for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                    if (entry.getValue() != 0) {
                        if (room.victory == 1 && entry.getValue() == 1) {
                            Tools.spawnFirework(entry.getKey());
                        }else if (room.victory == 2 && entry.getValue() == 2) {
                            Tools.spawnFirework(entry.getKey());
                        }
                    }
                }
            }
        }
    }

    private void cmd(Player player, List<String> cmds) {
        if (player == null || cmds == null || cmds.size() < 1) {
            return;
        }
        for (String s : cmds) {
            String[] cmd = s.split("&");
            if ((cmd.length > 1) && (cmd[1].equals("con"))) {
                owner.getServer().dispatchCommand(new ConsoleCommandSender(), cmd[0].replace("@p", player.getName()));
            } else {
                owner.getServer().dispatchCommand(player, cmd[0].replace("@p", player.getName()));
            }
        }
    }

}
