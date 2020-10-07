package cn.lanink.gunwar.command;

import cn.lanink.gunwar.command.adminsub.*;
import cn.lanink.gunwar.command.base.BaseCommand;
import cn.lanink.gunwar.ui.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class AdminCommand extends BaseCommand {

    public AdminCommand(String name) {
        super(name, "GunWar 管理命令");
        this.setPermission("GunWar.command.admin");
        //游戏内使用命令不区分大小写！
        this.addSubCommand(new CreateRoom("CreateRoom"));
        this.addSubCommand(new SetWaitSpawnCommand("setwaitspawn"));
        this.addSubCommand(new SetRedSpawnCommand("setredspawn"));
        this.addSubCommand(new SetBlueSpawnCommand("setbluespawn"));
        this.addSubCommand(new SetWaitTimeCommand("setwaittime"));
        this.addSubCommand(new SetGameTimeCommand("setgametime"));
        this.addSubCommand(new SetVictoryScore("setvictoryscore"));
        this.addSubCommand(new SetMinPlayers("setminplayers"));
        this.addSubCommand(new SetMaxPlayers("setmaxplayers"));
        this.addSubCommand(new SetGameMode("setgamemode"));
        this.addSubCommand(new StartRoom("startroom"));
        this.addSubCommand(new StopRoom("stoproom"));
        this.addSubCommand(new ReloadCommand("reloadroom"));
        this.addSubCommand(new UnloadCommand("unloadroom"));
        this.addSubCommand(new AddWeapon("AddWeapon"));
        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.language.adminHelp.replace("%cmdName%", this.getName()));
    }

    @Override
    public void sendUI(CommandSender sender) {
        GuiCreate.sendAdminMenu((Player) sender);
    }

}
