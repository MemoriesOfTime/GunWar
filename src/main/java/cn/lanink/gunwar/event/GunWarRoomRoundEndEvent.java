package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class GunWarRoomRoundEndEvent extends GunWarRoomEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Team victoryTeam;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public GunWarRoomRoundEndEvent(BaseRoom room, Team victory) {
        this.room = room;
        this.victoryTeam = victory;
    }

    public void setVictoryTeam(Team victoryTeam) {
        this.victoryTeam = victoryTeam;
    }

    public Team getVictoryTeam() {
        return this.victoryTeam;
    }
}
