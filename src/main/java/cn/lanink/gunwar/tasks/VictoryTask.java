package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.PluginTask;

import java.util.List;
import java.util.Map;


public class VictoryTask extends PluginTask<GunWar> {

    private final Language language = GunWar.getInstance().getLanguage();
    private final Room room;
    private int victoryTime;

    public VictoryTask(GunWar owner, Room room, int victory) {
        super(owner);
        this.room = room;
        this.victoryTime = 10;
        this.room.victory = victory;
        List<String> vCmds = owner.getConfig().getStringList("胜利执行命令");
        List<String> dCmds = owner.getConfig().getStringList("失败执行命令");
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            if (victory == 1) {
                entry.getKey().sendTitle(this.language.victoryRed, "", 10, 30, 20);
                if (entry.getValue() == 1 || entry.getValue() == 11) {
                    this.cmd(entry.getKey(), vCmds);
                }else if (entry.getValue() == 2 || entry.getValue() == 12) {
                    this.cmd(entry.getKey(), dCmds);
                }
            }else if (victory == 2) {
                entry.getKey().sendTitle(this.language.victoryBlue, "", 10, 30, 20);
                if (entry.getValue() == 1 || entry.getValue() == 11) {
                    this.cmd(entry.getKey(), dCmds);
                }else if (entry.getValue() == 2 || entry.getValue() == 12) {
                    this.cmd(entry.getKey(), vCmds);
                }
            }
        }
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 3) {
            this.cancel();
        }
        if (this.victoryTime < 1) {
            this.room.endGame();
            this.cancel();
        }else {
            this.victoryTime--;
/*            owner.getServer().getScheduler().scheduleAsyncTask(GunWar.getInstance(), new AsyncTask() {
                @Override
                public void onRun() {
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
            });*/
        }
    }

    private void cmd(Player player, List<String> cmds) {
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
