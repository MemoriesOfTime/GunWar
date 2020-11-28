package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.gui.GuiCreate;
import cn.lanink.gunwar.tasks.adminroom.SetRoomTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Level;

/**
 * @author lt_name
 */
public class SetRoom extends BaseSubCommand {

    public SetRoom(String name) {
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
        if (this.gunWar.setRoomTask.containsKey(player)) {
            this.gunWar.setRoomTask.get(player).cancel();
        }else {
            if (args.length < 2) {
                GuiCreate.sendSetRoomMenu(player);
            }else {
                if (this.gunWar.getRoomConfigs().containsKey(args[1])) {
                    Level level = Server.getInstance().getLevelByName(args[1]);
                    if (player.getLevel() != level) {
                        player.teleport(level.getSafeSpawn());
                    }
                    //this.gunWar.setRoomSchedule.put(player, 10);
                    SetRoomTask task = new SetRoomTask(this.gunWar, player, level);
                    this.gunWar.setRoomTask.put(player, task);
                    Server.getInstance().getScheduler().scheduleRepeatingTask(this.gunWar, task, 10);
                    sender.sendMessage(this.language.admin_setRoom_start.replace("%name%", args[1]));
                }else {
                    sender.sendMessage(this.language.admin_setRoom_noExist);
                }
            }
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[]{ CommandParameter.newType("roomName", CommandParamType.TEXT) };
    }

}
