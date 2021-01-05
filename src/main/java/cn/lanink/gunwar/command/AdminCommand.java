package cn.lanink.gunwar.command;

import cn.lanink.gunwar.command.adminsub.*;
import cn.lanink.gunwar.command.base.BaseCommand;
import cn.lanink.gunwar.gui.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class AdminCommand extends BaseCommand {

    public AdminCommand(String name) {
        super(name, "GunWar 管理命令");
        this.setPermission("GunWar.command.admin");
        //游戏内使用命令不区分大小写！
        this.addSubCommand(new CreateRoom("CreateRoom"));
        this.addSubCommand(new SetRoom("SetRoom"));
        this.addSubCommand(new StartRoom("StartRoom"));
        this.addSubCommand(new StopRoom("StopRoom"));
        this.addSubCommand(new ReloadCommand("ReloadRoom"));
        this.addSubCommand(new UnloadCommand("UnloadRoom"));
        this.addSubCommand(new AddWeapon("AddWeapon"));
        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.language.translateString("adminHelp", this.getName()));
    }

    @Override
    public void sendUI(CommandSender sender) {
        GuiCreate.sendAdminMenu((Player) sender);
    }

}
