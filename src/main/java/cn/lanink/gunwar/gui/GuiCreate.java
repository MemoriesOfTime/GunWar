package cn.lanink.gunwar.gui;

import cn.lanink.gamecore.form.element.ResponseElementButton;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowCustom;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowModal;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowSimple;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.room.base.BaseRoom;
import cn.lanink.gunwar.room.base.GunWarGameRoomManager;
import cn.lanink.gunwar.room.base.RoomConfig;
import cn.lanink.gunwar.room.base.Team;
import cn.lanink.gunwar.supplier.SupplyConfigManager;
import cn.lanink.gunwar.utils.Tools;
import cn.lanink.gunwar.utils.gamerecord.GameRecord;
import cn.lanink.gunwar.utils.gamerecord.RankingManager;
import cn.lanink.gunwar.utils.gamerecord.RecordType;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.*;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author lt_name
 */
public class GuiCreate {

    public static final String PLUGIN_NAME = "§l§7[§1G§2u§3n§4W§5a§6r§7]";
    public static final HashMap<Player, HashMap<Integer, GuiType>> UI_CACHE = new HashMap<>();

    /**
     * 显示用户菜单
     * @param player 玩家
     */
    public static void sendUserMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME);

        simple.addButton(
                new ResponseElementButton(language.translateString("userMenuButton1"),
                        new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, "textures/ui/switch_start_button")
                ).onClicked(cp -> GunWar.getInstance().getServer().dispatchCommand(cp, GunWar.getInstance().getCmdUser() + " join"))
        );
        simple.addButton(
                new ResponseElementButton(language.translateString("userMenuButton2"),
                        new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, "textures/ui/switch_select_button")
                ).onClicked(cp -> GunWar.getInstance().getServer().dispatchCommand(cp, GunWar.getInstance().getCmdUser() + " quit"))
        );
        simple.addButton(
                new ResponseElementButton(language.translateString("userMenuButton3"),
                        new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, "textures/ui/servers")
                ).onClicked(GuiCreate::sendRoomListMenu)
        );
        simple.addButton(
                new ResponseElementButton(language.translateString("userMenuButton4"),
                        new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, "textures/ui/creative_icon")
                ).onClicked(GuiCreate::sendRecordList)
        );

        player.showFormWindow(simple);
    }

    /**
     * 显示管理菜单
     * @param player 玩家
     */
    public static void sendAdminMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME);

        simple.addButton(
                new ResponseElementButton(language.translateString("gui_admin_main_createRoom"),
                        new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, "textures/ui/World")
                ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, GunWar.getInstance().getCmdAdmin() + " CreateRoom"))
        );
        simple.addButton(
                new ResponseElementButton(language.translateString("gui_admin_main_setRoom"),
                        new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, "textures/ui/dev_glyph_color")
                ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, GunWar.getInstance().getCmdAdmin() + " SetRoom"))
        );
        simple.addButton(
                new ResponseElementButton(language.translateString("gui_admin_main_reloadAllRoom"),
                        new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, "textures/ui/refresh_light")
                ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, GunWar.getInstance().getCmdAdmin() + " reloadroom"))
        );
        simple.addButton(
                new ResponseElementButton(language.translateString("gui_admin_main_unloadAllRoom"),
                        new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, "textures/ui/redX1")
                ).onClicked(cp -> Server.getInstance().dispatchCommand(cp, GunWar.getInstance().getCmdAdmin() + " unloadroom"))
        );

        player.showFormWindow(simple);
    }

    /**
     * 显示创建房间菜单（选择地图）
     * @param player 玩家
     */
    public static void sendCreateRoomMenu(Player player) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME,
                GunWar.getInstance().getLanguage().translateString("gui_admin_room_selectWorld"));
        for (Level level : Server.getInstance().getLevels().values()) {
            simple.addButton(new ResponseElementButton(level.getFolderName())
                    .onClicked(cp -> Server.getInstance().dispatchCommand(cp,
                            GunWar.getInstance().getCmdAdmin() + " CreateRoom " + level.getFolderName())));
        }
        player.showFormWindow(simple);
    }

    /**
     * 显示设置房间菜单（选择房间）
     * @param player 玩家
     */
    public static void sendSetRoomMenu(Player player) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME,
                GunWar.getInstance().getLanguage().translateString("gui_admin_room_selectRoom"));
        for (String roomName : GunWar.getInstance().getRoomConfigs().keySet()) {
            simple.addButton(new ResponseElementButton(roomName)
                    .onClicked(cp -> Server.getInstance().dispatchCommand(cp,
                            GunWar.getInstance().getCmdAdmin() + " SetRoom " + roomName)));
        }
        player.showFormWindow(simple);
    }

    /**
     * 显示设置时间菜单
     * @param player 玩家
     */
    public static void sendAdminTimeMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(PLUGIN_NAME);
        Config nowConfig = GunWar.getInstance().getRoomConfig(player.getLevel());
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText1"), "", nowConfig.getInt("waitTime", 60) + ""));
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText2"), "", nowConfig.getInt("gameTime", 300) + ""));
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText3"), "", nowConfig.getInt("victoryScore", 5) + ""));

        custom.onResponded((formResponseCustom, cp) -> {
            try {
                int waitTime = Integer.parseInt(formResponseCustom.getInputResponse(0));
                int gameTime = Integer.parseInt(formResponseCustom.getInputResponse(1));
                int victoryScore = Integer.parseInt(formResponseCustom.getInputResponse(2));
                if (waitTime < 1 || gameTime < 1 || victoryScore < 1) {
                    throw new Exception();
                }
                Config config = GunWar.getInstance().getRoomConfig(cp.getLevel());
                config.set("waitTime", waitTime);
                config.set("gameTime", gameTime);
                config.set("victoryScore", victoryScore);
                config.save();
                cp.sendMessage(language.translateString("adminSetWaitTime", waitTime));
                cp.sendMessage(language.translateString("adminSetGameTime", gameTime));
                cp.sendMessage(language.translateString("adminSetVictoryScore", victoryScore));
            } catch (Exception e) {
                cp.sendMessage(language.translateString("adminNotNumber"));
                if (GunWar.debug) {
                    GunWar.getInstance().getLogger().error("设置时间参数错误", e);
                }
            }
        });

        player.showFormWindow(custom);
    }

    /**
     * 设置商店菜单
     *
     * @param player 玩家
     */
    public static void sendAdminShopMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(PLUGIN_NAME);
        Config nowConfig = GunWar.getInstance().getRoomConfig(player.getLevel());

        ArrayList<String> sTypeList = new ArrayList<>();
        int nowChoose = 0;
        int i = 0;
        for (RoomConfig.SupplyType supplyType : RoomConfig.SupplyType.values()) {
            sTypeList.add(Tools.getShowSupplyType(supplyType));
            if (nowConfig.exists("supplyType") && supplyType.name().equalsIgnoreCase(nowConfig.getString("supplyType"))) {
                nowChoose = i;
            }
            i++;
        }
        custom.addElement(new ElementDropdown(language.translateString("gui_admin_AdminShopMenu_SupplyType"), sTypeList, nowChoose)); //0
        nowChoose = 0;
        i = 0;
        if (nowConfig.exists("supply")) {
            for (String string : SupplyConfigManager.getSUPPLY_CONFIG_MAP().keySet()) {
                if (string.equalsIgnoreCase(nowConfig.getString("supply"))) {
                    nowChoose = i;
                    break;
                }
                i++;
            }
        }
        custom.addElement(new ElementDropdown(language.translateString("gui_admin_AdminShopMenu_Supply", Tools.getShowTeamName(Team.RED)), new ArrayList<>(SupplyConfigManager.getSUPPLY_CONFIG_MAP().keySet()), nowChoose)); //1
        custom.addElement(new ElementDropdown(language.translateString("gui_admin_AdminShopMenu_Supply", Tools.getShowTeamName(Team.BLUE)), new ArrayList<>(SupplyConfigManager.getSUPPLY_CONFIG_MAP().keySet()), nowChoose)); //2
        custom.addElement(new ElementInput(
                language.translateString("gui_admin_AdminShopMenu_SupplyEnableTime",
                        Tools.getShowSupplyType(RoomConfig.SupplyType.ONLY_ROUND_START)),
                "",
                nowConfig.getInt("supplyEnableTime", 10) + ""
        )); //3

        custom.onResponded((formResponseCustom, cp) -> {
            try {
                Config config = GunWar.getInstance().getRoomConfig(cp.getLevel());
                List<RoomConfig.SupplyType> list = Arrays.asList(RoomConfig.SupplyType.values());
                RoomConfig.SupplyType supplyType = list.get(formResponseCustom.getDropdownResponse(0).getElementID());
                config.set("supplyType", supplyType.name());
                cp.sendMessage(language.translateString("admin_Set_SupplyType", Tools.getShowSupplyType(supplyType)));

                if (supplyType != RoomConfig.SupplyType.CLOSE) {
                    String redConfig = formResponseCustom.getDropdownResponse(1).getElementContent();
                    config.set("redTeamSupply", redConfig);
                    cp.sendMessage(language.translateString("admin_Set_Supply", redConfig, Tools.getShowTeamName(Team.RED)));

                    String blueConfig = formResponseCustom.getDropdownResponse(2).getElementContent();
                    config.set("blueTeamSupply", blueConfig);
                    cp.sendMessage(language.translateString("admin_Set_Supply", blueConfig, Tools.getShowTeamName(Team.BLUE)));

                    if (supplyType == RoomConfig.SupplyType.ONLY_ROUND_START) {
                        int time = Tools.toInt(formResponseCustom.getInputResponse(3));
                        if (time < 1) {
                            cp.sendMessage(language.translateString("admin_Set_SupplyEnableTime_Short"));
                            return;
                        }
                        config.set("supplyEnableTime", time);
                        cp.sendMessage(language.translateString("admin_Set_SupplyEnableTime", time));
                    }
                }
                config.save();
            } catch (Exception e) {
                cp.sendMessage(language.translateString("adminNotNumber"));
                if (GunWar.debug) {
                    GunWar.getInstance().getLogger().error("设置商店错误：", e);
                }
            }
        });

        player.showFormWindow(custom);
    }

    /**
     * 设置房间游戏人数菜单
     * @param player 玩家
     */
    public static void sendAdminPlayersMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(PLUGIN_NAME);
        Config nowConfig = GunWar.getInstance().getRoomConfig(player.getLevel());
        custom.addElement(new ElementInput(language.translateString("adminPlayersMenuInputText1"), "", nowConfig.getInt("minPlayers", 2) + ""));
        custom.addElement(new ElementInput(language.translateString("adminPlayersMenuInputText2"), "", nowConfig.getInt("maxPlayers", 10) + ""));

        custom.onResponded((formResponseCustom, cp) -> {
            try {
                int minPlayers = Integer.parseInt(formResponseCustom.getInputResponse(0));
                int maxPlayers = Integer.parseInt(formResponseCustom.getInputResponse(1));
                if (minPlayers < 1 || maxPlayers < 2) {
                    throw new Exception("");
                }
                Config config = GunWar.getInstance().getRoomConfig(cp.getLevel());
                config.set("minPlayers", minPlayers);
                config.set("maxPlayers", maxPlayers);
                config.save();
                cp.sendMessage(language.translateString("adminSetMinPlayers", minPlayers));
                cp.sendMessage(language.translateString("adminSetMaxPlayers", maxPlayers));
            } catch (Exception e) {
                cp.sendMessage(language.translateString("adminNotNumber"));
            }
        });

        player.showFormWindow(custom);
    }

    /**
     * 设置房间模式菜单
     * @param player 玩家
     */
    public static void sendAdminModeMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(PLUGIN_NAME);
        Config nowConfig = GunWar.getInstance().getRoomConfig(player.getLevel());
        LinkedList<String> list = new LinkedList<>(GunWarGameRoomManager.getGameRoomClassMap().keySet());
        LinkedList<String> showList = new LinkedList<>();
        int nowChoose = 0;
        int i = 0;
        for (String gameMode : list) {
            showList.add(Tools.getShowGameMode(gameMode));
            if (nowConfig.exists("gameMode") && gameMode.equalsIgnoreCase(nowConfig.getString("gameMode"))) {
                nowChoose = i;
            }
            i++;
        }
        custom.addElement(new ElementDropdown("\n\n\n" +
                language.translateString("adminMenuSetLevel", player.getLevel().getName()),
                showList,
                nowChoose
        )); //0

        custom.onResponded((formResponseCustom, cp) -> {
            String gameMode = list.get(formResponseCustom.getDropdownResponse(0).getElementID());
            if (GunWarGameRoomManager.getGameRoomClassMap().containsKey(gameMode)) {
                Config config = GunWar.getInstance().getRoomConfig(player.getLevel());
                config.set("gameMode", gameMode);
                config.save();
                cp.sendMessage(language.translateString("adminSetGameMode", Tools.getShowGameMode(gameMode)));
            }else {
                cp.sendMessage(language.translateString("gameMode_NotFound", Tools.getShowGameMode(gameMode) + " (" + gameMode + ")"));
            }
        });

        player.showFormWindow(custom);
    }

    public static void sendAdminItemAddWeaponMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME, "");
        simple.addButton(new ResponseElementButton(language.translateString("gui_admin_item_add_weapon_melee"))
                .onClicked(GuiCreate::sendAdminItemAddWeaponMeleeMenu));
        simple.addButton(new ResponseElementButton(language.translateString("gui_admin_item_add_weapon_projectile"))
                .onClicked(GuiCreate::sendAdminItemAddWeaponProjectileMenu));
        simple.addButton(new ResponseElementButton(language.translateString("gui_admin_item_add_weapon_gun"))
                .onClicked(GuiCreate::sendAdminItemAddWeaponGunMenu));
        player.showFormWindow(simple);
    }

    public static void sendAdminItemAddWeaponMeleeMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_name"), "", "Sword"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_showName"), "", "a demo Sword"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_id"), "", "272:0"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_lore"), "", "剑\n这是一个配置演示"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_minDamage"), "", "1"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_maxDamage"), "", "2"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_attackCooldown"), "", "20"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_knockBack"), "", "0.3"));
        custom.addElement(new ElementToggle(language.translateString("gui_admin_item_weapon_infiniteDurability")));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_killMessage"), "", "%damager% --[+＝＝》 %player%"));
        showFormWindow(player, custom, GuiType.ADMIN_ITEM_ADD_WEAPON_MELEE);
    }

    public static void sendAdminItemAddWeaponProjectileMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_name"), "", "Grenade"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_showName"), "", "a demo Grenade"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_id"), "", "344:0"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_lore"), "", "手榴弹\n这是一个配置演示"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_minDamage"), "", "1"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_maxDamage"), "", "2"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_particle"), "", "HugeExplodeSeedParticle@Vector3:pos"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_attackCooldown"), "", "20"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_range"), "", "5"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_killMessage"), "", "%damager% ☼ %player%"));
        showFormWindow(player, custom, GuiType.ADMIN_ITEM_ADD_WEAPON_PROJECTILE);
    }

    public static void sendAdminItemAddWeaponGunMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_name"), "", "Gun"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_showName"), "", "a demo gun"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_id"), "", "290:0"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_lore"), "", "枪\n这是一个配置演示"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_minDamage"), "", "1"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_maxDamage"), "", "2"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_attackCooldown"), "", "10"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_maxMagazine"), "", "30"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_reloadTime"), "", "5"));
        custom.addElement(new ElementToggle(language.translateString("gui_admin_item_weapon_reloadInterrupted")));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_bulletGravity"), "", "0.03"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_bulletMotionMultiply"), "", "1.5"));
        custom.addElement(new ElementInput(language.translateString("gui_admin_item_weapon_killMessage"), "", "%damager% ︻┳═一 %player%"));
        showFormWindow(player, custom, GuiType.ADMIN_ITEM_ADD_WEAPON_GUN);
    }


    /**
     * 显示房间列表菜单
     * @param player 玩家
     */
    public static void sendRoomListMenu(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME, "");

        for (Map.Entry<String, BaseRoom> entry : GunWar.getInstance().getGameRoomManager().getGameRoomMap().entrySet()) {
            simple.addButton(new ResponseElementButton("§e" + entry.getKey() +
                    "\n§r§eMode: " + Tools.getShowGameMode(entry.getValue().getGameMode()) +
                            " Player: " + entry.getValue().getPlayerDataMap().size() + "/" + entry.getValue().getMaxPlayers(),
                    new ElementButtonImageData("path", "textures/ui/switch_start_button"))
                    .onClicked(cp -> sendRoomJoinOkMenu(cp, entry.getKey())));
        }
        simple.addButton(new ResponseElementButton(language.translateString("buttonReturn"),
                new ElementButtonImageData("path", "textures/ui/cancel")).onClicked(GuiCreate::sendUserMenu));

        player.showFormWindow(simple);
    }

    /**
     * 加入房间确认(自选)
     * @param player 玩家
     */
    public static void sendRoomJoinOkMenu(Player player, String roomName) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowModal modal;
        BaseRoom room = GunWar.getInstance().getGameRoomManager().getGameRoom(roomName);
        if (room != null) {
            if (room.getStatus() == 2 || room.getStatus() == 3) {
                modal = new AdvancedFormWindowModal(
                        PLUGIN_NAME, language.translateString("joinRoomIsPlaying"),
                        language.translateString("buttonReturn"),
                        language.translateString("buttonReturn"));
            }else if (room.getPlayerDataMap().size() > 15){
                modal = new AdvancedFormWindowModal(
                        PLUGIN_NAME, language.translateString("joinRoomIsFull"),
                        language.translateString("buttonReturn"),
                        language.translateString("buttonReturn"));
            }else {
                modal = new AdvancedFormWindowModal(
                        PLUGIN_NAME, language.translateString("joinRoomOK", "\"" + roomName + "\""),
                        language.translateString("buttonOK"),
                        language.translateString("buttonReturn"));
            }
        }else {
            modal = new AdvancedFormWindowModal(
                    PLUGIN_NAME, language.translateString("joinRoomIsNotFound"),
                    language.translateString("buttonReturn"),
                    language.translateString("buttonReturn"));
        }
        showFormWindow(player, modal, GuiType.ROOM_JOIN_OK);
    }

    /**
     * 显示战绩列表
     * @param player 玩家
     */
    public static void sendRecordList(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME);

        simple.addButton(new ResponseElementButton(language.translateString("recordListButton1"), new ElementButtonImageData("path", "textures/ui/copy"))
                .onClicked(GuiCreate::sendGameRecord));
        simple.addButton(new ResponseElementButton(language.translateString("recordListButton2"), new ElementButtonImageData("path", "textures/ui/creative_icon"))
                .onClicked(cp -> GuiCreate.sendRankingList(cp, RecordType.KILLS)));
        simple.addButton(new ResponseElementButton(language.translateString("recordListButton3"), new ElementButtonImageData("path", "textures/ui/creative_icon"))
                .onClicked(cp -> GuiCreate.sendRankingList(cp, RecordType.DEATHS)));
        simple.addButton(new ResponseElementButton(language.translateString("recordListButton4"), new ElementButtonImageData("path", "textures/ui/creative_icon"))
                .onClicked(cp -> GuiCreate.sendRankingList(cp, RecordType.VICTORY)));
        simple.addButton(new ResponseElementButton(language.translateString("recordListButton5"),  new ElementButtonImageData("path", "textures/ui/creative_icon"))
                .onClicked(cp -> GuiCreate.sendRankingList(cp, RecordType.DEFEAT)));
        simple.addButton(new ResponseElementButton(language.translateString("buttonReturn"), new ElementButtonImageData("path", "textures/ui/cancel"))
                .onClicked(GuiCreate::sendUserMenu));

        player.showFormWindow(simple);
    }

    /**
     * 显示个人战绩
     * @param player 玩家
     */
    public static void sendGameRecord(Player player) {
        Language language = GunWar.getInstance().getLanguage();
        String s = language.translateString("playerGameRecord").replace("%kills%", GameRecord.getPlayerRecord(player, RecordType.KILLS) + "")
                .replace("%deaths%", GameRecord.getPlayerRecord(player, RecordType.DEATHS) + "")
                .replace("%victory%", GameRecord.getPlayerRecord(player, RecordType.VICTORY) + "")
                .replace("%defeat%", GameRecord.getPlayerRecord(player, RecordType.DEFEAT) + "");
        AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                PLUGIN_NAME, s, language.translateString("buttonOK"), language.translateString("buttonReturn"));

        modal.onClickedFalse(GuiCreate::sendRecordList);

        player.showFormWindow(modal);
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
                s.append(language.translateString("killsRanking")).append("\n");
                break;
            case DEATHS:
                s.append(language.translateString("deathsRanking")).append("\n");
                break;
            case VICTORY:
                s.append(language.translateString("victoryRanking")).append("\n");
                break;
            case DEFEAT:
                s.append(language.translateString("defeatRanking")).append("\n");
                break;
        }
        int i = 1;
        for (Map.Entry<String, Integer> entry : list.entrySet()) {
            s.append(language.translateString("ranking").replace("%ranking%", i + "")
                    .replace("%player%", entry.getKey())
                    .replace("%number%", entry.getValue() + "")).append("\n");
            i++;
            if (i > 10) {
                break;
            }
        }
        AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                PLUGIN_NAME, s.toString(),
                language.translateString("buttonOK"),
                language.translateString("buttonReturn"));

        modal.onClickedFalse(GuiCreate::sendRecordList);

        player.showFormWindow(modal);
    }

    public static void sendCreateRankMenu(@NotNull Player player) {
        Language language = GunWar.getInstance().getLanguage();
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(language.translateString("gui_admin_ranking_list_title"));

        custom.addElement(new ElementLabel(language.translateString("gui_admin_ranking_list_label"))); //0
        custom.addElement(new ElementInput(language.translateString("gui_admin_ranking_list_input_text"), language.translateString("gui_admin_ranking_list_input_placeholder"), language.translateString("gui_admin_ranking_list_input_default"))); //1
        ArrayList<String> list = new ArrayList<>();
        for (RecordType type : RecordType.values()) {
            list.add(type.getName());
        }
        custom.addElement(new ElementDropdown(language.translateString("gui_admin_ranking_list_dropdown"), list)); //2

        custom.onResponded((response, cp) -> {
            String name = response.getInputResponse(1);
            for (RankingManager.RankingData rankingData : RankingManager.getRANKING_DATA_LIST()) {
                if (rankingData.getName().equalsIgnoreCase(name)) {
                    cp.sendMessage(language.translateString("gui_admin_ranking_list_nameInvalid", name));
                    return;
                }
            }
            String stringType = response.getDropdownResponse(2).getElementContent();
            RankingManager.addRanking(new RankingManager.RankingData(name, RecordType.of(stringType), cp.getPosition()));
            RankingManager.save();
            RankingManager.load();
            cp.sendMessage(language.translateString("gui_admin_ranking_list_add_success", name));
        });

        custom.showToPlayer(player);
    }

    public static void showFormWindow(@NotNull Player player, @NotNull FormWindow window, @NotNull GuiType guiType) {
        UI_CACHE.computeIfAbsent(player, i -> new HashMap<>()).put(player.showFormWindow(window), guiType);
    }

}
