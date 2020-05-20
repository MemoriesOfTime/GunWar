package cn.lanink.gunwar.command;

import cn.lanink.gunwar.command.adminsub.*;
import cn.lanink.gunwar.command.base.BaseCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class AdminCommand extends BaseCommand {

    public AdminCommand(String name) {
        super(name, "GunWar 管理命令");
        this.setPermission("GunWar.command.admin");
        this.addSubCommand(new UiCommand("ui"));
        this.addSubCommand(new SetWaitSpawnCommand("setwaitspawn"));
        this.addSubCommand(new SetRedSpawnCommand("setredspawn"));
        this.addSubCommand(new SetBlueSpawnCommand("setbluespawn"));
        this.addSubCommand(new SetWaitTimeCommand("setwaittime"));
        this.addSubCommand(new SetGameTimeCommand("setgametime"));
        this.addSubCommand(new ReloadCommand("reloadroom"));
        this.addSubCommand(new UnloadCommand("unloadroom"));
        this.loadCommandBase();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(this.getPermission());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            return super.execute(sender, label, args);
        }else {
            if(args.length > 0 && args[0].equals("reload")) {
                this.gunWar.reLoadRooms();
                sender.sendMessage(this.language.adminReload);
                return true;
            }else if(args.length > 0 && args[0].equals("unload")) {
                this.gunWar.unloadRooms();
                sender.sendMessage(this.language.adminUnload);
                return true;
            }else {
                sender.sendMessage(this.language.useCmdInCon);
            }
            return true;
        }
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.language.adminHelp.replace("%cmdName%", this.getName()));
    }

}
