package cn.lanink.gunwar.command.usersub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class ListCommand extends BaseSubCommand {

    public ListCommand(String name) {
        super(name);
    }

    @Override
    public String[] getAliases() {
        return new String[] { "列表" };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        StringBuilder list = new StringBuilder();
        for (String string : this.gunWar.getRooms().keySet()) {
            list.append(string).append(" ");
        }
        sender.sendMessage(this.language.listRoom.replace("%list%", String.valueOf(list)));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
