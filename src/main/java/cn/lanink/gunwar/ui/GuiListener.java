package cn.lanink.gunwar.ui;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.item.weapon.MeleeWeapon;
import cn.lanink.gunwar.utils.Language;
import cn.lanink.gunwar.utils.gamerecord.RecordType;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;

import java.io.File;

public class GuiListener implements Listener {

    private final GunWar gunWar;
    private final Language language;

    public GuiListener(GunWar gunWar) {
        this.gunWar = gunWar;
        this.language = gunWar.getLanguage();
    }

    /**
     * 玩家操作ui事件
     * 直接执行现有命令，减小代码重复量，也便于维护
     * @param event 事件
     */
    @EventHandler
    public void onPlayerFormResponded(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getWindow() == null || event.getResponse() == null) {
            return;
        }
        GuiType cache = GuiCreate.UI_CACHE.containsKey(player) ? GuiCreate.UI_CACHE.get(player).get(event.getFormID()) : null;
        if (cache == null) return;
        GuiCreate.UI_CACHE.get(player).remove(event.getFormID());
        if (event.getWindow() instanceof FormWindowSimple) {
            FormWindowSimple simple = (FormWindowSimple) event.getWindow();
            switch (cache) {
                case USER_MENU:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            GunWar.getInstance().getServer().dispatchCommand(player, this.gunWar.getCmdUser() + " join");
                            break;
                        case 1:
                            GunWar.getInstance().getServer().dispatchCommand(player, this.gunWar.getCmdUser() + " quit");
                            break;
                        case 2:
                            GuiCreate.sendRoomListMenu(player);
                            break;
                        case 3:
                            GuiCreate.sendRecordList(player);
                            break;
                    }
                    break;
                case ROOM_LIST_MENU:
                    if (simple.getResponse().getClickedButton().getText().equals(language.buttonReturn)) {
                        GuiCreate.sendUserMenu(player);
                    }else {
                        GuiCreate.sendRoomJoinOkMenu(player, simple.getResponse().getClickedButton().getText().split("\n")[0]);
                    }
                    break;
                case RECORD_LIST:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            GuiCreate.sendGameRecord(player);
                            break;
                        case 1:
                            GuiCreate.sendRankingList(player, RecordType.KILLS);
                            break;
                        case 2:
                            GuiCreate.sendRankingList(player, RecordType.DEATHS);
                            break;
                        case 3:
                            GuiCreate.sendRankingList(player, RecordType.VICTORY);
                            break;
                        case 4:
                            GuiCreate.sendRankingList(player, RecordType.DEFEAT);
                            break;
                        case 5:
                            GuiCreate.sendUserMenu(player);
                            break;
                    }
                    break;
                case ADMIN_MENU:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            GunWar.getInstance().getServer().dispatchCommand(player, this.gunWar.getCmdAdmin() + " setwaitspawn");
                            break;
                        case 1:
                            GunWar.getInstance().getServer().dispatchCommand(player, this.gunWar.getCmdAdmin() + " setredspawn");
                            break;
                        case 2:
                            GunWar.getInstance().getServer().dispatchCommand(player, this.gunWar.getCmdAdmin() + " setbluespawn");
                            break;
                        case 3:
                            GuiCreate.sendAdminTimeMenu(player);
                            break;
                        case 4:
                            GuiCreate.sendAdminModeMenu(player);
                            break;
                        case 5:
                            GunWar.getInstance().getServer().dispatchCommand(player, this.gunWar.getCmdAdmin() + " reloadroom");
                            break;
                        case 6:
                            GunWar.getInstance().getServer().dispatchCommand(player, this.gunWar.getCmdAdmin() + " unloadroom");
                            break;
                    }
                    break;
            }
        }else if (event.getWindow() instanceof FormWindowCustom) {
            FormWindowCustom custom = (FormWindowCustom) event.getWindow();
            switch (cache) {
                case ADMIN_TIME_MENU:
                    this.gunWar.getServer().dispatchCommand(player, this.gunWar.getCmdAdmin() + " setwaittime " + custom.getResponse().getInputResponse(0));
                    this.gunWar.getServer().dispatchCommand(player, this.gunWar.getCmdAdmin() + " setgametime " + custom.getResponse().getInputResponse(1));
                    this.gunWar.getServer().dispatchCommand(player, this.gunWar.getCmdAdmin() + " setvictoryscore " + custom.getResponse().getInputResponse(2));
                    break;
                case ADMIN_MODE_MENU:
                    this.gunWar.getServer().dispatchCommand(player, this.gunWar.getCmdAdmin() + " setgamemode " +
                        custom.getResponse().getDropdownResponse(0).getElementID());
                    break;
                case ADMIN_ITEM_ADD_WEAPON_MELEE:
                    String name = custom.getResponse().getInputResponse(0);
                    File file = new File(this.gunWar.getItemManage().getMeleeWeaponFolder() + "/" + name + ".yml");
                    if (file.exists()) {
                        player.sendMessage(name + " 已存在！");
                        return;
                    }else {
                        Config config = new Config(Config.YAML);
                        config.set("showName", custom.getResponse().getInputResponse(1));
                        String stringID = custom.getResponse().getInputResponse(2);
                        String[] split = stringID.split(":");
                        if (split.length == 2) {
                            try {
                                Integer.parseInt(split[0]);
                                Integer.parseInt(split[1]);
                                config.set("id", stringID);
                            } catch (NumberFormatException e) {
                                player.sendMessage(name + " 物品ID格式错误！");
                                return;
                            }
                        }else {
                            try {
                                Integer.parseInt(stringID);
                                config.set("id", stringID);
                            } catch (NumberFormatException e) {
                                player.sendMessage(name + " 物品ID格式错误！");
                                return;
                            }
                        }
                        try {
                            String minDamage = custom.getResponse().getInputResponse(3);
                            String maxDamage = custom.getResponse().getInputResponse(4);
                            Integer.parseInt(minDamage);
                            Integer.parseInt(maxDamage);
                            config.set("damage", minDamage + "-" + maxDamage);
                        } catch (NumberFormatException e) {
                            player.sendMessage(name + " 伤害只能输入数字！");
                            return;
                        }
                        try {
                            config.set("attackCooldown", Integer.parseInt(custom.getResponse().getInputResponse(5)));
                        } catch (NumberFormatException e) {
                            player.sendMessage(name + " 攻击冷却只能输入数字！");
                            return;
                        }
                        try {
                            config.set("knockBack", Double.parseDouble(custom.getResponse().getInputResponse(6)));
                        } catch (NumberFormatException e) {
                            player.sendMessage(name + " 击退只能输入数字！");
                            return;
                        }
                        config.set("infiniteDurability", custom.getResponse().getToggleResponse(7));
                        config.set("killMessage", custom.getResponse().getInputResponse(8));
                        config.save(file, true);
                        this.gunWar.getItemManage().getMeleeWeaponMap().put(name, new MeleeWeapon(name, config));
                        player.sendMessage(name + " 添加成功！");
                    }
                    break;
            }
        }else if (event.getWindow() instanceof FormWindowModal) {
            FormWindowModal modal = (FormWindowModal) event.getWindow();
            switch (cache) {
                case ROOM_JOIN_OK:
                    if (modal.getResponse().getClickedButtonId() == 0 && !modal.getButton1().equals(language.buttonReturn)) {
                        String[] s = modal.getContent().split("\"");
                        GunWar.getInstance().getServer().dispatchCommand(
                                player, this.gunWar.getCmdUser() + " join " + s[1].replace("§e", "").trim());
                    }else {
                        GuiCreate.sendRoomListMenu(player);
                    }
                    break;
                case GAME_RECORD:
                case RANKING_LIST:
                    if (modal.getResponse().getClickedButtonId() == 1) {
                        GuiCreate.sendRecordList(player);
                    }
                    break;
            }
        }
    }

}