package cn.lanink.gunwar.command.adminsub;

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
        if (this.gunWar.createRoomSchedule.containsKey(player)) {
            sender.sendMessage("你已经进入创建房间状态了");
        }else {
            this.gunWar.createRoomSchedule.put(player, 10);
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.gunWar,
                    new CreateRoomTask(this.gunWar, player), 10);
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
