package cn.lanink.gunwar.command;

import cn.lanink.gunwar.command.base.BaseCommand;
import cn.lanink.gunwar.command.usersub.JoinCommand;
import cn.lanink.gunwar.command.usersub.ListCommand;
import cn.lanink.gunwar.command.usersub.QuitCommand;
import cn.lanink.gunwar.command.usersub.UiCommand;
import cn.nukkit.command.CommandSender;

public class UserCommand extends BaseCommand {

    public UserCommand(String name) {
        super(name, "GunWar 命令");
        this.setPermission("GunWar.command.user");
        this.addSubCommand(new UiCommand("ui"));
        this.addSubCommand(new JoinCommand("join"));
        this.addSubCommand(new QuitCommand("quit"));
        this.addSubCommand(new ListCommand("list"));
        this.loadCommandBase();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(this.getPermission());
    }

    @Override
    public boolean execute(CommandSender commandSender, String label, String[] args) {
        return super.execute(commandSender, label, args);
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.language.userHelp.replace("%cmdName%", this.getName()));
    }

}
