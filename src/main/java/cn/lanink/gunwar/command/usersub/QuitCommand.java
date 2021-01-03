package cn.lanink.gunwar.command.usersub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.room.base.BaseRoom;
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
        for (BaseRoom room : this.gunWar.getRooms().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player);
                return true;
            }
        }
        sender.sendMessage(this.languageOld.quitRoomNotInRoom);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
