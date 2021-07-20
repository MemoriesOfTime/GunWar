package cn.lanink.gunwar.tasks.game.ctf;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.entity.EntityFlag;
import cn.lanink.gunwar.room.capturetheflag.CTFModeRoom;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

/**
 * @author lt_name
 */
public class FlagPickupCheckTask extends PluginTask<GunWar> {

    private int time;
    private final CTFModeRoom room;
    private final EntityFlag entityFlag;
    private final int team;

    public FlagPickupCheckTask(GunWar owner, CTFModeRoom room, EntityFlag entityFlag) {
        super(owner);
        this.time = 10;
        this.room = room;
        this.entityFlag = entityFlag;
        entityFlag.setNameTagVisible(true);
        entityFlag.setNameTagAlwaysVisible(true);
        this.team = entityFlag.namedTag.getInt("GunWarTeam");
    }

    @Override
    public void onRun(int i) {
        if (time <= 0) {
            this.cancel();
            return;
        }
        this.entityFlag.setNameTag("§e" + this.time);
        if (this.team == 11 && this.room.haveRedFlag != null) {
            this.cancel();
        }else if (this.team == 12 && this.room.haveBlueFlag != null) {
            this.cancel();
        }
        this.time--;
    }

    @Override
    public void onCancel() {
        switch (this.team) {
            case 11:
                this.entityFlag.teleport(this.room.getRedSpawn().add(0, 0.3, 0));
                break;
            case 12:
                this.entityFlag.teleport(this.room.getBlueSpawn().add(0, 0.3, 0));
                break;
        }

        this.entityFlag.setNameTag("");
        this.entityFlag.setNameTagVisible(false);
        this.entityFlag.setNameTagAlwaysVisible(false);
        this.entityFlag.getLevel().addSound(entityFlag, Sound.MOB_ENDERMEN_PORTAL);
    }

}
