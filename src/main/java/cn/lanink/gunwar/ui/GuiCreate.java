package cn.lanink.gunwar.ui;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.Room;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.gamerecord.GameRecord;
import cn.lanink.gunwar.utils.gamerecord.RecordType;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.scheduler.Task;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;


public class GuiCreate {

    public static final String PLUGIN_NAME = "§l§7[§1G§2u§3n§4W§5a§6r§7]";
    public static final HashMap<Player, HashMap<Integer, GuiType>> UI_CACHE = new HashMap<>();

    /**
     * 显示用户菜单
     * @param player 玩家
     */
    public static void sendUserMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, "");
        simple.addButton(new ElementButton(language.userMenuButton1, new ElementButtonImageData("path", "textures/ui/switch_start_button")));
        simple.addButton(new ElementButton(language.userMenuButton2, new ElementButtonImageData("path", "textures/ui/switch_select_button")));
        simple.addButton(new ElementButton(language.userMenuButton3, new ElementButtonImageData("path", "textures/ui/servers")));
        simple.addButton(new ElementButton(language.userMenuButton4, new ElementButtonImageData("path", "textures/ui/creative_icon")));
        showFormWindow(player, simple, GuiType.USER_MENU);
    }

    /**
     * 显示管理菜单
     * @param player 玩家
     */
    public static void sendAdminMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, language.adminMenuSetLevel.replace("%name%", player.getLevel().getName()));
        simple.addButton(new ElementButton(language.adminMenuButton1, new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(language.adminMenuButton2, new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(language.adminMenuButton3, new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(language.adminMenuButton4, new ElementButtonImageData("path", "textures/ui/timer")));
        simple.addButton(new ElementButton(language.adminMenuButton5, new ElementButtonImageData("path", "textures/ui/dev_glyph_color")));
        simple.addButton(new ElementButton(language.adminMenuButton6,  new ElementButtonImageData("path", "textures/ui/refresh_light")));
        simple.addButton(new ElementButton(language.adminMenuButton7, new ElementButtonImageData("path", "textures/ui/redX1")));
        showFormWindow(player, simple, GuiType.ADMIN_MENU);
    }

    /**
     * 显示设置时间菜单
     * @param player 玩家
     */
    public static void sendAdminTimeMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput(language.adminTimeMenuInputText1, "", "60"));
        custom.addElement(new ElementInput(language.adminTimeMenuInputText2, "", "300"));
        custom.addElement(new ElementInput(language.adminTimeMenuInputText3, "", "5"));
        showFormWindow(player, custom, GuiType.ADMIN_TIME_MENU);
    }

    /**
     * 设置房间模式菜单
     * @param player 玩家
     */
    public static void sendAdminModeMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementDropdown("\n\n\n" +
                language.adminMenuSetLevel.replace("%name%", player.getLevel().getName()), new LinkedList<String>() {
            {
                add(language.classic);
                add(language.captureTheFlag);
            }
        }));
        showFormWindow(player, custom, GuiType.ADMIN_MODE_MENU);
    }

    /**
     * 显示房间列表菜单
     * @param player 玩家
     */
    public static void sendRoomListMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, "");
        for (Map.Entry<String, Room> entry : GunWar.getInstance().getRooms().entrySet()) {
            simple.addButton(new ElementButton("§e" + entry.getKey() +
                    "\n§r§eMode: " + entry.getValue().getGameMode().getName() +
                            " Player: " + entry.getValue().getPlayers().size() + "/10",
                    new ElementButtonImageData("path", "textures/ui/switch_start_button")));
        }
        simple.addButton(new ElementButton(language.buttonReturn, new ElementButtonImageData("path", "textures/ui/cancel")));
        showFormWindow(player, simple, GuiType.ROOM_LIST_MENU);
    }

    /**
     * 加入房间确认(自选)
     * @param player 玩家
     */
    public static void sendRoomJoinOkMenu(Player player, String roomName) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowModal modal;
        if (GunWar.getInstance().getRooms().containsKey(roomName.replace("§e", "").trim())) {
            Room room = GunWar.getInstance().getRooms().get(roomName.replace("§e", "").trim());
            if (room.getMode() == 2 || room.getMode() == 3) {
                modal = new FormWindowModal(
                        PLUGIN_NAME, language.joinRoomIsPlaying, language.buttonReturn, language.buttonReturn);
            }else if (room.getPlayers().size() > 15){
                modal = new FormWindowModal(
                        PLUGIN_NAME, language.joinRoomIsFull, language.buttonReturn, language.buttonReturn);
            }else {
                modal = new FormWindowModal(
                        PLUGIN_NAME, language.joinRoomOK.replace("%name%", "\"" + roomName + "\""), language.buttonOK, language.buttonReturn);
            }
        }else {
            modal = new FormWindowModal(
                    PLUGIN_NAME, language.joinRoomIsNotFound, language.buttonReturn, language.buttonReturn);
        }
        showFormWindow(player, modal, GuiType.ROOM_JOIN_OK);
    }

    /**
     * 显示战绩列表
     * @param player 玩家
     */
    public static void sendRecordList(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, "");
        simple.addButton(new ElementButton(language.recordListButton1, new ElementButtonImageData("path", "textures/ui/copy")));
        simple.addButton(new ElementButton(language.recordListButton2, new ElementButtonImageData("path", "textures/ui/creative_icon")));
        simple.addButton(new ElementButton(language.recordListButton3, new ElementButtonImageData("path", "textures/ui/creative_icon")));
        simple.addButton(new ElementButton(language.recordListButton4, new ElementButtonImageData("path", "textures/ui/creative_icon")));
        simple.addButton(new ElementButton(language.recordListButton5,  new ElementButtonImageData("path", "textures/ui/creative_icon")));
        simple.addButton(new ElementButton(language.buttonReturn, new ElementButtonImageData("path", "textures/ui/cancel")));
        showFormWindow(player, simple, GuiType.RECORD_LIST);
    }

    /**
     * 显示个人战绩
     * @param player 玩家
     */
    public static void sendGameRecord(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        String s = language.playerGameRecord.replace("%kills%", GameRecord.getPlayerRecord(player, RecordType.KILLS) + "")
                .replace("%deaths%", GameRecord.getPlayerRecord(player, RecordType.DEATHS) + "")
                .replace("%victory%", GameRecord.getPlayerRecord(player, RecordType.VICTORY) + "")
                .replace("%defeat%", GameRecord.getPlayerRecord(player, RecordType.DEFEAT) + "");
        FormWindowModal modal = new FormWindowModal(
                PLUGIN_NAME, s, language.buttonOK, language.buttonReturn);
        showFormWindow(player, modal, GuiType.GAME_RECORD);
    }

    /**
     * 显示排行榜
     * @param player 玩家
     */
    public static void sendRankingList(Player player, RecordType recordType) {
        Language language = GunWar.getInstance().getLanguage();
        LinkedHashMap<String,Integer> list = GameRecord.getRankingList(recordType);
        StringBuilder s = new StringBuilder();
        switch (recordType) {
            case KILLS:
                s.append(language.killsRanking).append("\n");
                break;
            case DEATHS:
                s.append(language.deathsRanking).append("\n");
                break;
            case VICTORY:
                s.append(language.victoryRanking).append("\n");
                break;
            case DEFEAT:
                s.append(language.defeatRanking).append("\n");
                break;
        }
        int i = 1;
        for (Map.Entry<String, Integer> entry : list.entrySet()) {
            s.append(language.ranking.replace("%ranking%", i + "")
                    .replace("%player%", entry.getKey())
                    .replace("%number%", entry.getValue() + "")).append("\n");
            i++;
            if (i > 10) {
                break;
            }
        }
        FormWindowModal modal = new FormWindowModal(
                PLUGIN_NAME, s.toString(), language.buttonOK, language.buttonReturn);
        showFormWindow(player, modal, GuiType.RANKING_LIST);
    }

    public static void showFormWindow(Player player, FormWindow window, GuiType guiType) {
        HashMap<Integer, GuiType> map;
        if (!UI_CACHE.containsKey(player)) {
            map = new HashMap<>();
            UI_CACHE.put(player, map);
        }else {
            map = UI_CACHE.get(player);
        }
        int id = player.showFormWindow(window);
        map.put(id, guiType);
        Server.getInstance().getScheduler().scheduleDelayedTask(GunWar.getInstance(), new Task() {
            @Override
            public void onRun(int i) {
                if (UI_CACHE.containsKey(player))
                    UI_CACHE.get(player).remove(id);
            }
        }, 2400);
    }

}
