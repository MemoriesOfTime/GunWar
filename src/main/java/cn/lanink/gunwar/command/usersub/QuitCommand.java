package cn.lanink.gunwar.command.usersub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.teamsystem.TeamSystem;
import cn.lanink.teamsystem.team.Team;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class QuitCommand extends BaseSubCommand {

    public QuitCommand(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "退出" };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (this.gunWar.isHasTeamSystem()) {
            Team team = TeamSystem.Companion.getTeamByPlayer(player);
            if (team != null) {
                if (!team.isTeamLeader(player)) {
                    sender.sendMessage("[GunWar-TeamSystem] 你不是队长，无法主动退出游戏！");
                    sender.sendMessage("[GunWar-TeamSystem] 请让队长退出游戏或先退出队伍！！");
                    return true;
                }
            }
        }

        for (BaseRoom room : this.gunWar.getGameRoomManager().getGameRoomMap().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player);
                return true;
            }
        }
        sender.sendMessage(this.language.translateString("quitRoomNotInRoom"));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
