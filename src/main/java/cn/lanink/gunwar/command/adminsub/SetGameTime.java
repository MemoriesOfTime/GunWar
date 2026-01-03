package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

/**
 * 设置游戏时间命令
 *
 * @author LT_Name
 */
public class SetGameTime extends BaseSubCommand {

    public SetGameTime(String name) {
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
        BaseRoom room = this.gunWar.getGameRoomManager().getGameRoom(player.getLevel().getFolderName());

        if (room == null) {
            sender.sendMessage("§c当前世界不是游戏房间！");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§c用法: /gunwaradmin setgametime <时间(秒)>");
            sender.sendMessage("§e当前游戏时间: §6" + room.gameTime + " §e秒");
            return true;
        }

        try {
            int newTime = Integer.parseInt(args[1]);
            if (newTime < 0) {
                sender.sendMessage("§c时间不能为负数！");
                return true;
            }

            room.gameTime = newTime;
            sender.sendMessage("§a成功设置游戏时间为: §6" + newTime + " §a秒");
        } catch (NumberFormatException e) {
            sender.sendMessage("§c时间必须是整数！");
        }

        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[]{
            new CommandParameter("time", CommandParamType.INT, false)
        };
    }

}
