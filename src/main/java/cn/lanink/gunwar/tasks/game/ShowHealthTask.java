package cn.lanink.gunwar.tasks.game;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.GameMode;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.DummyBossBar;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShowHealthTask extends PluginTask<GunWar> {

    private final Language language;
    private final Room room;
    private final ConcurrentHashMap<Player, DummyBossBar> bossBarMap = new ConcurrentHashMap<>();

    public ShowHealthTask(GunWar owner, Room room) {
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
            if (!this.bossBarMap.containsKey(entry.getKey())) {
                DummyBossBar bossBar = new DummyBossBar.Builder(entry.getKey()).build();
                bossBar.setColor(255, 0, 0);
                entry.getKey().createBossBar(bossBar);
                this.bossBarMap.put(entry.getKey(), bossBar);
            }
            DummyBossBar bossBar = this.bossBarMap.get(entry.getKey());
            switch (entry.getValue()) {
                case 11:
                case 12:
                    if (this.room.getGameMode() == GameMode.CTF) {
                        bossBar.setText(this.language.gameTimeRespawnBottom
                                .replace("%time%", room.getPlayerRespawnTime(entry.getKey()) + ""));
                        bossBar.setLength(0);
                    }
                    break;
                default:
                    float health = this.room.getPlayerHealth(entry.getKey());
                    bossBar.setText(this.language.gameTimeBottom
                            .replace("%health%", "§c" + String.format("%.1f", health) + "/20  "));
                    bossBar.setLength(health / 20 * 100);
                    break;
            }
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        for (DummyBossBar bossBar : this.bossBarMap.values()) {
            bossBar.getPlayer().removeBossBar(bossBar.getBossBarId());
        }
    }

}
