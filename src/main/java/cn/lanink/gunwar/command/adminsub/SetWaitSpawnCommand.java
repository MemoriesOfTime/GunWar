package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class SetWaitSpawnCommand extends BaseSubCommand {

    public SetWaitSpawnCommand(String name) {
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
        this.gunWar.roomSetWaitSpawn(player, this.gunWar.getRoomConfig(player.getLevel()));
        sender.sendMessage(this.language.adminSetWaitSpawn);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
