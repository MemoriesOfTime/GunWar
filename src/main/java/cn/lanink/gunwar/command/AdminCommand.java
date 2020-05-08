package cn.lanink.gunwar.command;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.ui.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class AdminCommand extends Command {

    private final GunWar gunWar = GunWar.getInstance();
    private final String name;

    public AdminCommand(String name) {
        super(name, "GunWar 管理命令", "/" + name + " help");
        this.name = name;
        this.setPermission("GunWar.op");
        this.setPermissionMessage("§c你没有权限");
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
                            commandSender.sendMessage("§a等待点设置成功！");
                            return true;
                        case "setredspawn":
                            gunWar.roomSetRedSpawn(player, gunWar.getRoomConfig(player.getLevel()));
                            commandSender.sendMessage("§a红队出生点设置成功！");
                            return true;
                        case "setbluespawn":
                            gunWar.roomSetBlueSpawn(player, gunWar.getRoomConfig(player.getLevel()));
                            commandSender.sendMessage("§a蓝队出生点设置成功！");
                            return true;
                        case "setwaittime":
                            if (strings.length == 2) {
                                if (strings[1].matches("[0-9]*")) {
                                    gunWar.roomSetWaitTime(Integer.valueOf(strings[1]), gunWar.getRoomConfig(player.getLevel()));
                                    commandSender.sendMessage("§a等待时间已设置为：" + Integer.valueOf(strings[1]));
                                }else {
                                    commandSender.sendMessage("§a时间只能设置为正整数！");
                                }
                            }else {
                                commandSender.sendMessage("§a查看帮助：/" + name + " help");
                            }
                            return true;
                        case "setgametime":
                            if (strings.length == 2) {
                                if (strings[1].matches("[0-9]*")) {
                                    if (Integer.parseInt(strings[1]) > 60) {
                                        gunWar.roomSetGameTime(Integer.valueOf(strings[1]), gunWar.getRoomConfig(player.getLevel()));
                                        commandSender.sendMessage("§a游戏时间已设置为：" + Integer.valueOf(strings[1]));
                                    } else {
                                        commandSender.sendMessage("§a游戏时间最小不能低于1分钟！");
                                    }
                                }else {
                                    commandSender.sendMessage("§a时间只能设置为正整数！");
                                }
                            }else {
                                commandSender.sendMessage("§a查看帮助：/" + name + " help");
                            }
                            return true;
                        case "reload": case "重载":
                            gunWar.reLoadRooms();
                            commandSender.sendMessage("§a配置重载完成！请在后台查看信息！");
                            return true;
                        case "unload":
                            gunWar.unloadRooms();
                            commandSender.sendMessage("§a已卸载所有房间！请在后台查看信息！");
                            return true;
                        default:
                            commandSender.sendMessage("§eSnowballWar--命令帮助");
                            commandSender.sendMessage("§a/" + name + " §e打开ui");
                            commandSender.sendMessage("§a/" + name + " setwaitspawn §e设置当前位置为等待点");
                            commandSender.sendMessage("§a/" + name + " setredspawn §e将当前位置设置为红队出生点");
                            commandSender.sendMessage("§a/" + name + " setbluespawn §e将当前位置设置为蓝队出生点");
                            commandSender.sendMessage("§a/" + name + " setwaittime 数字 §e设置游戏人数足够后的等待时间");
                            commandSender.sendMessage("§a/" + name + " setgametime 数字 §e设置每轮游戏最长时间");
                            commandSender.sendMessage("§a/" + name + " reload §e重载所有房间");
                            commandSender.sendMessage("§a/" + name + " unload §e关闭所有房间,卸载配置");
                            return true;
                    }
                }else {
                    GuiCreate.sendAdminMenu(player);
                    return true;
                }
            }else {
                commandSender.sendMessage("§c你没有权限！");
                return true;
            }
        }else {
            if(strings.length > 0 && strings[0].equals("reload")) {
                gunWar.reLoadRooms();
                commandSender.sendMessage("§a配置重载完成！");
                return true;
            }else if(strings.length > 0 && strings[0].equals("unload")) {
                gunWar.unloadRooms();
                commandSender.sendMessage("§a已卸载所有房间！");
                return true;
            }else {
                commandSender.sendMessage("§a请在游戏内输入！");
            }
            return true;
        }
    }
}
