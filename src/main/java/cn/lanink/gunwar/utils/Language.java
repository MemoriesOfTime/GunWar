package cn.lanink.gunwar.utils;

import cn.nukkit.utils.Config;

public class Language {

    //命令
    public String useCmdInRoom = "§e >> §c游戏中无法使用其他命令";
    public String cmdHelp = "§a查看帮助：/%cmdName% help";
    public String userHelp = "§eGunWar--命令帮助 \n" +
            "§a/%cmdName% §e打开ui(仅游戏内可用) \n" +
            "§a/%cmdName% join 房间名称 §e加入游戏 \n" +
            "§a/%cmdName% quit §e退出游戏 \n" +
            "§a/%cmdName% list §e查看房间列表 \n" +
            "§a/%cmdName% record §e查看战绩(排行榜)";
    public String noPermission = "§c你没有权限使用这个命令！";
    public String joinRoom = "§a你已加入房间: %name%";
    public String joinRoomIsInRoom = "§c你已经在一个房间中了!";
    public String joinRoomIsRiding = "§a请勿在骑乘状态下进入房间！";
    public String joinRandomRoom = "§a已为你随机分配房间！";
    public String joinRoomIsPlaying = "§a该房间正在游戏中，请稍后";
    public String joinRoomIsFull = "§a该房间已满人，请稍后";
    public String joinRoomIsNotFound = "§a暂无符合条件的房间！";
    public String joinRoomNotAvailable = "§a暂无房间可用！";
    public String quitRoom = "§a你已退出房间";
    public String quitRoomNotInRoom = "§a你本来就不在游戏房间！";
    public String listRoom = "§e房间列表： §a %list%";
    public String useCmdInCon = "请不要在控制台执行此指令!";
    public String adminHelp = "§eGunWar--命令帮助 \n" +
            "§a/%cmdName% §e打开ui(仅游戏内可用) \n" +
            "§a/%cmdName% setwaitspawn §e设置当前位置为等待点 \n" +
            "§a/%cmdName% setredspawn §e将当前位置设置为红队出生点 \n" +
            "§a/%cmdName% setbluespawn §e将当前位置设置为蓝队出生点 \n" +
            "§a/%cmdName% setwaittime 数字 §e设置游戏人数足够后的等待时间 \n" +
            "§a/%cmdName% setgametime 数字 §e设置每回合游戏最长时间 \n" +
            "§a/%cmdName% setVictoryScore 数字 §e设置胜利所需分数 \n" +
            "§a/%cmdName% setgamemode 数字 §e设置房间模式 \n" +
            "§a/%cmdName% startroom §e开始所在地图的房间游戏 \n" +
            "§a/%cmdName% stoproom §e强制关闭所在地图的房间 \n" +
            "§a/%cmdName% reloadroom §e重载所有房间 \n" +
            "§a/%cmdName% unloadroom §e关闭所有房间,并卸载配置";
    public String adminSetWaitSpawn = "§a等待出生点设置成功！";
    public String adminSetRedSpawn = "§a红队出生点设置成功！";
    public String adminSetBlueSpawn = "§a蓝队出生点设置成功！";
    public String adminNotNumber = "§a输入的参数不是数字！";
    public String adminSetWaitTime = "§a等待时间已设置为：%time%";
    public String adminSetGameTime = "§a游戏时间已设置为：%time%";
    public String adminSetGameTimeShort = "§a游戏时间最小不能低于1分钟！";
    public String adminSetVictoryScore = "§a胜利分数已设置为: %score%";
    public String adminSetGameMode = "§a房间模式已设置为: %roomMode%";
    public String adminStartRoom = "§a已强制开启游戏！";
    public String adminStartRoomNoPlayer = "§a房间人数不足两人,无法开始游戏！";
    public String adminStartRoomIsPlaying = "§c房间已经开始了！";
    public String adminLevelNoRoom = "§c当前地图不是游戏房间！";
    public String adminStopRoom = "§a已强制结束房间！";
    public String adminReload = "§a配置重载完成！请在后台查看信息！";
    public String adminUnload = "§a已卸载所有房间！请在后台查看信息！";
    //房间模式
    public String classic = "经典";
    public String captureTheFlag = "夺旗";
    //游戏提示
    public String playerTeamSelect = "你已选择队伍";
    public String roomSafeKick = "\n§c房间非正常关闭!\n为了您的背包安全，请稍后重进服务器！";
    public String noTeamSelect = "未选择队伍";
    public String teamNameRed = "§c红队";
    public String teamNameBlue = "§9蓝队";
    public String roundVictoryRed = "§c红队获得本轮胜利";
    public String roundVictoryBlue = "§9蓝队获得本轮胜利";
    public String victoryRed = "§c红队获得胜利";
    public String victoryBlue = "§9蓝队获得胜利";
    public String roundVictoryDraw = "平局";
    public String titleDeathTitle = "死亡";
    public String titleDeathSubtitle = "你被 %player% 击杀了";
    public String killMessage = "§e >> §c%damagePlayer% 杀死了 %player%";
    public String suicideMessage = "§e >> §c%player% 自杀了";
    public String tpJoinRoomLevel = "§e >> §c要进入游戏地图，请先加入游戏！";
    public String tpQuitRoomLevel = "§e >> §c退出房间请使用命令！";
    public String gameArmor = "§e >> §c游戏中无法脱下护甲！";
    public String playerTeamChat = "§a[队伍]§f %player% §b >>> %message%";
    //ScoreBoardTask
    public String scoreBoardTitle = "§eGunWar";
    public String waitTimeScoreBoard = " 所属队伍: %team% \n 玩家: §a%playerNumber%/10 \n §a开始倒计时: §e%time% ";
    public String waitScoreBoard = " 所属队伍: %team% \n 玩家: §a%playerNumber%/10 \n 最低游戏人数为 2 人 \n 等待玩家加入中 ";
    public String waitTimeBottom = "§a当前已有: %playerNumber% 位玩家 \n §a游戏还有: %time% 秒开始！";
    public String waitBottom = "§c等待玩家加入中,当前已有: %playerNumber% 位玩家";
    public String gameTimeScoreBoard = " §l§a所属队伍: %team% \n " +
            "§l§a当前血量:§e %health% \n " +
            "§l§a剩余时间:§e %time% 秒 \n " +
            "§l§a队伍存活人数: \n " +
            "§l§c红: %red% 人 §9蓝: %blue% 人 \n " +
            "§l§a队伍胜利：\n " +
            "§l§c红: %redRound% 回合 §9蓝: %blueRound% 回合 ";
    public String gameTimeBottom = "§l§c血量： %health%";
    public String gameTimeRespawnBottom = "§l§c复活倒计时: %time%";
    public String victoryMessage = "§e恭喜 %teamName% §e获得胜利";
    //ui相关
    public String userMenuButton1 = "§e随机加入房间";
    public String userMenuButton2 = "§e退出当前房间";
    public String userMenuButton3 = "§e查看房间列表";
    public String userMenuButton4 = "§e查看战绩排行榜";
    public String adminMenuSetLevel = "当前设置地图：%name%";
    public String adminMenuButton1 = "§e设置等待出生点";
    public String adminMenuButton2 = "§e设置红队出生点";
    public String adminMenuButton3 = "§e设置蓝队出生点";
    public String adminMenuButton4 = "§e设置更多参数";
    public String adminMenuButton5 = "§e设置房间模式";
    public String adminMenuButton6 = "§e重载所有房间";
    public String adminMenuButton7 = "§c卸载所有房间";
    public String adminTimeMenuInputText1 = "等待时间（秒）";
    public String adminTimeMenuInputText2 = "游戏时间（秒）";
    public String adminTimeMenuInputText3 = "胜利所需分数";
    public String joinRoomOK = "§l§a确认要加入房间: %name% §l§a？";
    public String buttonOK = "§a确定";
    public String buttonReturn = "§c返回";
    public String recordListButton1 = "§e查看个人战绩";
    public String recordListButton2 = "§e查看击杀排行榜";
    public String recordListButton3 = "§e查看死亡排行榜";
    public String recordListButton4 = "§e查看胜利排行榜";
    public String recordListButton5 = "§e查看失败排行榜";
    public String killsRanking = "击杀排行榜： ";
    public String deathsRanking = "死亡排行榜： ";
    public String victoryRanking = "胜利次数排行榜： ";
    public String defeatRanking = "失败次数排行榜： ";
    public String ranking = "Top%ranking% 玩家: %player% 次数: %number%";
    public String playerGameRecord = " 击杀数: %kills% \n 死亡数: %deaths% \n 胜利次数: %victory% \n 失败次数: %defeat%";
    //物品
    public String itemQuitRoom = "§c退出房间";
    public String itemQuitRoomLore = "手持点击,即可退出房间";
    public String itemTeamSelectRed = "§c选择红队";
    public String itemTeamSelectBlue = "§9选择蓝队";
    public String itemGrenade = "§a手榴弹";
    public String itemGrenadeLore = "伤害: 2-10 \n 伤害半径: 5";
    public String itemFlashBang = "§a闪光弹";
    public String itemFlashBangLore = "造成短暂失明效果 \n 有效半径: 5";

    public Language(Config config) {
        this.useCmdInRoom = config.getString("useCmdInRoom", this.useCmdInRoom);
        this.cmdHelp = config.getString("cmdHelp", this.cmdHelp);
        this.userHelp = config.getString("userHelp", this.userHelp);
        this.noPermission = config.getString("noPermission", this.noPermission);
        this.joinRoom = config.getString("joinRoom", this.joinRoom);
        this.joinRoomIsInRoom = config.getString("joinRoomIsInRoom", this.joinRoomIsInRoom);
        this.joinRoomIsRiding = config.getString("joinRoomIsRiding", this.joinRoomIsRiding);
        this.joinRandomRoom = config.getString("joinRandomRoom", this.joinRandomRoom);
        this.joinRoomIsPlaying = config.getString("joinRoomIsPlaying", this.joinRoomIsPlaying);
        this.joinRoomIsFull = config.getString("joinRoomIsFull", this.joinRoomIsFull);
        this.joinRoomIsNotFound = config.getString("joinRoomIsNotFound", this.joinRoomIsNotFound);
        this.joinRoomNotAvailable = config.getString("joinRoomNotAvailable", this.joinRoomNotAvailable);
        this.quitRoom = config.getString("quitRoom", this.quitRoom);
        this.quitRoomNotInRoom = config.getString("quitRoomNotInRoom", this.quitRoomNotInRoom);
        this.listRoom = config.getString("listRoom", this.listRoom);
        this.useCmdInCon = config.getString("useCmdInCon", this.useCmdInCon);
        this.adminHelp = config.getString("adminHelp", this.adminHelp);
        this.adminSetWaitSpawn = config.getString("adminSetWaitSpawn", this.adminSetWaitSpawn);
        this.adminSetRedSpawn = config.getString("adminSetRedSpawn", this.adminSetRedSpawn);
        this.adminSetBlueSpawn = config.getString("adminSetBlueSpawn", this.adminSetBlueSpawn);
        this.adminNotNumber = config.getString("adminNotNumber", this.adminNotNumber);
        this.adminSetWaitTime = config.getString("adminSetWaitTime", this.adminSetWaitTime);
        this.adminSetGameTime = config.getString("adminSetGameTime", this.adminSetGameTime);
        this.adminSetGameTimeShort = config.getString("adminSetGameTimeShort", this.adminSetGameTimeShort);
        this.adminSetVictoryScore = config.getString("adminSetVictoryScore", this.adminSetVictoryScore);
        this.adminSetGameMode = config.getString("adminSetGameMode", this.adminSetGameMode);
        this.adminStartRoom = config.getString("adminStartRoom", this.adminStartRoom);
        this.adminStartRoomNoPlayer = config.getString("adminStartRoomNoPlayer", this.adminStartRoomNoPlayer);
        this.adminStartRoomIsPlaying = config.getString("adminStartRoomIsPlaying", this.adminStartRoomIsPlaying);
        this.adminLevelNoRoom = config.getString("adminLevelNoRoom", this.adminLevelNoRoom);
        this.adminStopRoom = config.getString("adminStopRoom", this.adminStopRoom);
        this.adminReload = config.getString("adminReload", this.adminReload);
        this.adminUnload = config.getString("adminUnload", this.adminUnload);
        //房间模式
        this.classic = config.getString("classic", this.classic);
        this.captureTheFlag = config.getString("captureTheFlag", this.captureTheFlag);
        //提示信息
        this.playerTeamSelect = config.getString("playerTeamSelect", this.playerTeamSelect);
        this.roomSafeKick = config.getString("roomSafeKick", this.roomSafeKick);
        this.noTeamSelect = config.getString("noTeamSelect", this.noTeamSelect);
        this.teamNameRed = config.getString("teamNameRed", this.teamNameRed);
        this.teamNameBlue = config.getString("teamNameBlue", this.teamNameBlue);
        this.roundVictoryRed = config.getString("roundVictoryRed", this.roundVictoryRed);
        this.roundVictoryBlue = config.getString("roundVictoryBlue", this.roundVictoryBlue);
        this.victoryRed = config.getString("victoryRed", this.victoryRed);
        this.victoryBlue = config.getString("victoryBlue", this.victoryBlue);
        this.roundVictoryDraw = config.getString("roundVictoryDraw", this.roundVictoryDraw);
        this.titleDeathTitle = config.getString("titleDeathTitle", this.titleDeathTitle);
        this.titleDeathSubtitle = config.getString("titleDeathSubtitle", this.titleDeathSubtitle);
        this.killMessage = config.getString("killMessage", this.killMessage);
        this.suicideMessage = config.getString("suicideMessage", this.suicideMessage);
        this.tpJoinRoomLevel = config.getString("tpJoinRoomLevel", this.tpJoinRoomLevel);
        this.tpQuitRoomLevel = config.getString("tpQuitRoomLevel", this.tpQuitRoomLevel);
        this.gameArmor = config.getString("gameArmor", this.gameArmor);
        this.playerTeamChat = config.getString("playerTeamChat", this.playerTeamChat);
        //tips
        this.scoreBoardTitle = config.getString("scoreBoardTitle", this.scoreBoardTitle);
        this.waitTimeScoreBoard = config.getString("waitTimeScoreBoard", this.waitTimeScoreBoard);
        this.waitScoreBoard = config.getString("waitScoreBoard", this.waitScoreBoard);
        this.waitTimeBottom = config.getString("waitTimeBottom", this.waitTimeBottom);
        this.waitBottom = config.getString("waitBottom", this.waitBottom);
        this.gameTimeScoreBoard = config.getString("gameTimeScoreBoard", this.gameTimeScoreBoard);
        this.gameTimeBottom = config.getString("gameTimeBottom", this.gameTimeBottom);
        this.gameTimeRespawnBottom = config.getString("gameTimeRespawnBottom", this.gameTimeRespawnBottom);
        this.victoryMessage = config.getString("victoryMessage", this.victoryMessage);
        //ui
        this.userMenuButton1 = config.getString("userMenuButton1", this.userMenuButton1);
        this.userMenuButton2 = config.getString("userMenuButton2", this.userMenuButton2);
        this.userMenuButton3 = config.getString("userMenuButton3", this.userMenuButton3);
        this.userMenuButton4 = config.getString("userMenuButton4", this.userMenuButton4);
        this.adminMenuSetLevel = config.getString("adminMenuSetLevel", this.adminMenuSetLevel);
        this.adminMenuButton1 = config.getString("adminMenuButton1", this.adminMenuButton1);
        this.adminMenuButton2 = config.getString("adminMenuButton2", this.adminMenuButton2);
        this.adminMenuButton3 = config.getString("adminMenuButton3", this.adminMenuButton3);
        this.adminMenuButton4 = config.getString("adminMenuButton4", this.adminMenuButton4);
        this.adminMenuButton5 = config.getString("adminMenuButton5", this.adminMenuButton5);
        this.adminMenuButton6 = config.getString("adminMenuButton6", this.adminMenuButton6);
        this.adminTimeMenuInputText1 = config.getString("adminTimeMenuInputText1", this.adminTimeMenuInputText1);
        this.adminTimeMenuInputText2 = config.getString("adminTimeMenuInputText2", this.adminTimeMenuInputText2);
        this.adminTimeMenuInputText3 = config.getString("adminTimeMenuInputText3", this.adminTimeMenuInputText3);
        this.joinRoomOK = config.getString("joinRoomOK", this.joinRoomOK);
        this.buttonOK = config.getString("buttonOK", this.buttonOK);
        this.buttonReturn = config.getString("buttonReturn", this.buttonReturn);
        this.recordListButton1 = config.getString("recordListButton1", this.recordListButton1);
        this.recordListButton2 = config.getString("recordListButton2", this.recordListButton2);
        this.recordListButton3 = config.getString("recordListButton3", this.recordListButton3);
        this.recordListButton4 = config.getString("recordListButton4", this.recordListButton4);
        this.recordListButton5 = config.getString("recordListButton5", this.recordListButton5);
        this.killsRanking = config.getString("killsRanking", this.killsRanking);
        this.deathsRanking = config.getString("deathsRanking", this.deathsRanking);
        this.victoryRanking = config.getString("victoryRanking", this.victoryRanking);
        this.defeatRanking = config.getString("defeatRanking", this.defeatRanking);
        this.ranking = config.getString("ranking", this.ranking);
        this.playerGameRecord = config.getString("playerGameRecord", this.playerGameRecord);
        //物品
        this.itemQuitRoom = config.getString("itemQuitRoom", this.itemQuitRoom);
        this.itemQuitRoomLore = config.getString("itemQuitRoomLore", this.itemQuitRoomLore);
        this.itemTeamSelectRed = config.getString("itemTeamSelectRed", this.itemTeamSelectRed);
        this.itemTeamSelectBlue = config.getString("itemTeamSelectBlue", this.itemTeamSelectBlue);
        this.itemGrenade = config.getString("itemGrenade", this.itemGrenade);
        this.itemGrenadeLore = config.getString("itemGrenadeLore", this.itemGrenadeLore);
        this.itemFlashBang = config.getString("itemFlashBang", this.itemFlashBang);
        this.itemFlashBangLore = config.getString("itemFlashBangLore", this.itemFlashBangLore);
    }

}
