package cn.lanink.gunwar.event;

import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.Team;
import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;

import java.util.LinkedList;
import java.util.Map;

public class GunWarRoomEndEvent extends GunWarRoomEvent {

    private static final HandlerList handlers = new HandlerList();
    private int victory;

    public GunWarRoomEndEvent(BaseRoom room, int victory) {
        this.room = room;
        this.victory = victory;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public int getVictory() {
        return this.victory;
    }

    public void setVictory(int victory) {
        this.victory = victory;
    }

    /**
     * 获取胜利玩家
     * @return 胜利玩家
     */
    public LinkedList<Player> getVictoryPlayers() {
        LinkedList<Player> players = new LinkedList<>();
        for (Map.Entry<Player, Team> entry : this.room.getPlayers().entrySet()) {
            if (this.victory == 1) {
                if (entry.getValue() == Team.RED || entry.getValue() == Team.RED_DEATH) {
                    players.add(entry.getKey());
                }
            }else if (this.victory == 2) {
                if (entry.getValue() == Team.BLUE || entry.getValue() == Team.BLUE_DEATH) {
                    players.add(entry.getKey());
                }
            }
        }
        return players;
    }

    /**
     * 获取失败玩家
     * @return 失败玩家
     */
    public LinkedList<Player> getDefeatPlayers() {
        LinkedList<Player> players = new LinkedList<>();
        for (Map.Entry<Player, Team> entry : this.room.getPlayers().entrySet()) {
            if (this.victory == 1) {
                if (entry.getValue() == Team.BLUE || entry.getValue() == Team.BLUE_DEATH) {
                    players.add(entry.getKey());
                }
            }else if (this.victory == 2) {
                if (entry.getValue() == Team.RED || entry.getValue() == Team.RED_DEATH) {
                    players.add(entry.getKey());
                }
            }
        }
        return players;
    }

}
