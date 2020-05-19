package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class SetBlueSpawnCommand extends BaseSubCommand {

    public SetBlueSpawnCommand(String name) {
        super(name);
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        this.gunWar.roomSetBlueSpawn(player,this.gunWar.getRoomConfig(player.getLevel()));
        sender.sendMessage(this.language.adminSetBlueSpawn);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
