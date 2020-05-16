package cn.lanink.gunwar.command;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.ui.GuiCreate;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class UserCommand extends Command {

    private final GunWar gunWar = GunWar.getInstance();
    private final Language language = gunWar.getLanguage();
    private final String name;

    public UserCommand(String name) {
        super(name, "GunWar 命令", "/" + name + " help");
        this.name = name;
    }

    @Override
    public boolean execute(CommandSender commandSender, String label, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = ((Player) commandSender).getPlayer();
            if (strings.length > 0) {
                switch (strings[0]) {
                    case "join":
                    case "加入":
                        if (gunWar.getRooms().size() > 0) {
                            for (Room room : gunWar.getRooms().values()) {
                                if (room.isPlaying(player)) {
                                    commandSender.sendMessage(this.language.joinRoomIsInRoom);
                                    return true;
                                }
                            }
                            if (player.riding != null) {
                                commandSender.sendMessage(this.language.joinRoomIsRiding);
                                return true;
                            }
                            if (strings.length < 2) {
                                for (Room room : gunWar.getRooms().values()) {
                                    if (room.getMode() == 0 || room.getMode() == 1) {
                                        room.joinRoom(player);
                                        commandSender.sendMessage(this.language.joinRandomRoom);
                                        return true;
                                    }
                                }
                            } else if (gunWar.getRooms().containsKey(strings[1])) {
                                Room room = gunWar.getRooms().get(strings[1]);
                                if (room.getMode() == 2 || room.getMode() == 3) {
                                    commandSender.sendMessage(this.language.joinRoomIsPlaying);
                                } else if (room.getPlayers().values().size() > 10) {
                                    commandSender.sendMessage(this.language.joinRoomIsFull);
                                } else {
                                    room.joinRoom(player);
                                }
                                return true;
                            } else {
                                commandSender.sendMessage(this.language.joinRoomIsNotFound);
                                return true;
                            }
                        }
                        commandSender.sendMessage(this.language.joinRoomNotAvailable);
                        return true;
                    case "quit":
                    case "退出":
                        for (Room room : gunWar.getRooms().values()) {
                            if (room.isPlaying(player)) {
                                room.quitRoom(player, true);
                                return true;
                            }
                        }
                        commandSender.sendMessage(this.language.quitRoomNotInRoom);
                        return true;
                    case "list":
                    case "列表":
                        StringBuilder list = new StringBuilder();
                        for (String string : gunWar.getRooms().keySet()) {
                            list.append(string).append(" ");
                        }
                        commandSender.sendMessage(this.language.listRoom.replace("%list%", String.valueOf(list)));
                        return true;
                    default:
                        commandSender.sendMessage(
                                this.language.userHelp.replace("%cmdName%", this.name));
                        return true;
                }
            }else {
                GuiCreate.sendUserMenu(player);
                return true;
            }
        }else {
            commandSender.sendMessage(this.language.useCmdInCon);
            return true;
        }
    }

}
