package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

/**
 * @author lt_name
 */
public class StartRoom extends BaseSubCommand {

    public StartRoom(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        BaseRoom room = this.gunWar.getRooms().get(player.getLevel().getFolderName());
        if (room != null) {
            if (room.getPlayers().size() >= 2) {
                if (room.getStatus() == 1) {
                    room.startGame();
                    sender.sendMessage(this.language.translateString("adminStartRoom"));
                }else {
                    sender.sendMessage(this.language.translateString("adminStartRoomIsPlaying"));
                }
            }else {
                sender.sendMessage(this.language.translateString("adminStartRoomNoPlayer"));
            }
        }else {
            sender.sendMessage(this.language.translateString("adminLevelNoRoom"));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
