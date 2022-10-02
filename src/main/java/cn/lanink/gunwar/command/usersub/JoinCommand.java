package cn.lanink.gunwar.command.usersub;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.teamsystem.TeamSystem;
import cn.lanink.teamsystem.team.Team;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

import java.util.LinkedList;

public class JoinCommand extends BaseSubCommand {

    public JoinCommand(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "加入" };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (this.gunWar.getGameRoomManager().getGameRoomMap().size() > 0) {
            if (player.riding != null) {
                sender.sendMessage(this.language.translateString("joinRoomIsRiding"));
                return true;
            }
            for (BaseRoom room : this.gunWar.getGameRoomManager().getGameRoomMap().values()) {
                if (room.isPlaying(player)) {
                    sender.sendMessage(this.language.translateString("joinRoomIsInRoom"));
                    return true;
                }
            }

            if (this.gunWar.isHasTeamSystem()) {
                Team team = TeamSystem.Companion.getTeamByPlayer(player);
                if (team != null) {
                    if (!team.isTeamLeader(player)) {
                        sender.sendMessage("[GunWar-TeamSystem] 你不是队长，无法主动加入游戏！");
                        sender.sendMessage("[GunWar-TeamSystem] 请让队长加入游戏或先退出队伍！！");
                        return true;
                    }
                    if (!team.isAllMemberOnline()) {
                        //TODO 当TeamSystem支持后，尝试传送玩家到当前服务器
                        sender.sendMessage("[GunWar-TeamSystem] 队伍中有玩家不在线，无法加入游戏！");
                        return true;
                    }
                }
            }

            if (args.length < 2) {
                BaseRoom room = this.gunWar.getGameRoomManager().getCanJoinGameRoom(player);
                if (room != null) {
                    room.joinRoom(player);
                    sender.sendMessage(this.language.translateString("joinRandomRoom"));
                    return true;
                }
            }else {
                String[] s = args[1].split(":");
                if (s.length == 2 && s[0].toLowerCase().trim().equals("mode")) {
                    String modeName = s[1].toLowerCase().trim();
                    LinkedList<BaseRoom> rooms = new LinkedList<>();
                    for (BaseRoom room : this.gunWar.getGameRoomManager().getCanJoinGameRoomList(modeName)) {
                        if (room.canJoin(player)) {
                            if (room.getPlayers().size() > 0) {
                                room.joinRoom(player);
                                sender.sendMessage(this.language.translateString("joinRandomRoom"));
                                return true;
                            }
                            rooms.add(room);
                        }
                    }
                    if (rooms.size() > 0) {
                        BaseRoom room = rooms.get(GunWar.RANDOM.nextInt(rooms.size()));
                        room.joinRoom(player);
                        sender.sendMessage(this.language.translateString("joinRandomRoom"));
                        return true;
                    }
                    sender.sendMessage(this.language.translateString("joinRoomIsNotFound"));
                    return true;
                }else if (this.gunWar.getGameRoomManager().hasGameRoom(args[1])) {
                    BaseRoom room = this.gunWar.getGameRoomManager().getGameRoom(args[1]);
                    if (room.getStatus() != 0 && room.getStatus() != 1) {
                        sender.sendMessage(this.language.translateString("joinRoomIsPlaying"));
                    } else if (room.getPlayers().size() >= room.getMaxPlayers()) {
                        sender.sendMessage(this.language.translateString("joinRoomIsFull"));
                    } else {
                        room.joinRoom(player);
                    }
                    return true;
                } else {
                    sender.sendMessage(this.language.translateString("joinRoomIsNotFound"));
                    return true;
                }
            }
        }
        sender.sendMessage(this.language.translateString("joinRoomNotAvailable"));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("roomName", CommandParamType.TEXT) };
    }


}
