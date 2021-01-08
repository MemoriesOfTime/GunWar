package cn.lanink.gunwar.command.usersub;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.room.base.BaseRoom;
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
        if (this.gunWar.getRooms().size() > 0) {
            if (player.riding != null) {
                sender.sendMessage(this.language.translateString("joinRoomIsRiding"));
                return true;
            }
            for (BaseRoom room : this.gunWar.getRooms().values()) {
                if (room.isPlaying(player)) {
                    sender.sendMessage(this.language.translateString("joinRoomIsInRoom"));
                    return true;
                }
            }
            if (args.length < 2) {
                LinkedList<BaseRoom> rooms = new LinkedList<>();
                for (BaseRoom room : this.gunWar.getRooms().values()) {
                    if (room.canJoin()) {
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
            }else {
                String[] s = args[1].split(":");
                if (s.length == 2 && s[0].toLowerCase().trim().equals("mode")) {
                    String modeName = s[1].toLowerCase().trim();
                    LinkedList<BaseRoom> rooms = new LinkedList<>();
                    for (BaseRoom room : this.gunWar.getRooms().values()) {
                        if (room.canJoin() && room.getGameMode().equals(modeName)) {
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
                }else if (this.gunWar.getRooms().containsKey(args[1])) {
                    BaseRoom room = this.gunWar.getRooms().get(args[1]);
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
