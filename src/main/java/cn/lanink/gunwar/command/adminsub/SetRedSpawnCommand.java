package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

public class SetRedSpawnCommand extends BaseSubCommand {

    public SetRedSpawnCommand(String name) {
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
        String spawn = player.getFloorX() + ":" + player.getFloorY() + ":" + player.getFloorZ();
        Config config = this.gunWar.getRoomConfig(player.getLevel());
        config.set("redSpawn", spawn);
        config.save();
        sender.sendMessage(this.language.adminSetRedSpawn);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
