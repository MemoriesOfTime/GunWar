package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class SetGameTimeCommand extends BaseSubCommand {

    public SetGameTimeCommand(String name) {
        super(name);
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 2) {
            if (args[1].matches("[0-9]*")) {
                if (Integer.parseInt(args[1]) > 60) {
                    this.gunWar.roomSetGameTime(Integer.parseInt(args[1]), this.gunWar.getRoomConfig(player.getLevel()));
                    sender.sendMessage(this.language.adminSetGameTime.replace("%time%", args[1]));
                } else {
                    sender.sendMessage(this.language.adminSetGameTimeShort);
                }
            }else {
                sender.sendMessage(this.language.adminNotNumber);
            }
        }else {
            sender.sendMessage(this.language.cmdHelp.replace("%cmdName%", this.getName()));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { new CommandParameter("time", CommandParamType.INT, false) };
    }

}
