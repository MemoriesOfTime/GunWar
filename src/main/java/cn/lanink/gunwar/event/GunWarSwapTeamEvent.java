package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.blasting.BlastingModeRoom;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

/**
 * 爆破模式队伍交换事件
 *
 * @author LT_Name
 */
public class GunWarSwapTeamEvent extends GunWarRoomEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    @Getter
    protected final LinkedList<Player> oldRedTeam;
    @Getter
    protected final LinkedList<Player> oldBlueTeam;
    @Getter
    protected final int oldRedScore;
    @Getter
    protected final int oldBlueScore;

    @Getter
    @Setter
    protected LinkedList<Player> newRedTeam;
    @Getter
    @Setter
    protected LinkedList<Player> newBlueTeam;
    @Getter
    @Setter
    protected int newRedScore;
    @Getter
    @Setter
    protected int newBlueScore;

    public GunWarSwapTeamEvent(BlastingModeRoom room, LinkedList<Player> oldRedTeam, LinkedList<Player> oldBlueTeam, int oldRedScore, int oldBlueScore) {
        this.room = room;
        this.oldRedTeam = oldRedTeam;
        this.oldBlueTeam = oldBlueTeam;
        this.oldRedScore = oldRedScore;
        this.oldBlueScore = oldBlueScore;

        this.newRedTeam = new LinkedList<>(this.oldBlueTeam);
        this.newBlueTeam = new LinkedList<>(this.oldRedTeam);
        this.newRedScore = this.oldBlueScore;
        this.newBlueScore = this.oldRedScore;
    }

    @Override
    public BlastingModeRoom getRoom() {
        return (BlastingModeRoom) this.room;
    }
}
