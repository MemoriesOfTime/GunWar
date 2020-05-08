package cn.lanink.gunwar.tasks;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
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

    private final String taskName = "TipsTask";
    private final Room room;
    private final boolean bottom, scoreBoard;
    private TipMessage tipMessage;
    private ScoreBoardMessage scoreBoardMessage;

    public TipsTask(GunWar owner, Room room) {
        super(owner);
        this.room = room;
        this.bottom = owner.getConfig().getBoolean("底部显示信息", true);
        this.scoreBoard = owner.getConfig().getBoolean("计分板显示信息", true);
        this.tipMessage = new TipMessage(room.getLevel().getName(), true, 0, null);
        this.scoreBoardMessage = new ScoreBoardMessage(
                room.getLevel().getName(), true, "§eGunWar", new LinkedList<>());
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
                                tipMessage.setMessage("§a当前已有: " + room.getPlayers().size() + " 位玩家" +
                                        "\n§a游戏还有: " + room.waitTime + " 秒开始！");
                                LinkedList<String> ms = new LinkedList<>();
                                ms.add("玩家: §a" + room.getPlayers().size() + "/16 ");
                                ms.add("§a开始倒计时： §l§e" + room.waitTime + " ");
                                scoreBoardMessage.setMessages(ms);
                            }else {
                                tipMessage.setMessage("§c等待玩家加入中,当前已有: " + room.getPlayers().size() + " 位玩家");
                                LinkedList<String> ms = new LinkedList<>();
                                ms.add("玩家: §a" + room.getPlayers().size() + "/10 ");
                                ms.add("最低游戏人数为 2 人 ");
                                ms.add("等待玩家加入中 ");
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
                                    tipMessage.setMessage("§l§c血量： " + room.getPlayerHealth().get(player));
                                    Api.setPlayerShowMessage(player.getName(), tipMessage);
                                }
                                if (scoreBoard) {
                                    LinkedList<String> ms = new LinkedList<>();
                                    ms.add("§l§a当前血量： §e" + room.getPlayerHealth().get(player) + " ");
                                    ms.add("§l§a剩余时间： §e" + room.gameTime + " §a秒 ");
                                    ms.add("§l§a队伍存活人数：");
                                    ms.add("§l§c红: " + red + " 人 §9蓝: " + blue + "人 ");
                                    ms.add("§l§a队伍胜利：");
                                    ms.add("§l§c红: " + room.redRound + " 回合 §9蓝: " + room.blueRound + "回合 ");
                                    scoreBoardMessage.setMessages(ms);
                                    Api.setPlayerShowMessage(player.getName(), scoreBoardMessage);
                                }
                            }
                        }else if (room.getMode() == 3) {
                            if (room.victory == 1) {
                                tipMessage.setMessage("§e恭喜红队获得胜利");
                                LinkedList<String> ms = new LinkedList<>();
                                ms.add("§e恭喜红队获得胜利! ");
                                scoreBoardMessage.setMessages(ms);
                            } else {
                                tipMessage.setMessage("§e恭喜蓝队获得胜利！");
                                LinkedList<String> ms = new LinkedList<>();
                                ms.add("§e恭喜蓝队获得胜利! ");
                                scoreBoardMessage.setMessages(ms);
                            }
                            this.sendMessage();
                        }
                        room.task.remove(taskName);
                    }
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
