package cn.lanink.gunwar.utils;

import cn.nukkit.utils.Config;

public class Language {

    //命令
    public String cmdHelp = "§a查看帮助：/%cmdName% help";
    public String userHelp = "§eGunWar--命令帮助 \n " +
            "§a/%cmdName% §e打开ui \n " +
            "§a/%cmdName% join 房间名称 §e加入游戏 \n " +
            "§a/%cmdName% quit §e退出游戏 \n " +
            "§a/%cmdName% list §e查看房间列表";
    public String noPermission = "§c你没有权限！";
    public String joinRoom = "§a你已加入房间: %name%";
    public String joinRoomOnRoom = "§c你已经在一个房间中了!";
    public String joinRoomOnRiding = "§a请勿在骑乘状态下进入房间！";
    public String joinRandomRoom = "§a已为你随机分配房间！";
    public String joinRoomIsPlaying = "§a该房间正在游戏中，请稍后";
    public String joinRoomIsFull = "§a该房间已满人，请稍后";
    public String joinRoomIsNotFound = "§a该房间不存在！";
    public String joinRoomNotAvailable = "§a暂无房间可用！";
    public String quitRoom = "§a你已退出房间";
    public String quitRoomNotInRoom = "§a你本来就不在游戏房间！";
    public String listRoom = "§e房间列表： §a %list%";
    public String useCmdInCon = "请在游戏内输入！";
    public String adminHelp = "§eGunWar--命令帮助 \n " +
            "§a/%cmdName% §e打开ui \n " +
            "§a/%cmdName% setwaitspawn §e设置当前位置为等待点 \n " +
            "§a/%cmdName% setredspawn §e将当前位置设置为红队出生点 \n " +
            "§a/%cmdName% setbluespawn §e将当前位置设置为蓝队出生点 \n " +
            "§a/%cmdName% setwaittime 数字 §e设置游戏人数足够后的等待时间 \n " +
            "§a/%cmdName% setgametime 数字 §e设置每回合游戏最长时间 \n " +
            "§a/%cmdName% reload §e重载所有房间 \n " +
            "§a/%cmdName% unload §e关闭所有房间,并卸载配置";
    public String adminSetWaitSpawn = "§a等待出生点设置成功！";
    public String adminSetRedSpawn = "§a红队出生点设置成功！";
    public String adminSetBlueSpawn = "§a蓝队出生点设置成功！";
    public String adminNotNumber = "§a时间只能设置为正整数！";
    public String adminSetWaitTime = "§a等待时间已设置为：%time%";
    public String adminSetGameTime = "§a游戏时间已设置为：%time%";
    public String adminSetGameTimeShort = "§a游戏时间最小不能低于1分钟！";
    public String adminReload = "§a配置重载完成！请在后台查看信息！";
    public String adminUnload = "§a已卸载所有房间！请在后台查看信息！";
    public String roomSafeKick = "\n§c房间非正常关闭!\n为了您的背包安全，请稍后重进服务器！";
    //游戏提示
    public String teamNameRed = "§c红队";
    public String teamNameBlue = "§9蓝队";
    public String roundVictoryRed = "§c红队获得本轮胜利";
    public String roundVictoryBlue = "§9蓝队获得本轮胜利";
    public String victoryRed = "§c红队获得胜利";
    public String victoryBlue = "§9蓝队获得胜利";
    public String roundVictoryDraw = "平局";
    public String titleDeathTitle = "死亡";
    public String titleDeathSubtitle = "你被 %player% 击杀了";
    public String killMessage = "%damagePlayer% 杀死了 %%player";
    public String tpJoinRoomLevel = "§e >> §c要进入游戏地图，请先加入游戏！";
    public String tpQuitRoomLevel = "§e >> §c退出房间请使用命令！";
    public String gameArmor = "游戏中无法脱下护甲！";
    public String playerTeamChat = "§c[队伍] %player% + §b >>> %message%";
    //TipsTask
    public String scoreBoardTitle = "§eGunWar";
    public String waitTimeScoreBoard = " 玩家: §a %playerNumber%/10 \n §a开始倒计时： §l§e %time%";
    public String waitScoreBoard = " 玩家: §a %playerNumber%/10 \n 最低游戏人数为 2 人 \n 等待玩家加入中";
    public String waitTimeBottom = "§a当前已有: %playerNumber% 位玩家 \n §a游戏还有: %time% 秒开始！";
    public String waitBottom = "§c等待玩家加入中,当前已有: %playerNumber% 位玩家";
    public String gameTimeScoreBoard = "§l§a当前血量:§e %health% \n " +
            "§l§a剩余时间:§e %time% 秒 \n " +
            "§l§a队伍存活人数: \n " +
            "§l§c红:  + %red% +  人 §9蓝:  + %blue% +  人 \n " +
            "§l§a队伍胜利：\n " +
            "§l§c红:  + %redRound% +  回合 §9蓝:  + %blueRound% +  回合 ";
    public String gameTimeBottom = "§l§c血量： %health%";
    public String victoryMessage = "§e恭喜 %teamName% §e获得胜利";
    //ui相关
    public String userMenuButton1 = "§e随机加入房间";
    public String userMenuButton2 = "§e退出当前房间";
    public String userMenuButton3 = "§e查看房间列表";
    public String adminMenuSetLevel = "当前设置地图：%name%";
    public String adminMenuButton1 = "§e设置等待出生点";
    public String adminMenuButton2 = "§e设置红队出生点";
    public String adminMenuButton3 = "§e设置蓝队出生点";
    public String adminMenuButton4 = "§e设置时间参数";
    public String adminMenuButton5 = "§e重载所有房间";
    public String adminMenuButton6 = "§c卸载所有房间";
    public String adminTimeMenuInputText1 = "等待时间（秒）";
    public String adminTimeMenuInputText2 = "游戏时间（秒）";
    public String joinRoomOK = "§l§a确认要加入房间: %name% §l§a？";
    public String buttonOK = "§a确定";
    public String buttonReturn = "§c返回";

    public Language(Config config) {

    }

}