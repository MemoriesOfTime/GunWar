package cn.lanink.gunwar.ui;

import cn.lanink.gunwar.GunWar;
import cn.lanink.gunwar.utils.GameRecord;
import cn.lanink.gunwar.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;

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
        GuiType cache = this.gunWar.getGuiCache().get(event.getFormID());
        if (cache == null) return;
        this.gunWar.getGuiCache().remove(event.getFormID());
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
                        GuiCreate.sendRoomJoinOkMenu(player, simple.getResponse().getClickedButton().getText());
                    }
                    break;
                case RECORD_LIST:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            GuiCreate.sendGameRecord(player);
                            break;
                        case 1:
                            GuiCreate.sendRankingList(player, GameRecord.type.KILLS);
                            break;
                        case 2:
                            GuiCreate.sendRankingList(player, GameRecord.type.DEATHS);
                            break;
                        case 3:
                            GuiCreate.sendRankingList(player, GameRecord.type.VICTORY);
                            break;
                        case 4:
                            GuiCreate.sendRankingList(player, GameRecord.type.DEFEAT);
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