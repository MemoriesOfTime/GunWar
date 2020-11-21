package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gamecore.utils.FileUtil;
import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.tasks.CreateRoomTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

/**
 * @author lt_name
 */
public class CreateRoom extends BaseSubCommand {

    public CreateRoom(String name) {
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
        if (!this.gunWar.getRooms().containsKey(player.getLevel().getFolderName())) {
            if (this.gunWar.createRoomSchedule.containsKey(player)) {
                this.gunWar.createRoomSchedule.remove(player);
                sender.sendMessage(this.language.admin_createRoom_cancel);
            }else {
                this.gunWar.getRoomConfigs().remove(player.getLevel().getFolderName());
                FileUtil.deleteFile(this.gunWar.getDataFolder() + "/Rooms/" + player.getLevel().getFolderName() + ".yml");
                this.gunWar.createRoomSchedule.put(player, 10);
                Server.getInstance().getScheduler().scheduleRepeatingTask(this.gunWar,
                        new CreateRoomTask(this.gunWar, player), 10);
            }
        }else {
            sender.sendMessage(this.language.admin_createRoom_exist);
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
