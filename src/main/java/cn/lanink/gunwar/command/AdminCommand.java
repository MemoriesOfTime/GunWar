package cn.lanink.gunwar.command;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.ui.GuiCreate;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class AdminCommand extends Command {

    private final GunWar gunWar = GunWar.getInstance();
    private final Language language = gunWar.getLanguage();
    private final String name;

    public AdminCommand(String name) {
        super(name, "GunWar 管理命令", "/" + name + " help");
        this.name = name;
        this.setPermission("GunWar.op");
        this.setPermissionMessage(language.noPermission);
    }

    @Override
    public boolean execute(CommandSender commandSender, String label, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = ((Player) commandSender).getPlayer();
            if (player.isOp()) {
                if (strings.length > 0) {
                    switch (strings[0]) {
                        case "setwaitspawn":
                            gunWar.roomSetWaitSpawn(player, gunWar.getRoomConfig(player.getLevel()));
                            commandSender.sendMessage(this.language.adminSetWaitSpawn);
                            return true;
                        case "setredspawn":
                            gunWar.roomSetRedSpawn(player, gunWar.getRoomConfig(player.getLevel()));
                            commandSender.sendMessage(this.language.adminSetRedSpawn);
                            return true;
                        case "setbluespawn":
                            gunWar.roomSetBlueSpawn(player, gunWar.getRoomConfig(player.getLevel()));
                            commandSender.sendMessage(this.language.adminSetBlueSpawn);
                            return true;
                        case "setwaittime":
                            if (strings.length == 2) {
                                if (strings[1].matches("[0-9]*")) {
                                    gunWar.roomSetWaitTime(Integer.valueOf(strings[1]), gunWar.getRoomConfig(player.getLevel()));
                                    commandSender.sendMessage(this.language.adminSetWaitTime.replace("%time%", strings[1]));
                                }else {
                                    commandSender.sendMessage(this.language.adminNotNumber);
                                }
                            }else {
                                commandSender.sendMessage(this.language.cmdHelp.replace("%cmdName%", this.name));
                            }
                            return true;
                        case "setgametime":
                            if (strings.length == 2) {
                                if (strings[1].matches("[0-9]*")) {
                                    if (Integer.parseInt(strings[1]) > 60) {
                                        gunWar.roomSetGameTime(Integer.valueOf(strings[1]), gunWar.getRoomConfig(player.getLevel()));
                                        commandSender.sendMessage(this.language.adminSetWaitTime.replace("%time%", strings[1]));
                                    } else {
                                        commandSender.sendMessage(this.language.adminSetGameTimeShort);
                                    }
                                }else {
                                    commandSender.sendMessage(this.language.adminNotNumber);
                                }
                            }else {
                                commandSender.sendMessage(this.language.cmdHelp.replace("%cmdName%", this.name));
                            }
                            return true;
                        case "reload": case "重载":
                            gunWar.reLoadRooms();
                            commandSender.sendMessage(this.language.adminReload);
                            return true;
                        case "unload":
                            gunWar.unloadRooms();
                            commandSender.sendMessage(this.language.adminUnload);
                            return true;
                        default:
                            commandSender.sendMessage(
                                    this.language.adminHelp.replace("%cmdName%", this.name));
                            return true;
                    }
                }else {
                    GuiCreate.sendAdminMenu(player);
                    return true;
                }
            }else {
                commandSender.sendMessage(this.language.noPermission);
                return true;
            }
        }else {
            if(strings.length > 0 && strings[0].equals("reload")) {
                gunWar.reLoadRooms();
                commandSender.sendMessage(this.language.adminReload);
                return true;
            }else if(strings.length > 0 && strings[0].equals("unload")) {
                gunWar.unloadRooms();
                commandSender.sendMessage(this.language.adminUnload);
                return true;
            }else {
                commandSender.sendMessage(this.language.useCmdInCon);
            }
            return true;
        }
    }
}
