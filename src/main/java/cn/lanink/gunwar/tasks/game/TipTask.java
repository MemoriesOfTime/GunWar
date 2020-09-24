package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.GameMode;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.Map;

public class TipTask extends PluginTask<GunWar> {

    private final Language language;
    private final Room room;

    public TipTask(GunWar owner, Room room) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != 2) {
            this.cancel();
            return;
        }
        for (Map.Entry<Player, Integer> entry : this.room.getPlayers().entrySet()) {
            switch (entry.getValue()) {
                case 11:
                case 12:
                    if (this.room.getGameMode() == GameMode.CTF) {
                        entry.getKey().sendTip(this.language.gameTimeRespawnBottom
                                .replace("%time%", room.getPlayerRespawnTime(entry.getKey()) + ""));
                    }
                    break;
                default:
                    entry.getKey().sendTip(this.language.gameTimeBottom
                            .replace("%health%",
                                    this.getStringHealth(this.room.getPlayerHealth(entry.getKey()))));
                    break;
            }
        }
    }

    private String getStringHealth(float health) {
        StringBuilder string = new StringBuilder("§c" + String.format("%.1f", health) + "/20  ");
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
