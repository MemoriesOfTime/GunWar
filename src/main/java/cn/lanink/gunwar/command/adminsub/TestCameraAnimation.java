package cn.lanink.gunwar.command.adminsub;

import cn.lanink.gunwar.command.base.BaseSubCommand;
import cn.lanink.gunwar.room.action.ActionModeRoom;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

/**
 * 测试摄像机动画命令
 * 用于验证行动模式的开场动画和加时赛动画效果
 *
 * @author Claude
 */
public class TestCameraAnimation extends BaseSubCommand {

    public TestCameraAnimation(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[]{"testcamera", "testcam"};
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        // 解析动画类型参数
        String animationType = "opening";  // 默认为开场动画
        if (args.length > 1) {
            String type = args[1].toLowerCase();
            if (type.equals("opening") || type.equals("start") || type.equals("开场")) {
                animationType = "opening";
            } else if (type.equals("overtime") || type.equals("ot") || type.equals("加时") || type.equals("最后攻势") || type.equals("最后防线")) {
                animationType = "overtime";
            } else {
                sender.sendMessage("§c错误: 未知的动画类型 '" + args[1] + "'");
                sender.sendMessage("§e可用类型:");
                sender.sendMessage("  §7- §aopening §7(开场动画)");
                sender.sendMessage("  §7- §aovertime §7(加时赛动画/最后攻势/最后防线)");
                return true;
            }
        }

        // 获取房间
        BaseRoom room;
        if (args.length > 2) {
            // 如果提供了房间名参数，尝试获取指定房间
            String roomName = args[2];
            room = this.gunWar.getGameRoomManager().getGameRoom(roomName);
            if (room == null) {
                sender.sendMessage("§c错误: 找不到房间 '" + roomName + "'");
                return true;
            }
        } else {
            // 否则使用当前所在房间
            room = this.gunWar.getGameRoomManager().getGameRoom(player.getLevel().getFolderName());
            if (room == null) {
                sender.sendMessage("§c错误: 当前地图没有游戏房间");
                sender.sendMessage("§e用法: /" + label + " testcameraanimation [类型] [房间名]");
                sender.sendMessage("§7类型: opening(开场) 或 overtime(加时赛)");
                return true;
            }
        }

        // 检查是否为行动模式房间
        if (!(room instanceof ActionModeRoom)) {
            sender.sendMessage("§c错误: 该房间不是行动模式房间，不支持摄像机动画");
            sender.sendMessage("§e提示: 只有行动模式(ActionMode)房间才支持摄像机动画");
            return true;
        }

        ActionModeRoom actionRoom = (ActionModeRoom) room;

        // 检查是否有配置的区域
        if (actionRoom.getZones().isEmpty()) {
            sender.sendMessage("§c错误: 该房间没有配置任何区域，无法生成摄像机动画");
            sender.sendMessage("§e提示: 请先使用 /gunwaradmin setroom 配置区域和控制点");
            return true;
        }

        // 根据类型播放不同的动画
        try {
            if (animationType.equals("opening")) {
                // 播放开场动画
                if (!actionRoom.isEnableCameraAnimation()) {
                    sender.sendMessage("§6警告: 该房间未启用摄像机动画 (enableCameraAnimation: false)");
                    sender.sendMessage("§e仍将尝试播放动画...");
                }

                sender.sendMessage("§a开始播放开场动画...");
                sender.sendMessage("§7房间: §e" + room.getLevelName());
                sender.sendMessage("§7区域数: §e" + actionRoom.getZones().size());

                actionRoom.testCameraAnimation(player);
                sender.sendMessage("§a开场动画已启动！");

            } else if (animationType.equals("overtime")) {
                // 播放加时赛动画
                if (!actionRoom.isEnableOvertime()) {
                    sender.sendMessage("§6警告: 该房间未启用加时赛 (enableOvertime: false)");
                    sender.sendMessage("§e仍将尝试播放动画...");
                }

                sender.sendMessage("§a开始播放加时赛动画...");
                sender.sendMessage("§7房间: §e" + room.getLevelName());
                sender.sendMessage("§7你的队伍: §e" + (actionRoom.getPlayerTeam(player) == null ? "未分配" :
                    (actionRoom.getPlayerTeam(player).name().equals("RED") ? "§c进攻方" : "§9防守方")));

                actionRoom.testOvertimeAnimation(player);
                sender.sendMessage("§a加时赛动画已启动！");
            }
        } catch (Exception e) {
            sender.sendMessage("§c动画播放失败: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[]{
            CommandParameter.newEnum("类型", true, new String[]{"opening", "overtime"}),
            CommandParameter.newType("房间名", true, CommandParamType.STRING)
        };
    }

}
