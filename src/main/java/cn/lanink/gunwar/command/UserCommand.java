package cn.lanink.gunwar.command;

import cn.lanink.gunwar.command.base.BaseCommand;
import cn.lanink.gunwar.command.usersub.*;
import cn.nukkit.command.CommandSender;

public class UserCommand extends BaseCommand {

    public UserCommand(String name) {
        super(name, "GunWar 命令");
        this.setPermission("GunWar.command.user");
        this.addSubCommand(new UiCommand("ui"));
        this.addSubCommand(new JoinCommand("join"));
        this.addSubCommand(new QuitCommand("quit"));
        this.addSubCommand(new ListCommand("list"));
        this.addSubCommand(new RecordCommand("record"));
        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.language.userHelp.replace("%cmdName%", this.getName()));
    }

}
