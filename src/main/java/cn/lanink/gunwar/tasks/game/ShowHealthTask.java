package cn.lanink.gunwar.tasks.game;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.capturetheflag.CTFModeRoom;
import cn.lanink.gunwar.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.DummyBossBar;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShowHealthTask extends PluginTask<GunWar> {

    private final Language language;
    private final BaseRoom room;
    private final ConcurrentHashMap<Player, DummyBossBar> bossBarMap = new ConcurrentHashMap<>();

    public ShowHealthTask(GunWar owner, BaseRoom room) {
        super(owner);
        this.language = owner.getLanguage();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != IRoomStatus.ROOM_STATUS_GAME) {
            this.cancel();
            return;
        }
        for (Map.Entry<Player, Integer> entry : this.room.getPlayers().entrySet()) {
            Tools.createBossBar(entry.getKey(), this.bossBarMap);
            DummyBossBar bossBar = this.bossBarMap.get(entry.getKey());
            switch (entry.getValue()) {
                case 11:
                case 12:
                    if (this.room instanceof CTFModeRoom) {
                        int respawnTime = ((CTFModeRoom) this.room).getPlayerRespawnTime(entry.getKey());
                        bossBar.setText(this.language.translateString("gameTimeRespawnBoosBar", respawnTime));
                        bossBar.setLength(100 - (respawnTime / 20F * 100));
                        break;
                    }
                default:
                    float health;
                    if (this.owner.isEnableAloneHealth()) {
                        health = this.room.getPlayerHealth(entry.getKey());
                    }else {
                        health = entry.getKey().getHealth();
                    }
                    bossBar.setText(this.language.translateString("gameTimeBoosBar",
                            "§c" + String.format("%.1f", health) + "/20  "));
                    bossBar.setLength(health / 20 * 100);
                    break;
            }
        }
        for (Map.Entry<Player, DummyBossBar> entry : this.bossBarMap.entrySet()) {
            if (!this.room.isPlaying(entry.getKey())) {
                entry.getKey().removeBossBar(entry.getValue().getBossBarId());
            }
        }
    }

    @Override
    public void onCancel() {
        for (DummyBossBar bossBar : this.bossBarMap.values()) {
            bossBar.getPlayer().removeBossBar(bossBar.getBossBarId());
        }
    }

}
