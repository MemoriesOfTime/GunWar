package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
@Deprecated
public class SetVictoryScore extends BaseSubCommand {

    public SetVictoryScore(String name) {
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
        if (args.length == 2) {
            if (args[1].matches("[0-9]*")) {
                Player player = (Player) sender;
                Config config = this.gunWar.getRoomConfig(player.getLevel());
                config.set("victoryScore", Integer.parseInt(args[1]));
                config.save();
                sender.sendMessage(this.language.adminSetVictoryScore.replace("%score%", args[1]));
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
        return new CommandParameter[] { new CommandParameter("victoryScore", CommandParamType.INT, false) };
    }

}
