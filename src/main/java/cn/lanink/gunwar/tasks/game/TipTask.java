package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import tip.messages.TipMessage;
import tip.utils.Api;

public class TipTask extends PluginTask<GunWar> {

    private final String taskName = "ScoreBoardTask";
    private final Language language;
    private final Room room;

    public TipTask(GunWar owner, Room room) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
        }
        for (Player player : room.getPlayers().keySet()) {
            TipMessage tip = new TipMessage(room.getLevel().getName(), true, 0, null);
            tip.setMessage(language.gameTimeBottom
                    .replace("%health%", this.getStringHealth(room.getPlayerHealth().getOrDefault(player, 0F))));
            Api.setPlayerShowMessage(player.getName(), tip);
        }
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

}
